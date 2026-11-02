package br.com.fiap.resource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testa os endpoints de /usuarios, incluindo o login.
 * Apenas leituras e validacoes - nenhum teste grava no banco.
 */
@SpringBootTest
@AutoConfigureMockMvc
class UsuarioResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listarNaoExpoeASenha() throws Exception {
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].senha").isEmpty());
    }

    @Test
    void cadastrarSemSenhaRetorna400() throws Exception {
        String json = "{ \"nome\": \"Teste Sem Senha\", \"email\": \"sem.senha@teste.com\" }";

        mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Senha deve ter pelo menos 6 caracteres."));
    }

    @Test
    void cadastrarComCpfInvalidoRetorna400() throws Exception {
        String json = "{ \"nome\": \"Teste Cpf\", \"cpf\": \"123\", \"email\": \"cpf@teste.com\", \"senha\": \"123456\" }";

        mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("CPF invalido! Informe 11 digitos."));
    }

    @Test
    void loginSemSenhaRetorna400() throws Exception {
        mockMvc.perform(post("/usuarios/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"email\": \"alguem@teste.com\" }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginComEmailInexistenteRetorna401() throws Exception {
        mockMvc.perform(post("/usuarios/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"email\": \"nao.existe@teste.com\", \"senha\": \"qualquer\" }"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("E-mail ou senha invalidos."));
    }
}
