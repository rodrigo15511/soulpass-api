package br.com.fiap.resource;

import br.com.fiap.bo.UsuarioBO;
import br.com.fiap.entities.Usuario;
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
 * Endpoints REST de Usuario - base: /usuarios
 *
 * GET    /usuarios       -> lista todos           (200)
 * GET    /usuarios/{id}  -> busca um              (200 / 404)
 * POST   /usuarios       -> cadastra              (201 / 400 / 409)
 * PUT    /usuarios/{id}  -> atualiza              (200 / 400 / 404 / 409)
 * DELETE /usuarios/{id}  -> exclui                (204 / 404 / 409)
 * Erro inesperado (ex.: banco fora do ar)         (500)
 */
@RestController
@RequestMapping("/usuarios")
public class UsuarioResource {

    private final UsuarioBO usuarioBO = new UsuarioBO();

    @GetMapping
    public ResponseEntity<?> listar() {
        try {
            return ResponseEntity.ok(usuarioBO.selecionarBo());
        } catch (Exception e) {
            return erro(e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable int id) {
        try {
            Usuario usuario = usuarioBO.buscarPorIdBo(id);
            if (usuario == null) {
                return mensagem(HttpStatus.NOT_FOUND, "Usuario " + id + " nao encontrado.");
            }
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            return erro(e);
        }
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody Usuario usuario) {
        try {
            Usuario criado = usuarioBO.inserirBo(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(criado);
        } catch (Exception e) {
            return erro(e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable int id, @RequestBody Usuario usuario) {
        try {
            Usuario atualizado = usuarioBO.atualizarBo(id, usuario);
            if (atualizado == null) {
                return mensagem(HttpStatus.NOT_FOUND, "Usuario " + id + " nao encontrado.");
            }
            return ResponseEntity.ok(atualizado);
        } catch (Exception e) {
            return erro(e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable int id) {
        try {
            if (!usuarioBO.deletarBo(id)) {
                return mensagem(HttpStatus.NOT_FOUND, "Usuario " + id + " nao encontrado.");
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
            // CPF/e-mail repetido (UNIQUE) ou usuario com registros ligados (FK)
            return mensagem(HttpStatus.CONFLICT, "Operacao viola uma regra do banco: " + e.getMessage());
        }
        e.printStackTrace();
        return mensagem(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno: " + e.getMessage());
    }

    private ResponseEntity<Map<String, String>> mensagem(HttpStatus status, String texto) {
        return ResponseEntity.status(status).body(Map.of("mensagem", texto));
    }
}
