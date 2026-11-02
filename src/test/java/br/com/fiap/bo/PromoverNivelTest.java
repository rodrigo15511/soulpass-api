package br.com.fiap.bo;

import br.com.fiap.entities.Nivel;
import br.com.fiap.entities.Usuario;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste unitario da subida de nivel - nao usa banco nem Spring.
 * Mesmos niveis cadastrados em TB_NIVEL.
 */
class PromoverNivelTest {

    private static final Nivel INICIANTE = new Nivel(1, "Iniciante", 1, 0, "");
    private static final Nivel EXPLORADOR = new Nivel(2, "Explorador", 2, 500, "");
    private static final Nivel MOBILIZADOR = new Nivel(3, "Mobilizador", 3, 1500, "");
    private static final Nivel AGENTE = new Nivel(4, "Agente Sustentavel", 4, 3000, "");
    private static final List<Nivel> NIVEIS = List.of(INICIANTE, EXPLORADOR, MOBILIZADOR, AGENTE);

    private Usuario usuario(double xp, Nivel nivelAtual) {
        Usuario usuario = new Usuario();
        usuario.setPontosXp(xp);
        usuario.setNivel(nivelAtual);
        return usuario;
    }

    @Test
    void abaixoDe500ContinuaIniciante() {
        Usuario u = usuario(480, INICIANTE);
        assertFalse(AcaoRealizadaBO.promoverNivel(u, NIVEIS));
        assertEquals(1, u.getNivel().getIdNivel());
    }

    @Test
    void ao500SobeParaExplorador() {
        Usuario u = usuario(500, INICIANTE);
        assertTrue(AcaoRealizadaBO.promoverNivel(u, NIVEIS));
        assertEquals(2, u.getNivel().getIdNivel());
    }

    @Test
    void pulaDoisNiveisDeUmaVez() {
        Usuario u = usuario(1600, INICIANTE);
        assertTrue(AcaoRealizadaBO.promoverNivel(u, NIVEIS));
        assertEquals(3, u.getNivel().getIdNivel());
    }

    @Test
    void jaNoNivelCertoNaoMuda() {
        Usuario u = usuario(630, EXPLORADOR);
        assertFalse(AcaoRealizadaBO.promoverNivel(u, NIVEIS));
        assertEquals(2, u.getNivel().getIdNivel());
    }

    @Test
    void nivelMaximo() {
        Usuario u = usuario(3000, MOBILIZADOR);
        assertTrue(AcaoRealizadaBO.promoverNivel(u, NIVEIS));
        assertEquals(4, u.getNivel().getIdNivel());
    }
}
