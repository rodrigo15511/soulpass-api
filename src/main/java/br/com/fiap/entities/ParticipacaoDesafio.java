package br.com.fiap.entities;

import java.time.LocalDate;

/**
 * Mapeia TB_PARTICIPACAO_DESAFIO.
 * Cada linha e a contribuicao de um usuario para um desafio coletivo.
 */
public class ParticipacaoDesafio {

    private int idParticipacao;
    private int idDesafio;
    private int idUsuario;
    private double pontosContribuidos;
    private LocalDate dataIngresso;

    public ParticipacaoDesafio() {
        this.dataIngresso = LocalDate.now();
        this.pontosContribuidos = 0;
    }

    public ParticipacaoDesafio(int idParticipacao, int idDesafio, int idUsuario,
                               double pontosContribuidos, LocalDate dataIngresso) {
        this.idParticipacao = idParticipacao;
        this.idDesafio = idDesafio;
        this.idUsuario = idUsuario;
        this.pontosContribuidos = pontosContribuidos;
        this.dataIngresso = dataIngresso;
    }

    public int getIdParticipacao() {
        return idParticipacao;
    }

    public void setIdParticipacao(int idParticipacao) {
        this.idParticipacao = idParticipacao;
    }

    public int getIdDesafio() {
        return idDesafio;
    }

    public void setIdDesafio(int idDesafio) {
        this.idDesafio = idDesafio;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public double getPontosContribuidos() {
        return pontosContribuidos;
    }

    public void setPontosContribuidos(double pontosContribuidos) {
        this.pontosContribuidos = pontosContribuidos;
    }

    public LocalDate getDataIngresso() {
        return dataIngresso;
    }

    public void setDataIngresso(LocalDate dataIngresso) {
        this.dataIngresso = dataIngresso;
    }

    // contribuir - soma pontos a contribuicao deste participante
    public String contribuir(double pontos) {
        if (pontos <= 0) {
            return "Quantidade invalida!";
        }

        this.pontosContribuidos = this.pontosContribuidos + pontos;

        return "Contribuicao registrada: +" + pontos + " pontos."
                + "\nTotal contribuido: " + pontosContribuidos;
    }

    // diasNoDesafio - tempo de participacao
    public long diasNoDesafio() {
        if (dataIngresso == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(dataIngresso, LocalDate.now());
    }

    @Override
    public String toString() {
        return "\nParticipacaoDesafio " +
                "\nidParticipacao=" + idParticipacao +
                "\nidDesafio=" + idDesafio +
                "\nidUsuario=" + idUsuario +
                "\npontosContribuidos=" + pontosContribuidos +
                "\ndataIngresso=" + dataIngresso;
    }
}
