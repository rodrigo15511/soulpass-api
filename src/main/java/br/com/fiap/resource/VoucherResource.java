package br.com.fiap.resource;

import br.com.fiap.bo.VoucherBO;
import br.com.fiap.entities.Voucher;
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
 * Endpoints REST de Voucher (catalogo) - base: /vouchers
 *
 * GET    /vouchers       -> lista o catalogo     (200)
 * GET    /vouchers/{id}  -> busca um             (200 / 404)
 * POST   /vouchers       -> cadastra             (201 / 400)
 * PUT    /vouchers/{id}  -> atualiza             (200 / 400 / 404)
 * DELETE /vouchers/{id}  -> exclui               (204 / 404 / 409)
 * Erro inesperado (ex.: banco fora do ar)        (500)
 */
@RestController
@RequestMapping("/vouchers")
public class VoucherResource {

    private final VoucherBO voucherBO = new VoucherBO();

    @GetMapping
    public ResponseEntity<?> listar() {
        try {
            return ResponseEntity.ok(voucherBO.selecionarBo());
        } catch (Exception e) {
            return erro(e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable int id) {
        try {
            Voucher voucher = voucherBO.buscarPorIdBo(id);
            if (voucher == null) {
                return mensagem(HttpStatus.NOT_FOUND, "Voucher " + id + " nao encontrado.");
            }
            return ResponseEntity.ok(voucher);
        } catch (Exception e) {
            return erro(e);
        }
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody Voucher voucher) {
        try {
            Voucher criado = voucherBO.inserirBo(voucher);
            return ResponseEntity.status(HttpStatus.CREATED).body(criado);
        } catch (Exception e) {
            return erro(e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable int id, @RequestBody Voucher voucher) {
        try {
            Voucher atualizado = voucherBO.atualizarBo(id, voucher);
            if (atualizado == null) {
                return mensagem(HttpStatus.NOT_FOUND, "Voucher " + id + " nao encontrado.");
            }
            return ResponseEntity.ok(atualizado);
        } catch (Exception e) {
            return erro(e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable int id) {
        try {
            if (!voucherBO.deletarBo(id)) {
                return mensagem(HttpStatus.NOT_FOUND, "Voucher " + id + " nao encontrado.");
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
            // voucher que ja foi resgatado por alguem (FK) nao pode ser excluido
            return mensagem(HttpStatus.CONFLICT, "Voucher possui resgates ligados: " + e.getMessage());
        }
        e.printStackTrace();
        return mensagem(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno: " + e.getMessage());
    }

    private ResponseEntity<Map<String, String>> mensagem(HttpStatus status, String texto) {
        return ResponseEntity.status(status).body(Map.of("mensagem", texto));
    }
}
