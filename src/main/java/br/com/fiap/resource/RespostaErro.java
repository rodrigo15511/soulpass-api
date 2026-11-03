package br.com.fiap.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.SQLException;
import java.util.Map;

/**
 * Respostas de erro padronizadas para todos os Resources: { "mensagem": "..." }.
 * Mensagens tecnicas do Oracle (ORA-xxxxx) NAO vao para o Front: elas so aparecem
 * no log do servidor (aba Logs do Render), onde a equipe consegue investigar.
 */
public final class RespostaErro {

    private static final Logger LOG = LoggerFactory.getLogger(RespostaErro.class);

    // codigos de erro do Oracle que viram mensagens amigaveis
    private static final int ORA_REGISTRO_DUPLICADO = 1;      // ORA-00001: UNIQUE violada
    private static final int ORA_CHECK_VIOLADO = 2290;        // ORA-02290: CHECK violada
    private static final int ORA_PAI_NAO_EXISTE = 2291;       // ORA-02291: FK aponta para registro inexistente
    private static final int ORA_TEM_FILHOS = 2292;           // ORA-02292: registro tem outros ligados a ele

    private RespostaErro() {
    }

    // erro - converte a excecao no status HTTP e na mensagem adequados
    public static ResponseEntity<Map<String, String>> erro(Exception e) {
        // nossas proprias regras de negocio (BO): a mensagem ja e amigavel
        if (e instanceof IllegalArgumentException) {
            return mensagem(HttpStatus.BAD_REQUEST, String.valueOf(e.getMessage()));
        }
        if (e instanceof IllegalStateException) {
            return mensagem(HttpStatus.CONFLICT, String.valueOf(e.getMessage()));
        }

        // regras do banco: traduz o codigo do Oracle e guarda o detalhe tecnico so no log
        if (e instanceof SQLException sql) {
            switch (sql.getErrorCode()) {
                case ORA_REGISTRO_DUPLICADO:
                    LOG.warn("Registro duplicado: {}", sql.getMessage());
                    return mensagem(HttpStatus.CONFLICT,
                            "Ja existe um cadastro com esses dados (ex.: e-mail ou CPF ja usado).");
                case ORA_TEM_FILHOS:
                    LOG.warn("Exclusao bloqueada por registros ligados: {}", sql.getMessage());
                    return mensagem(HttpStatus.CONFLICT,
                            "Nao e possivel excluir: existem outros registros ligados a este item.");
                case ORA_PAI_NAO_EXISTE:
                    LOG.warn("Referencia inexistente: {}", sql.getMessage());
                    return mensagem(HttpStatus.BAD_REQUEST,
                            "Um dos registros informados (usuario, missao ou voucher) nao existe.");
                case ORA_CHECK_VIOLADO:
                    LOG.warn("Valor fora das regras do banco: {}", sql.getMessage());
                    return mensagem(HttpStatus.BAD_REQUEST,
                            "Algum valor informado esta fora das regras permitidas.");
                default:
                    break;
            }
        }

        // qualquer outra coisa (banco fora do ar, bug): mensagem generica + detalhe completo no log
        LOG.error("Erro inesperado na API", e);
        return mensagem(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno no servidor. Tente novamente em instantes.");
    }

    public static ResponseEntity<Map<String, String>> mensagem(HttpStatus status, String texto) {
        return ResponseEntity.status(status).body(Map.of("mensagem", texto));
    }
}
