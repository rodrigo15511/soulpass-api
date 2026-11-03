package br.com.fiap.dao;

import br.com.fiap.conexoes.ConexaoFactory;
import br.com.fiap.entities.AcaoRealizada;
import br.com.fiap.entities.Resgate;
import br.com.fiap.entities.Usuario;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Testa os UPDATEs condicionais que impedem processar a mesma coisa duas vezes.
 * Cada teste roda numa transacao que termina em ROLLBACK: nada fica gravado no banco.
 */
class ConcorrenciaTest {

    private static final int USUARIO_TESTE = 2;   // Ana (sql/02_carga_usuario_teste.sql)

    @Test
    void acaoSoPodeSerAprovadaUmaVez() throws Exception {
        Connection conexao = abrirTransacao();
        try {
            AcaoRealizadaDao acaoDao = new AcaoRealizadaDao(conexao);
            List<AcaoRealizada> pendentes = acaoDao.selecionarPendentes();
            assumeTrue(!pendentes.isEmpty(), "Nenhuma acao PENDENTE no banco para testar");
            int idAcao = pendentes.get(0).getIdAcao();

            assertEquals(1, acaoDao.atualizarStatusSePendente(idAcao, "APROVADO", 10));
            // segunda tentativa (o "segundo clique"): a acao ja nao esta PENDENTE
            assertEquals(0, acaoDao.atualizarStatusSePendente(idAcao, "APROVADO", 10));
        } finally {
            desfazer(conexao);
        }
    }

    @Test
    void debitoNaoDeixaSaldoNegativo() throws Exception {
        Connection conexao = abrirTransacao();
        try {
            UsuarioDao usuarioDao = new UsuarioDao(conexao);
            Usuario usuario = usuarioDao.buscarPorId(USUARIO_TESTE);
            assumeTrue(usuario != null && usuario.getSaldoPontos() >= 1, "Usuario de teste sem saldo");
            double saldo = usuario.getSaldoPontos();

            assertFalse(usuarioDao.debitarSaldo(USUARIO_TESTE, saldo + 1));   // mais do que tem
            assertTrue(usuarioDao.debitarSaldo(USUARIO_TESTE, saldo));        // exatamente o que tem
            assertFalse(usuarioDao.debitarSaldo(USUARIO_TESTE, 1));           // agora o saldo e zero
            assertEquals(0, usuarioDao.buscarPorId(USUARIO_TESTE).getSaldoPontos());
        } finally {
            desfazer(conexao);
        }
    }

    @Test
    void somaDePontosEFeitaPeloBanco() throws Exception {
        Connection conexao = abrirTransacao();
        try {
            UsuarioDao usuarioDao = new UsuarioDao(conexao);
            Usuario antes = usuarioDao.buscarPorId(USUARIO_TESTE);
            assumeTrue(antes != null, "Usuario de teste nao existe");

            usuarioDao.somarPontos(USUARIO_TESTE, 50, 50);
            usuarioDao.somarPontos(USUARIO_TESTE, 70, 70);

            Usuario depois = usuarioDao.buscarPorId(USUARIO_TESTE);
            assertEquals(antes.getSaldoPontos() + 120, depois.getSaldoPontos());
            assertEquals(antes.getPontosXp() + 120, depois.getPontosXp());
        } finally {
            desfazer(conexao);
        }
    }

    @Test
    void voucherSoPodeSerUsadoUmaVez() throws Exception {
        Connection conexao = abrirTransacao();
        try {
            ResgateDao resgateDao = new ResgateDao(conexao);
            Resgate disponivel = resgateDao.buscarPorUsuario(USUARIO_TESTE).stream()
                    .filter(r -> "DISPONIVEL".equals(r.getStatusVoucher()))
                    .findFirst().orElse(null);
            assumeTrue(disponivel != null, "Nenhum voucher DISPONIVEL para testar");

            assertEquals(1, resgateDao.utilizarSeDisponivel(disponivel.getIdResgate()));
            assertEquals(0, resgateDao.utilizarSeDisponivel(disponivel.getIdResgate()));
            // depois de usado, tambem nao pode ser cancelado
            assertEquals(0, resgateDao.deletarSeDisponivel(disponivel.getIdResgate()));
        } finally {
            desfazer(conexao);
        }
    }

    private Connection abrirTransacao() throws Exception {
        Connection conexao = new ConexaoFactory().conexao();
        conexao.setAutoCommit(false);
        return conexao;
    }

    private void desfazer(Connection conexao) throws Exception {
        conexao.rollback();
        conexao.close();
    }
}
