package br.com.fiap.bo;

import br.com.fiap.conexoes.ConexaoFactory;
import br.com.fiap.dao.ResgateDao;
import br.com.fiap.dao.UsuarioDao;
import br.com.fiap.dao.VoucherDao;
import br.com.fiap.entities.Resgate;
import br.com.fiap.entities.Usuario;
import br.com.fiap.entities.Voucher;

import java.sql.Connection;
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

    // Resgatar - confere o saldo, debita os pontos e gera o voucher do usuario.
    // TRANSACAO: o debito do saldo e o registro do resgate sao gravados juntos ou nenhum dos dois.
    public Resgate resgatarBo(int idUsuario, int idVoucher) throws SQLException, ClassNotFoundException {
        Connection conexao = new ConexaoFactory().conexao();
        conexao.setAutoCommit(false);

        ResgateDao resgateDao = new ResgateDao(conexao);
        UsuarioDao usuarioDao = new UsuarioDao(conexao);
        VoucherDao voucherDao = new VoucherDao(conexao);
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

            // debita so o saldo (o XP e o nivel continuam iguais). A checagem do saldo e repetida
            // no proprio UPDATE: se outro resgate gastou os pontos nesse meio tempo, nao debita
            if (!usuarioDao.debitarSaldo(idUsuario, voucher.getCustoPontos())) {
                throw new IllegalStateException("Saldo insuficiente para este voucher.");
            }

            Resgate resgate = new Resgate();
            resgate.setIdResgate(resgateDao.proximoId());
            resgate.setIdUsuario(idUsuario);
            resgate.setIdVoucher(idVoucher);
            resgate.setCodigoGerado(Resgate.gerarCodigo(idUsuario, idVoucher));
            resgate.setDataResgate(LocalDate.now());
            resgate.setDataValidade(LocalDate.now().plusDays(Resgate.DIAS_VALIDADE));
            resgate.setStatusVoucher(Resgate.STATUS_DISPONIVEL);

            resgateDao.inserir(resgate);

            conexao.commit();   // saldo debitado + resgate gravado, juntos
            return resgateDao.buscarPorId(resgate.getIdResgate());
        } catch (Exception e) {
            conexao.rollback(); // falhou no meio: o saldo volta ao que era
            throw e;
        } finally {
            conexao.close();
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

            if (Resgate.STATUS_EXPIRADO.equalsIgnoreCase(resgate.getStatusVoucher())) {
                resgateDao.atualizar(resgate);   // grava o EXPIRADO
                throw new IllegalStateException(resultado);
            }

            // so marca como usado se ainda estiver DISPONIVEL: o mesmo voucher nao e usado 2x
            if (resgateDao.utilizarSeDisponivel(resgate.getIdResgate()) == 0) {
                throw new IllegalStateException("Este voucher ja foi utilizado.");
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

    // Cancelar - so resgates DISPONIVEIS; os pontos voltam para o saldo do usuario.
    // TRANSACAO: a devolucao dos pontos e a exclusao do resgate acontecem juntas.
    public boolean cancelarBo(int idResgate) throws SQLException, ClassNotFoundException {
        Connection conexao = new ConexaoFactory().conexao();
        conexao.setAutoCommit(false);

        ResgateDao resgateDao = new ResgateDao(conexao);
        UsuarioDao usuarioDao = new UsuarioDao(conexao);
        VoucherDao voucherDao = new VoucherDao(conexao);
        try {
            Resgate resgate = resgateDao.buscarPorId(idResgate);
            if (resgate == null) {
                return false;
            }
            if (!resgate.estaValido()) {
                throw new IllegalStateException("So e possivel cancelar vouchers DISPONIVEIS e dentro da validade.");
            }

            // exclui primeiro, e so se ainda estiver DISPONIVEL: dois cancelamentos nao devolvem pontos 2x
            if (resgateDao.deletarSeDisponivel(idResgate) == 0) {
                throw new IllegalStateException("Este resgate ja foi utilizado ou cancelado.");
            }

            // devolve so o saldo (o XP nao foi alterado no resgate), somando pelo Oracle
            Voucher voucher = voucherDao.buscarPorId(resgate.getIdVoucher());
            usuarioDao.somarPontos(resgate.getIdUsuario(), voucher.getCustoPontos(), 0);

            conexao.commit();
            return true;
        } catch (Exception e) {
            conexao.rollback();
            throw e;
        } finally {
            conexao.close();
        }
    }
}
