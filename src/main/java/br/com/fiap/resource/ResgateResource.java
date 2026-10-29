package br.com.fiap.resource;

import br.com.fiap.bo.ResgateBO;
import br.com.fiap.entities.Resgate;
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
 * Endpoints REST de Resgate - base: /resgates
 *
 * GET    /resgates                         -> lista todos                       (200)
 * GET    /resgates/usuario/{idUsuario}     -> vouchers de um usuario            (200)
 * GET    /resgates/codigo/{codigo}         -> busca pelo codigo do voucher      (200 / 404)
 * GET    /resgates/{id}                    -> busca um                          (200 / 404)
 * POST   /resgates                         -> troca pontos por voucher          (201 / 400 / 409)
 * PUT    /resgates/codigo/{codigo}/utilizar -> usa o voucher                    (200 / 404 / 409)
 * PUT    /resgates/expirar                 -> marca os vencidos como EXPIRADO   (200)
 * DELETE /resgates/{id}                    -> cancela e devolve os pontos       (204 / 404 / 409)
 * Erro inesperado (ex.: banco fora do ar)                                       (500)
 */
@RestController
@RequestMapping("/resgates")
public class ResgateResource {

    private final ResgateBO resgateBO = new ResgateBO();

    @GetMapping
    public ResponseEntity<?> listar() {
        try {
            return ResponseEntity.ok(resgateBO.selecionarBo());
        } catch (Exception e) {
            return erro(e);
        }
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<?> listarPorUsuario(@PathVariable int idUsuario) {
        try {
            return ResponseEntity.ok(resgateBO.buscarPorUsuarioBo(idUsuario));
        } catch (Exception e) {
            return erro(e);
        }
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<?> buscarPorCodigo(@PathVariable String codigo) {
        try {
            Resgate resgate = resgateBO.buscarPorCodigoBo(codigo);
            if (resgate == null) {
                return mensagem(HttpStatus.NOT_FOUND, "Voucher " + codigo + " nao encontrado.");
            }
            return ResponseEntity.ok(resgate);
        } catch (Exception e) {
            return erro(e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable int id) {
        try {
            Resgate resgate = resgateBO.buscarPorIdBo(id);
            if (resgate == null) {
                return mensagem(HttpStatus.NOT_FOUND, "Resgate " + id + " nao encontrado.");
            }
            return ResponseEntity.ok(resgate);
        } catch (Exception e) {
            return erro(e);
        }
    }

    // Corpo esperado: { "idUsuario": 1, "idVoucher": 2 }
    @PostMapping
    public ResponseEntity<?> resgatar(@RequestBody Resgate pedido) {
        try {
            Resgate criado = resgateBO.resgatarBo(pedido.getIdUsuario(), pedido.getIdVoucher());
            return ResponseEntity.status(HttpStatus.CREATED).body(criado);
        } catch (Exception e) {
            return erro(e);
        }
    }

    @PutMapping("/codigo/{codigo}/utilizar")
    public ResponseEntity<?> utilizar(@PathVariable String codigo) {
        try {
            Resgate resgate = resgateBO.utilizarBo(codigo);
            if (resgate == null) {
                return mensagem(HttpStatus.NOT_FOUND, "Voucher " + codigo + " nao encontrado.");
            }
            return ResponseEntity.ok(resgate);
        } catch (Exception e) {
            return erro(e);
        }
    }

    @PutMapping("/expirar")
    public ResponseEntity<?> expirarVencidos() {
        try {
            return mensagem(HttpStatus.OK, resgateBO.expirarVencidosBo());
        } catch (Exception e) {
            return erro(e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelar(@PathVariable int id) {
        try {
            if (!resgateBO.cancelarBo(id)) {
                return mensagem(HttpStatus.NOT_FOUND, "Resgate " + id + " nao encontrado.");
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
            // a requisicao e valida, mas o estado atual nao permite (ex.: saldo insuficiente)
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
