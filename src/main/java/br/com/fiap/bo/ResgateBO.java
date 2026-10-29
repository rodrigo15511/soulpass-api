package br.com.fiap.bo;

import br.com.fiap.dao.ResgateDao;
import br.com.fiap.dao.UsuarioDao;
import br.com.fiap.dao.VoucherDao;
import br.com.fiap.entities.Resgate;
import br.com.fiap.entities.Usuario;
import br.com.fiap.entities.Voucher;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Regras de negocio do Resgate - a troca de pontos por um voucher do catalogo.
 *
 * Fluxo: usuario escolhe um voucher -> o saldo e debitado (o XP nao muda)
 * -> nasce um Resgate DISPONIVEL com codigo unico e 90 dias de validade
 * -> o codigo e usado no transporte (UTILIZADO) ou vence (EXPIRADO).
 */
public class ResgateBO {

    // Selecionar todos
    public ArrayList<Resgate> selecionarBo() throws SQLException, ClassNotFoundException {
        ResgateDao resgateDao = new ResgateDao();
        try {
            return resgateDao.selecionar();
        } finally {
            resgateDao.fecharConexao();
        }
    }

    // Vouchers resgatados por um usuario
    public ArrayList<Resgate> buscarPorUsuarioBo(int idUsuario) throws SQLException, ClassNotFoundException {
        ResgateDao resgateDao = new ResgateDao();
        try {
            return resgateDao.buscarPorUsuario(idUsuario);
        } finally {
            resgateDao.fecharConexao();
        }
    }

    // Buscar por id - retorna null se nao existir
    public Resgate buscarPorIdBo(int idResgate) throws SQLException, ClassNotFoundException {
        ResgateDao resgateDao = new ResgateDao();
        try {
            return resgateDao.buscarPorId(idResgate);
        } finally {
            resgateDao.fecharConexao();
        }
    }

    // Buscar pelo codigo gerado - retorna null se nao existir
    public Resgate buscarPorCodigoBo(String codigo) throws SQLException, ClassNotFoundException {
        ResgateDao resgateDao = new ResgateDao();
        try {
            return resgateDao.buscarPorCodigo(codigo);
        } finally {
            resgateDao.fecharConexao();
        }
    }

    // Resgatar - confere o saldo, debita os pontos e gera o voucher do usuario
    public Resgate resgatarBo(int idUsuario, int idVoucher) throws SQLException, ClassNotFoundException {
        ResgateDao resgateDao = new ResgateDao();
        UsuarioDao usuarioDao = new UsuarioDao();
        VoucherDao voucherDao = new VoucherDao();
        try {
            Usuario usuario = usuarioDao.buscarPorId(idUsuario);
            if (usuario == null) {
                throw new IllegalArgumentException("Usuario " + idUsuario + " nao existe.");
            }

            Voucher voucher = voucherDao.buscarPorId(idVoucher);
            if (voucher == null) {
                throw new IllegalArgumentException("Voucher " + idVoucher + " nao existe.");
            }

            if (!usuario.isAtivo()) {
                throw new IllegalStateException("Usuario inativo nao pode resgatar vouchers.");
            }
            if (!usuario.podeResgatar(voucher)) {
                throw new IllegalStateException("Saldo insuficiente! Saldo atual: " + usuario.getSaldoPontos()
                        + " | custo do voucher: " + voucher.getCustoPontos() + " pontos.");
            }

            // debitarPontos (Sprint 3) tira so do saldo - o XP e o nivel continuam iguais
            usuario.debitarPontos(voucher.getCustoPontos(), "Resgate voucher " + voucher.getTipoTransporte());
            usuarioDao.atualizarPontos(idUsuario, usuario.getSaldoPontos(), usuario.getPontosXp());

            Resgate resgate = new Resgate();
            resgate.setIdResgate(resgateDao.proximoId());
            resgate.setIdUsuario(idUsuario);
            resgate.setIdVoucher(idVoucher);
            resgate.setCodigoGerado(Resgate.gerarCodigo(idUsuario, idVoucher));
            resgate.setDataResgate(LocalDate.now());
            resgate.setDataValidade(LocalDate.now().plusDays(Resgate.DIAS_VALIDADE));
            resgate.setStatusVoucher(Resgate.STATUS_DISPONIVEL);

            resgateDao.inserir(resgate);
            return resgateDao.buscarPorId(resgate.getIdResgate());
        } finally {
            resgateDao.fecharConexao();
            usuarioDao.fecharConexao();
            voucherDao.fecharConexao();
        }
    }

    // Utilizar - marca o voucher como usado; vencido vira EXPIRADO e nao pode ser usado
    public Resgate utilizarBo(String codigo) throws SQLException, ClassNotFoundException {
        ResgateDao resgateDao = new ResgateDao();
        try {
            Resgate resgate = resgateDao.buscarPorCodigo(codigo);
            if (resgate == null) {
                return null;
            }
            if (!Resgate.STATUS_DISPONIVEL.equalsIgnoreCase(resgate.getStatusVoucher())) {
                throw new IllegalStateException("Este voucher esta com status " + resgate.getStatusVoucher() + ".");
            }

            // utilizar (Sprint 3) decide entre UTILIZADO e EXPIRADO conforme a validade
            String resultado = resgate.utilizar();
            resgateDao.atualizar(resgate);

            if (Resgate.STATUS_EXPIRADO.equalsIgnoreCase(resgate.getStatusVoucher())) {
                throw new IllegalStateException(resultado);
            }

            return resgateDao.buscarPorId(resgate.getIdResgate());
        } finally {
            resgateDao.fecharConexao();
        }
    }

    // Expirar vencidos - rotina de manutencao; devolve a mensagem com a quantidade
    public String expirarVencidosBo() throws SQLException, ClassNotFoundException {
        ResgateDao resgateDao = new ResgateDao();
        try {
            return resgateDao.expirarVencidos();
        } finally {
            resgateDao.fecharConexao();
        }
    }

    // Cancelar - so resgates DISPONIVEIS; os pontos voltam para o saldo do usuario
    public boolean cancelarBo(int idResgate) throws SQLException, ClassNotFoundException {
        ResgateDao resgateDao = new ResgateDao();
        UsuarioDao usuarioDao = new UsuarioDao();
        VoucherDao voucherDao = new VoucherDao();
        try {
            Resgate resgate = resgateDao.buscarPorId(idResgate);
            if (resgate == null) {
                return false;
            }
            if (!resgate.estaValido()) {
                throw new IllegalStateException("So e possivel cancelar vouchers DISPONIVEIS e dentro da validade.");
            }

            Voucher voucher = voucherDao.buscarPorId(resgate.getIdVoucher());
            Usuario usuario = usuarioDao.buscarPorId(resgate.getIdUsuario());

            // devolve so o saldo - o XP nao foi alterado no resgate
            usuarioDao.atualizarPontos(usuario.getIdUsuario(),
                    usuario.getSaldoPontos() + voucher.getCustoPontos(), usuario.getPontosXp());
            resgateDao.deletar(idResgate);
            return true;
        } finally {
            resgateDao.fecharConexao();
            usuarioDao.fecharConexao();
            voucherDao.fecharConexao();
        }
    }
}
