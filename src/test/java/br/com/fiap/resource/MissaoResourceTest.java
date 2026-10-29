package br.com.fiap.resource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testa os endpoints de /missoes sem precisar do Postman.
 * O MockMvc simula as requisicoes HTTP dentro do proprio Java.
 * Apenas leituras e validacoes - nenhum teste grava no banco.
 */
@SpringBootTest
@AutoConfigureMockMvc
class MissaoResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listarTodasRetorna200ComLista() throws Exception {
        mockMvc.perform(get("/missoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void listarAtivasRetorna200ComLista() throws Exception {
        mockMvc.perform(get("/missoes/ativas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void listarPorTipoAceitaMinusculas() throws Exception {
        mockMvc.perform(get("/missoes/tipo/bicicleta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void buscarIdInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/missoes/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Missao 999999 nao encontrada."));
    }

    @Test
    void cadastrarTipoInvalidoRetorna400() throws Exception {
        String json = "{ \"tituloMissao\": \"Teste\", \"tipoMissao\": \"AVIAO\", "
                + "\"descricao\": \"x\", \"pontoBase\": 50, \"ativa\": true }";

        mockMvc.perform(post("/missoes").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cadastrarPontoBaseZeroRetorna400() throws Exception {
        String json = "{ \"tituloMissao\": \"Teste\", \"tipoMissao\": \"BICICLETA\", "
                + "\"descricao\": \"x\", \"pontoBase\": 0, \"ativa\": true }";

        mockMvc.perform(post("/missoes").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Ponto base deve ser maior que zero."));
    }

    @Test
    void corsLiberadoParaOFront() throws Exception {
        mockMvc.perform(get("/missoes").header("Origin", "https://soulpass.vercel.app"))
                .andExpect(header().string("Access-Control-Allow-Origin", "*"));
    }
}
