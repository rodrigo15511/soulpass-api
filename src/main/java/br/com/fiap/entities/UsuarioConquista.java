package br.com.fiap.entities;

import java.time.LocalDate;

/**
 * Mapeia TB_USUARIO_CONQUISTA.
 * Registra que um usuario desbloqueou uma conquista.
 */
public class UsuarioConquista {

    private int idUsuarioConquista;
    private int idUsuario;
    private int idConquista;
    private LocalDate dataConquista;

    public UsuarioConquista() {
        this.dataConquista = LocalDate.now();
    }

    public UsuarioConquista(int idUsuarioConquista, int idUsuario, int idConquista,
                            LocalDate dataConquista) {
        this.idUsuarioConquista = idUsuarioConquista;
        this.idUsuario = idUsuario;
        this.idConquista = idConquista;
        this.dataConquista = dataConquista;
    }

    public int getIdUsuarioConquista() {
        return idUsuarioConquista;
    }

    public void setIdUsuarioConquista(int idUsuarioConquista) {
        this.idUsuarioConquista = idUsuarioConquista;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdConquista() {
        return idConquista;
    }

    public void setIdConquista(int idConquista) {
        this.idConquista = idConquista;
    }

    public LocalDate getDataConquista() {
        return dataConquista;
    }

    public void setDataConquista(LocalDate dataConquista) {
        this.dataConquista = dataConquista;
    }

    @Override
    public String toString() {
        return "\nUsuarioConquista " +
                "\nidUsuarioConquista=" + idUsuarioConquista +
                "\nidUsuario=" + idUsuario +
                "\nidConquista=" + idConquista +
                "\ndataConquista=" + dataConquista;
    }
}
