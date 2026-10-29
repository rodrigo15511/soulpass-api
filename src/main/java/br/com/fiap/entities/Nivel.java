package br.com.fiap.entities;

/**
 * Mapeia TB_NIVEL.
 * Os niveis sao dados cadastrados no banco, nao constantes fixas no codigo.
 */
public class Nivel {

    private int idNivel;
    private String nomeNivel;
    private int nivelOrdem;
    private double pontosXpNecessarios;
    private String descricao;

    public Nivel() {
    }

    public Nivel(int idNivel, String nomeNivel, int nivelOrdem,
                 double pontosXpNecessarios, String descricao) {
        this.idNivel = idNivel;
        this.nomeNivel = nomeNivel;
        this.nivelOrdem = nivelOrdem;
        this.pontosXpNecessarios = pontosXpNecessarios;
        this.descricao = descricao;
    }

    public int getIdNivel() {
        return idNivel;
    }

    public void setIdNivel(int idNivel) {
        this.idNivel = idNivel;
    }

    public String getNomeNivel() {
        return nomeNivel;
    }

    public void setNomeNivel(String nomeNivel) {
        this.nomeNivel = nomeNivel;
    }

    public int getNivelOrdem() {
        return nivelOrdem;
    }

    public void setNivelOrdem(int nivelOrdem) {
        this.nivelOrdem = nivelOrdem;
    }

    public double getPontosXpNecessarios() {
        return pontosXpNecessarios;
    }

    public void setPontosXpNecessarios(double pontosXpNecessarios) {
        this.pontosXpNecessarios = pontosXpNecessarios;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    // atingidoPor - verifica se a pontuacao de XP alcanca este nivel
    public boolean atingidoPor(double pontosXp) {
        return pontosXp >= pontosXpNecessarios;
    }

    // pontosFaltantes - quanto falta de XP para alcancar este nivel
    public double pontosFaltantes(double pontosXp) {
        double faltam = pontosXpNecessarios - pontosXp;
        return faltam > 0 ? faltam : 0;
    }

    @Override
    public String toString() {
        return "\nNivel " +
                "\nidNivel=" + idNivel +
                "\nnomeNivel='" + nomeNivel + '\'' +
                "\nordem=" + nivelOrdem +
                "\npontosXpNecessarios=" + pontosXpNecessarios +
                "\ndescricao='" + descricao + '\'';
    }
}
