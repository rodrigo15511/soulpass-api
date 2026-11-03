package br.com.fiap.resource;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Teste unitario das respostas de erro: o texto tecnico do Oracle nunca chega ao Front.
 */
class RespostaErroTest {

    private static final String TEXTO_ORACLE = "ORA-00001: unique constraint (RM571840.UK_TB_USUARIO_EMAIL) violated";

    @Test
    void registroDuplicadoVira409SemTextoDoOracle() {
        ResponseEntity<Map<String, String>> r =
                RespostaErro.erro(new SQLIntegrityConstraintViolationException(TEXTO_ORACLE, "23000", 1));

        assertEquals(409, r.getStatusCode().value());
        assertFalse(r.getBody().get("mensagem").contains("ORA-"));
        assertFalse(r.getBody().get("mensagem").contains("RM571840"));
    }

    @Test
    void exclusaoComRegistrosLigadosVira409() {
        ResponseEntity<Map<String, String>> r =
                RespostaErro.erro(new SQLIntegrityConstraintViolationException("ORA-02292: ...", "23000", 2292));

        assertEquals(409, r.getStatusCode().value());
        assertEquals("Nao e possivel excluir: existem outros registros ligados a este item.",
                r.getBody().get("mensagem"));
    }

    @Test
    void erroDesconhecidoVira500Generico() {
        ResponseEntity<Map<String, String>> r =
                RespostaErro.erro(new SQLException("IO Error: The Network Adapter could not establish the connection"));

        assertEquals(500, r.getStatusCode().value());
        assertEquals("Erro interno no servidor. Tente novamente em instantes.", r.getBody().get("mensagem"));
    }

    @Test
    void regraDeNegocioMantemAMensagemDoBo() {
        ResponseEntity<Map<String, String>> r =
                RespostaErro.erro(new IllegalStateException("Saldo insuficiente para este voucher."));

        assertEquals(409, r.getStatusCode().value());
        assertEquals("Saldo insuficiente para este voucher.", r.getBody().get("mensagem"));
    }
}
