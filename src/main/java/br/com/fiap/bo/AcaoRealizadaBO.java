package br.com.fiap.bo;

import br.com.fiap.dao.AcaoRealizadaDao;
import br.com.fiap.dao.MissaoDao;
import br.com.fiap.dao.UsuarioDao;
import br.com.fiap.entities.AcaoRealizada;
import br.com.fiap.entities.Missao;
import br.com.fiap.entities.Usuario;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Regras de negocio da Acao Realizada - o coracao do SoulPass.
 *
 * Fluxo: o usuario registra a acao (PENDENTE) -> informa o codigo do ponto parceiro
 * -> codigo certo: APROVADO e os pontos da missao vao para o usuario
 * -> codigo errado: REJEITADO e nenhum ponto e creditado.
 */
public class AcaoRealizadaBO {

    // Selecionar todas
    public ArrayList<AcaoRealizada> selecionarBo() throws SQLException, ClassNotFoundException {
        AcaoRealizadaDao acaoDao = new AcaoRealizadaDao();
        try {
            return acaoDao.selecionar();
        } finally {
            acaoDao.fecharConexao();
        }
    }

    // Selecionar as acoes aguardando validacao
    public ArrayList<AcaoRealizada> selecionarPendentesBo() throws SQLException, ClassNotFoundException {
        AcaoRealizadaDao acaoDao = new AcaoRealizadaDao();
        try {
            return acaoDao.selecionarPendentes();
        } finally {
            acaoDao.fecharConexao();
        }
    }

    // Historico de acoes de um usuario
    public ArrayList<AcaoRealizada> buscarPorUsuarioBo(int idUsuario) throws SQLException, ClassNotFoundException {
        AcaoRealizadaDao acaoDao = new AcaoRealizadaDao();
        try {
            return acaoDao.buscarPorUsuario(idUsuario);
        } finally {
            acaoDao.fecharConexao();
        }
    }

    // Buscar por id - retorna null se nao existir
    public AcaoRealizada buscarPorIdBo(int idAcao) throws SQLException, ClassNotFoundException {
        AcaoRealizadaDao acaoDao = new AcaoRealizadaDao();
        try {
            return acaoDao.buscarPorId(idAcao);
        } finally {
            acaoDao.fecharConexao();
        }
    }

    // Registrar - a acao nasce PENDENTE, sem pontos, com um codigo de validacao gerado
    public AcaoRealizada registrarBo(AcaoRealizada acao) throws SQLException, ClassNotFoundException {
        if (acao == null) {
            throw new IllegalArgumentException("Dados da acao nao informados.");
        }
        if (!acao.metodoValido()) {
            throw new IllegalArgumentException("Metodo de validacao invalido! Use CHECKIN, CODIGO ou QRCODE.");
        }

        Usuario usuario = new UsuarioBO().buscarPorIdBo(acao.getIdUsuario());
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario " + acao.getIdUsuario() + " nao existe.");
        }
        if (!usuario.isAtivo()) {
            throw new IllegalStateException("Usuario inativo nao pode registrar acoes.");
        }

        Missao missao = new MissaoBO().buscarPorIdBo(acao.getIdMissao());
        if (missao == null) {
            throw new IllegalArgumentException("Missao " + acao.getIdMissao() + " nao existe.");
        }
        if (!missao.estaDisponivel()) {
            throw new IllegalStateException("A missao '" + missao.getTituloMissao() + "' nao esta disponivel.");
        }

        AcaoRealizadaDao acaoDao = new AcaoRealizadaDao();
        try {
            acao.setIdAcao(acaoDao.proximoId());
            acao.setMetodoValidacao(acao.getMetodoValidacao().toUpperCase());
            acao.setCodigoValidacao(AcaoRealizada.gerarCodigoValidacao(missao.getIdMissao(), acao.getMetodoValidacao()));
            acao.setStatusAprovacao(AcaoRealizada.STATUS_PENDENTE);
            acao.setPontosConquistados(0);
            acao.setDataConclusao(LocalDate.now());

            acaoDao.inserir(acao);
            return acaoDao.buscarPorId(acao.getIdAcao());
        } finally {
            acaoDao.fecharConexao();
        }
    }

    // Aprovar - confere o codigo; se correto grava APROVADO e credita os pontos ao usuario
    public AcaoRealizada aprovarBo(int idAcao, String codigoInformado) throws SQLException, ClassNotFoundException {
        AcaoRealizadaDao acaoDao = new AcaoRealizadaDao();
        UsuarioDao usuarioDao = new UsuarioDao();
        MissaoDao missaoDao = new MissaoDao();
        try {
            AcaoRealizada acao = acaoDao.buscarPorId(idAcao);
            if (acao == null) {
                return null;
            }
            if (!acao.estaPendente()) {
                throw new IllegalStateException("Esta acao ja esta com status " + acao.getStatusAprovacao() + ".");
            }
            if (codigoInformado == null || codigoInformado.isBlank()) {
                throw new IllegalArgumentException("Codigo de validacao nao informado.");
            }

            Missao missao = missaoDao.buscarPorId(acao.getIdMissao());
            if (missao == null || !missao.estaDisponivel()) {
                throw new IllegalStateException("A missao desta acao nao esta mais disponivel.");
            }

            // a regra do codigo esta na entidade (Sprint 3): muda o status e calcula os pontos
            acao.aprovar(codigoInformado, missao);
            acaoDao.atualizarStatus(idAcao, acao.getStatusAprovacao(), acao.getPontosConquistados());

            if (acao.foiAprovada()) {
                Usuario usuario = usuarioDao.buscarPorId(acao.getIdUsuario());
                usuario.creditarPontos(acao.getPontosConquistados(), missao.getTituloMissao());
                usuarioDao.atualizarPontos(usuario.getIdUsuario(), usuario.getSaldoPontos(), usuario.getPontosXp());
            }

            return acaoDao.buscarPorId(idAcao);
        } finally {
            acaoDao.fecharConexao();
            usuarioDao.fecharConexao();
            missaoDao.fecharConexao();
        }
    }

    // Rejeitar - recusa manual de uma acao pendente
    public AcaoRealizada rejeitarBo(int idAcao, String motivo) throws SQLException, ClassNotFoundException {
        AcaoRealizadaDao acaoDao = new AcaoRealizadaDao();
        try {
            AcaoRealizada acao = acaoDao.buscarPorId(idAcao);
            if (acao == null) {
                return null;
            }
            if (!acao.estaPendente()) {
                throw new IllegalStateException("Esta acao ja esta com status " + acao.getStatusAprovacao() + ".");
            }

            acao.rejeitar(motivo);
            acaoDao.atualizarStatus(idAcao, acao.getStatusAprovacao(), acao.getPontosConquistados());
            return acaoDao.buscarPorId(idAcao);
        } finally {
            acaoDao.fecharConexao();
        }
    }

    // Deletar - acao aprovada nao pode ser excluida (os pontos ja foram creditados)
    public boolean deletarBo(int idAcao) throws SQLException, ClassNotFoundException {
        AcaoRealizadaDao acaoDao = new AcaoRealizadaDao();
        try {
            AcaoRealizada acao = acaoDao.buscarPorId(idAcao);
            if (acao == null) {
                return false;
            }
            if (acao.foiAprovada()) {
                throw new IllegalStateException("Acao aprovada nao pode ser excluida: os pontos ja foram creditados.");
            }
            acaoDao.deletar(idAcao);
            return true;
        } finally {
            acaoDao.fecharConexao();
        }
    }
}
