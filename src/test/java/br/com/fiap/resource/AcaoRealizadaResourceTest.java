package br.com.fiap.resource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testa os endpoints de /acoes.
 * Apenas leituras e validacoes - nenhum teste grava no banco.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AcaoRealizadaResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listarTodasRetorna200ComLista() throws Exception {
        mockMvc.perform(get("/acoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void listarPendentesRetorna200ComLista() throws Exception {
        mockMvc.perform(get("/acoes/pendentes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void historicoDoUsuarioRetorna200ComLista() throws Exception {
        mockMvc.perform(get("/acoes/usuario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void buscarIdInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/acoes/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void registrarComMetodoInvalidoRetorna400() throws Exception {
        String json = "{ \"idUsuario\": 1, \"idMissao\": 1, \"metodoValidacao\": \"TELEPATIA\" }";

        mockMvc.perform(post("/acoes").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrarComUsuarioInexistenteRetorna400() throws Exception {
        String json = "{ \"idUsuario\": 999999, \"idMissao\": 1, \"metodoValidacao\": \"QRCODE\" }";

        mockMvc.perform(post("/acoes").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Usuario 999999 nao existe."));
    }

    @Test
    void aprovarAcaoInexistenteRetorna404() throws Exception {
        mockMvc.perform(put("/acoes/999999/aprovar")
                        .contentType(MediaType.APPLICATION_JSON).content("{ \"codigo\": \"X\" }"))
                .andExpect(status().isNotFound());
    }
}
