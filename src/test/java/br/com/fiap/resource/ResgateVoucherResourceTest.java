package br.com.fiap.resource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testa os endpoints de /vouchers e /resgates.
 * Apenas leituras e validacoes - nenhum teste grava no banco.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ResgateVoucherResourceTest {

    @Autowired
    private MockMvc mockMvc;

    // ---------- /vouchers ----------

    @Test
    void listarVouchersRetorna200ComLista() throws Exception {
        mockMvc.perform(get("/vouchers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void buscarVoucherInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/vouchers/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cadastrarVoucherComTipoInvalidoRetorna400() throws Exception {
        String json = "{ \"tipoTransporte\": \"FOGUETE\", \"custoPontos\": 100, \"valorMonetario\": 5 }";

        mockMvc.perform(post("/vouchers").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cadastrarVoucherComCustoZeroRetorna400() throws Exception {
        String json = "{ \"tipoTransporte\": \"METRO\", \"custoPontos\": 0, \"valorMonetario\": 5 }";

        mockMvc.perform(post("/vouchers").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Custo em pontos deve ser maior que zero."));
    }

    // ---------- /resgates ----------

    @Test
    void listarResgatesRetorna200ComLista() throws Exception {
        mockMvc.perform(get("/resgates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void resgatesDoUsuarioRetorna200ComLista() throws Exception {
        mockMvc.perform(get("/resgates/usuario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void buscarCodigoInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/resgates/codigo/NAO-EXISTE"))
                .andExpect(status().isNotFound());
    }

    @Test
    void resgatarComUsuarioInexistenteRetorna400() throws Exception {
        String json = "{ \"idUsuario\": 999999, \"idVoucher\": 1 }";

        mockMvc.perform(post("/resgates").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Usuario 999999 nao existe."));
    }

    @Test
    void utilizarCodigoInexistenteRetorna404() throws Exception {
        mockMvc.perform(put("/resgates/codigo/NAO-EXISTE/utilizar"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelarResgateInexistenteRetorna404() throws Exception {
        mockMvc.perform(delete("/resgates/999999"))
                .andExpect(status().isNotFound());
    }
}
