package br.com.fiap.resource;

import br.com.fiap.bo.AcaoRealizadaBO;
import br.com.fiap.entities.AcaoRealizada;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Map;

/**
 * Endpoints REST de Acao Realizada - base: /acoes
 *
 * GET    /acoes                      -> lista todas                          (200)
 * GET    /acoes/pendentes            -> aguardando validacao                 (200)
 * GET    /acoes/usuario/{idUsuario}  -> historico do usuario                 (200)
 * GET    /acoes/{id}                 -> busca uma                            (200 / 404)
 * POST   /acoes                      -> registra (nasce PENDENTE)            (201 / 400 / 409)
 * PUT    /acoes/{id}/aprovar         -> valida o codigo e credita os pontos  (200 / 400 / 404 / 409)
 * PUT    /acoes/{id}/rejeitar        -> recusa a acao                        (200 / 404 / 409)
 * DELETE /acoes/{id}                 -> exclui (so se nao aprovada)          (204 / 404 / 409)
 * Erro inesperado (ex.: banco fora do ar)                                    (500)
 */
@RestController
@RequestMapping("/acoes")
public class AcaoRealizadaResource {

    private final AcaoRealizadaBO acaoBO = new AcaoRealizadaBO();

    @GetMapping
    public ResponseEntity<?> listar() {
        try {
            return ResponseEntity.ok(acaoBO.selecionarBo());
        } catch (Exception e) {
            return erro(e);
        }
    }

    @GetMapping("/pendentes")
    public ResponseEntity<?> listarPendentes() {
        try {
            return ResponseEntity.ok(acaoBO.selecionarPendentesBo());
        } catch (Exception e) {
            return erro(e);
        }
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<?> listarPorUsuario(@PathVariable int idUsuario) {
        try {
            return ResponseEntity.ok(acaoBO.buscarPorUsuarioBo(idUsuario));
        } catch (Exception e) {
            return erro(e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable int id) {
        try {
            AcaoRealizada acao = acaoBO.buscarPorIdBo(id);
            if (acao == null) {
                return mensagem(HttpStatus.NOT_FOUND, "Acao " + id + " nao encontrada.");
            }
            return ResponseEntity.ok(acao);
        } catch (Exception e) {
            return erro(e);
        }
    }

    // Corpo esperado: { "idUsuario": 1, "idMissao": 2, "metodoValidacao": "QRCODE" }
    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody AcaoRealizada acao) {
        try {
            AcaoRealizada criada = acaoBO.registrarBo(acao);
            return ResponseEntity.status(HttpStatus.CREATED).body(criada);
        } catch (Exception e) {
            return erro(e);
        }
    }

    // Corpo esperado: { "codigo": "QRCODE-2-123456" }
    @PutMapping("/{id}/aprovar")
    public ResponseEntity<?> aprovar(@PathVariable int id, @RequestBody Map<String, String> corpo) {
        try {
            AcaoRealizada acao = acaoBO.aprovarBo(id, corpo.get("codigo"));
            if (acao == null) {
                return mensagem(HttpStatus.NOT_FOUND, "Acao " + id + " nao encontrada.");
            }
            return ResponseEntity.ok(acao);
        } catch (Exception e) {
            return erro(e);
        }
    }

    // Corpo opcional: { "motivo": "Foto ilegivel" }
    @PutMapping("/{id}/rejeitar")
    public ResponseEntity<?> rejeitar(@PathVariable int id, @RequestBody(required = false) Map<String, String> corpo) {
        try {
            String motivo = corpo != null ? corpo.get("motivo") : "Nao informado";
            AcaoRealizada acao = acaoBO.rejeitarBo(id, motivo);
            if (acao == null) {
                return mensagem(HttpStatus.NOT_FOUND, "Acao " + id + " nao encontrada.");
            }
            return ResponseEntity.ok(acao);
        } catch (Exception e) {
            return erro(e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable int id) {
        try {
            if (!acaoBO.deletarBo(id)) {
                return mensagem(HttpStatus.NOT_FOUND, "Acao " + id + " nao encontrada.");
            }
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return erro(e);
        }
    }

    // erro - converte a excecao no status HTTP adequado
    private ResponseEntity<Map<String, String>> erro(Exception e) {
        if (e instanceof IllegalArgumentException) {
            return mensagem(HttpStatus.BAD_REQUEST, String.valueOf(e.getMessage()));
        }
        if (e instanceof IllegalStateException) {
            // a requisicao e valida, mas o estado atual nao permite (ex.: acao ja aprovada)
            return mensagem(HttpStatus.CONFLICT, String.valueOf(e.getMessage()));
        }
        if (e instanceof SQLIntegrityConstraintViolationException) {
            return mensagem(HttpStatus.CONFLICT, "Operacao viola uma regra do banco: " + e.getMessage());
        }
        e.printStackTrace();
        return mensagem(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno: " + e.getMessage());
    }

    private ResponseEntity<Map<String, String>> mensagem(HttpStatus status, String texto) {
        return ResponseEntity.status(status).body(Map.of("mensagem", texto));
    }
}
