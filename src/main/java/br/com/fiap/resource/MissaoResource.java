package br.com.fiap.resource;

import br.com.fiap.bo.MissaoBO;
import br.com.fiap.entities.Missao;
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
 * Endpoints REST de Missao - base: /missoes
 *
 * GET    /missoes              -> lista todas                 (200)
 * GET    /missoes/ativas       -> lista so as ativas          (200)
 * GET    /missoes/tipo/{tipo}  -> ativas de um tipo           (200)
 * GET    /missoes/{id}         -> busca uma                   (200 / 404)
 * POST   /missoes              -> cadastra                    (201 / 400)
 * PUT    /missoes/{id}         -> atualiza                    (200 / 400 / 404)
 * DELETE /missoes/{id}         -> exclui                      (204 / 404 / 409)
 * Erro inesperado (ex.: banco fora do ar)                     (500)
 */
@RestController
@RequestMapping("/missoes")
public class MissaoResource {

    private final MissaoBO missaoBO = new MissaoBO();

    @GetMapping
    public ResponseEntity<?> listar() {
        try {
            return ResponseEntity.ok(missaoBO.selecionarBo());
        } catch (Exception e) {
            return erro(e);
        }
    }

    @GetMapping("/ativas")
    public ResponseEntity<?> listarAtivas() {
        try {
            return ResponseEntity.ok(missaoBO.selecionarAtivasBo());
        } catch (Exception e) {
            return erro(e);
        }
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<?> listarPorTipo(@PathVariable String tipo) {
        try {
            return ResponseEntity.ok(missaoBO.buscarPorTipoBo(tipo));
        } catch (Exception e) {
            return erro(e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable int id) {
        try {
            Missao missao = missaoBO.buscarPorIdBo(id);
            if (missao == null) {
                return mensagem(HttpStatus.NOT_FOUND, "Missao " + id + " nao encontrada.");
            }
            return ResponseEntity.ok(missao);
        } catch (Exception e) {
            return erro(e);
        }
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody Missao missao) {
        try {
            Missao criada = missaoBO.inserirBo(missao);
            return ResponseEntity.status(HttpStatus.CREATED).body(criada);
        } catch (Exception e) {
            return erro(e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable int id, @RequestBody Missao missao) {
        try {
            Missao atualizada = missaoBO.atualizarBo(id, missao);
            if (atualizada == null) {
                return mensagem(HttpStatus.NOT_FOUND, "Missao " + id + " nao encontrada.");
            }
            return ResponseEntity.ok(atualizada);
        } catch (Exception e) {
            return erro(e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable int id) {
        try {
            if (!missaoBO.deletarBo(id)) {
                return mensagem(HttpStatus.NOT_FOUND, "Missao " + id + " nao encontrada.");
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
        if (e instanceof SQLIntegrityConstraintViolationException) {
            // missao com acoes realizadas ligadas a ela (FK) nao pode ser excluida
            return mensagem(HttpStatus.CONFLICT, "Missao possui registros ligados: " + e.getMessage());
        }
        e.printStackTrace();
        return mensagem(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno: " + e.getMessage());
    }

    private ResponseEntity<Map<String, String>> mensagem(HttpStatus status, String texto) {
        return ResponseEntity.status(status).body(Map.of("mensagem", texto));
    }
}
