package br.com.fiap.dao;

import br.com.fiap.conexoes.ConexaoFactory;
import br.com.fiap.entities.Usuario;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Prova que a transacao dos BOs funciona: com a mesma conexao compartilhada pelos DAOs,
 * um rollback desfaz o que foi alterado. Usa o banco real, mas nada fica gravado.
 */
class TransacaoRollbackTest {

    private static final int USUARIO_TESTE = 2;   // Ana (sql/02_carga_usuario_teste.sql)

    @Test
    void rollbackDesfazAlteracaoDosPontos() throws Exception {
        Usuario antes = lerUsuario();
        assertNotNull(antes, "Usuario de teste 2 precisa existir no banco");

        Connection conexao = new ConexaoFactory().conexao();
        conexao.setAutoCommit(false);
        try {
            UsuarioDao usuarioDao = new UsuarioDao(conexao);
            usuarioDao.atualizarPontos(USUARIO_TESTE, 99999, 99999);

            // dentro da transacao a mudanca aparece...
            assertEquals(99999, usuarioDao.buscarPorId(USUARIO_TESTE).getSaldoPontos());

            // ...simula uma falha no meio da operacao
            throw new IllegalStateException("falha simulada");
        } catch (IllegalStateException e) {
            conexao.rollback();
        } finally {
            conexao.close();
        }

        // depois do rollback, outra conexao ve os valores originais
        Usuario depois = lerUsuario();
        assertEquals(antes.getSaldoPontos(), depois.getSaldoPontos());
        assertEquals(antes.getPontosXp(), depois.getPontosXp());
    }

    private Usuario lerUsuario() throws Exception {
        UsuarioDao usuarioDao = new UsuarioDao();
        try {
            return usuarioDao.buscarPorId(USUARIO_TESTE);
        } finally {
            usuarioDao.fecharConexao();
        }
    }
}
