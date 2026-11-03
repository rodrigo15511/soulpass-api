package br.com.fiap.bo;

import br.com.fiap.conexoes.ConexaoFactory;
import br.com.fiap.dao.AcaoRealizadaDao;
import br.com.fiap.dao.MissaoDao;
import br.com.fiap.dao.NivelDao;
import br.com.fiap.dao.UsuarioDao;
import br.com.fiap.entities.AcaoRealizada;
import br.com.fiap.entities.Missao;
import br.com.fiap.entities.Nivel;
import br.com.fiap.entities.Usuario;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Regras de negocio da Acao Realizada - o coracao do SoulPass.
 *
 * Fluxo: o usuario registra a acao (PENDENTE) -> informa o codigo do ponto parceiro
 * -> codigo certo: APROVADO, os pontos da missao vao para o usuario
 *    e, se o XP alcancar o proximo nivel, ele sobe de nivel automaticamente
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

    // Aprovar - confere o codigo; se correto grava APROVADO e credita os pontos ao usuario.
    // Tudo numa TRANSACAO: status da acao, pontos e nivel sao gravados juntos ou nada e gravado.
    public AcaoRealizada aprovarBo(int idAcao, String codigoInformado) throws SQLException, ClassNotFoundException {
        Connection conexao = new ConexaoFactory().conexao();
        conexao.setAutoCommit(false);

        AcaoRealizadaDao acaoDao = new AcaoRealizadaDao(conexao);
        UsuarioDao usuarioDao = new UsuarioDao(conexao);
        MissaoDao missaoDao = new MissaoDao(conexao);
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

            // so grava se ainda estiver PENDENTE: dois cliques em "aprovar" nao geram pontos em dobro
            if (acaoDao.atualizarStatusSePendente(idAcao, acao.getStatusAprovacao(), acao.getPontosConquistados()) == 0) {
                throw new IllegalStateException("Esta acao ja foi processada por outra requisicao.");
            }

            if (acao.foiAprovada()) {
                // soma feita pelo Oracle (SALDO + pontos): nao se perde ponto com aprovacoes simultaneas
                usuarioDao.somarPontos(acao.getIdUsuario(), acao.getPontosConquistados(), acao.getPontosConquistados());

                // le o usuario ja com o XP novo e confere se ele alcancou um nivel acima
                Usuario usuario = usuarioDao.buscarPorId(acao.getIdUsuario());
                if (promoverNivel(usuario, new NivelDao(conexao).selecionar())) {
                    usuarioDao.atualizarNivel(usuario.getIdUsuario(), usuario.getNivel().getIdNivel());
                }
            }

            conexao.commit();   // deu tudo certo: grava as alteracoes de uma vez
            return acaoDao.buscarPorId(idAcao);
        } catch (Exception e) {
            conexao.rollback(); // algo falhou no meio: desfaz tudo o que foi feito nesta transacao
            throw e;
        } finally {
            conexao.close();
        }
    }

    // promoverNivel - percorre os niveis do menor para o maior; subiuDeNivel (Sprint 3) so troca
    // quando o XP alcanca o nivel e ele e maior que o atual. Retorna true se o nivel mudou.
    static boolean promoverNivel(Usuario usuario, List<Nivel> niveisEmOrdem) {
        boolean subiu = false;
        for (Nivel nivel : niveisEmOrdem) {
            if (usuario.subiuDeNivel(nivel)) {
                subiu = true;
            }
        }
        return subiu;
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
            if (acaoDao.atualizarStatusSePendente(idAcao, acao.getStatusAprovacao(), acao.getPontosConquistados()) == 0) {
                throw new IllegalStateException("Esta acao ja foi processada por outra requisicao.");
            }
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
