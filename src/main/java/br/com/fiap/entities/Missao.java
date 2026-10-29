package br.com.fiap.entities;

/**
 * Mapeia TB_MISSAO.
 * Observacao: a tabela nao possui periodo de vigencia - ele existe apenas
 * em TB_DESAFIO_COLETIVO. Aqui a disponibilidade depende so da flag ativa.
 */
public class Missao {

    private int idMissao;
    private String tituloMissao;
    private String tipoMissao;
    private String descricao;
    private double pontoBase;
    private boolean ativa;

    // Valores aceitos na coluna TIPO_MISSAO (VARCHAR2(30))
    public static final String TIPO_TRANSPORTE_PUBLICO = "TRANSPORTE_PUBLICO";
    public static final String TIPO_CAMINHADA = "CAMINHADA";
    public static final String TIPO_BICICLETA = "BICICLETA";
    public static final String TIPO_DESAFIO_SEMANAL = "DESAFIO_SEMANAL";
    public static final String TIPO_DESAFIO_COLETIVO = "DESAFIO_COLETIVO";

    public Missao() {
        this.ativa = true;
    }

    public Missao(int idMissao, String tituloMissao, String tipoMissao, String descricao,
                  double pontoBase, boolean ativa) {
        this.idMissao = idMissao;
        this.tituloMissao = tituloMissao;
        this.tipoMissao = tipoMissao;
        this.descricao = descricao;
        this.pontoBase = pontoBase;
        this.ativa = ativa;
    }

    public int getIdMissao() {
        return idMissao;
    }

    public void setIdMissao(int idMissao) {
        this.idMissao = idMissao;
    }

    public String getTituloMissao() {
        return tituloMissao;
    }

    public void setTituloMissao(String tituloMissao) {
        this.tituloMissao = tituloMissao;
    }

    public String getTipoMissao() {
        return tipoMissao;
    }

    public void setTipoMissao(String tipoMissao) {
        this.tipoMissao = tipoMissao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getPontoBase() {
        return pontoBase;
    }

    public void setPontoBase(double pontoBase) {
        this.pontoBase = pontoBase;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }

    // multiplicador - bonus por modalidade, premiando o menor impacto ambiental
    public double multiplicador() {
        if (tipoMissao == null) {
            return 1.0;
        }

        switch (tipoMissao.toUpperCase()) {
            case TIPO_CAMINHADA:
                return 1.2;
            case TIPO_BICICLETA:
                return 1.5;
            case TIPO_DESAFIO_SEMANAL:
                return 2.0;
            case TIPO_DESAFIO_COLETIVO:
                return 2.5;
            default:
                return 1.0;
        }
    }

    // calcularPontos - pontuacao final gravada em ACAO_REALIZADA.PONTOS_CONQUISTADOS
    public double calcularPontos() {
        return Math.round(pontoBase * multiplicador() * 100) / 100.0;
    }

    // tipoValido - garante que o tipo gravado e um dos previstos
    public boolean tipoValido() {
        if (tipoMissao == null) {
            return false;
        }

        String tipo = tipoMissao.toUpperCase();

        return tipo.equals(TIPO_TRANSPORTE_PUBLICO) || tipo.equals(TIPO_CAMINHADA)
                || tipo.equals(TIPO_BICICLETA) || tipo.equals(TIPO_DESAFIO_SEMANAL)
                || tipo.equals(TIPO_DESAFIO_COLETIVO);
    }

    // ehColetiva - missoes coletivas alimentam TB_DESAFIO_COLETIVO
    public boolean ehColetiva() {
        return TIPO_DESAFIO_COLETIVO.equalsIgnoreCase(tipoMissao);
    }

    // estaDisponivel - a missao precisa estar ativa e com tipo valido
    public boolean estaDisponivel() {
        return ativa && tipoValido();
    }

    @Override
    public String toString() {
        return "\nMissao " +
                "\nidMissao=" + idMissao +
                "\ntituloMissao='" + tituloMissao + '\'' +
                "\ntipoMissao='" + tipoMissao + '\'' +
                "\npontoBase=" + pontoBase +
                "\nmultiplicador=x" + multiplicador() +
                "\npontosComBonus=" + calcularPontos() +
                "\nativa=" + (ativa ? "Sim" : "Nao");
    }
}
