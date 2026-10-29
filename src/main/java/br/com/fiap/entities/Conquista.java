package br.com.fiap.entities;

/**
 * Mapeia TB_CONQUISTA.
 * Catalogo de conquistas. A coluna CRITERIO guarda a regra em texto,
 * no formato CAMPO:OPERADOR:VALOR (ex.: "XP:>=:1000").
 */
public class Conquista {

    private int idConquista;
    private String nomeConquista;
    private String descricao;
    private String criterio;
    private double pontosBonus;

    // Campos aceitos no criterio
    public static final String CRITERIO_XP = "XP";
    public static final String CRITERIO_SALDO = "SALDO";
    public static final String CRITERIO_NIVEL = "NIVEL";

    public Conquista() {
    }

    public Conquista(int idConquista, String nomeConquista, String descricao,
                     String criterio, double pontosBonus) {
        this.idConquista = idConquista;
        this.nomeConquista = nomeConquista;
        this.descricao = descricao;
        this.criterio = criterio;
        this.pontosBonus = pontosBonus;
    }

    public int getIdConquista() {
        return idConquista;
    }

    public void setIdConquista(int idConquista) {
        this.idConquista = idConquista;
    }

    public String getNomeConquista() {
        return nomeConquista;
    }

    public void setNomeConquista(String nomeConquista) {
        this.nomeConquista = nomeConquista;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCriterio() {
        return criterio;
    }

    public void setCriterio(String criterio) {
        this.criterio = criterio;
    }

    public double getPontosBonus() {
        return pontosBonus;
    }

    public void setPontosBonus(double pontosBonus) {
        this.pontosBonus = pontosBonus;
    }

    // criterioValido - o texto precisa estar no formato CAMPO:OPERADOR:VALOR
    public boolean criterioValido() {
        if (criterio == null) {
            return false;
        }

        String[] partes = criterio.split(":");

        if (partes.length != 3) {
            return false;
        }

        try {
            Double.parseDouble(partes[2]);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    // atendidaPor - avalia o criterio contra os dados do usuario
    public boolean atendidaPor(Usuario usuario) {
        if (usuario == null || !criterioValido()) {
            return false;
        }

        String[] partes = criterio.split(":");
        String campo = partes[0].toUpperCase();
        String operador = partes[1];
        double alvo = Double.parseDouble(partes[2]);

        double valorUsuario;

        switch (campo) {
            case CRITERIO_XP:
                valorUsuario = usuario.getPontosXp();
                break;
            case CRITERIO_SALDO:
                valorUsuario = usuario.getSaldoPontos();
                break;
            case CRITERIO_NIVEL:
                valorUsuario = usuario.getNivel() != null ? usuario.getNivel().getNivelOrdem() : 0;
                break;
            default:
                return false;
        }

        switch (operador) {
            case ">=":
                return valorUsuario >= alvo;
            case ">":
                return valorUsuario > alvo;
            case "=":
                return valorUsuario == alvo;
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return "\nConquista " +
                "\nidConquista=" + idConquista +
                "\nnomeConquista='" + nomeConquista + '\'' +
                "\ndescricao='" + descricao + '\'' +
                "\ncriterio='" + criterio + '\'' +
                "\npontosBonus=" + pontosBonus;
    }
}
