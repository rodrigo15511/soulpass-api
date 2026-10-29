package br.com.fiap.entities;

/**
 * Mapeia TB_VOUCHER.
 * Atencao: esta tabela e o CATALOGO de beneficios disponiveis - nao pertence
 * a nenhum usuario. O voucher resgatado por alguem e a classe Resgate.
 */
public class Voucher {

    private int idVoucher;
    private String tipoTransporte;
    private double custoPontos;
    private double valorMonetario;
    private String descricao;

    // Valores aceitos em TIPO_TRANSPORTE - CHECK IN (ONIBUS, METRO, TREM, BICICLETA, APLICATIVO)
    public static final String TIPO_ONIBUS = "ONIBUS";
    public static final String TIPO_METRO = "METRO";
    public static final String TIPO_TREM = "TREM";
    public static final String TIPO_BICICLETA = "BICICLETA";
    public static final String TIPO_APLICATIVO = "APLICATIVO";

    public Voucher() {
    }

    public Voucher(int idVoucher, String tipoTransporte, double custoPontos,
                   double valorMonetario, String descricao) {
        this.idVoucher = idVoucher;
        this.tipoTransporte = tipoTransporte;
        this.custoPontos = custoPontos;
        this.valorMonetario = valorMonetario;
        this.descricao = descricao;
    }

    public int getIdVoucher() {
        return idVoucher;
    }

    public void setIdVoucher(int idVoucher) {
        this.idVoucher = idVoucher;
    }

    public String getTipoTransporte() {
        return tipoTransporte;
    }

    public void setTipoTransporte(String tipoTransporte) {
        this.tipoTransporte = tipoTransporte;
    }

    public double getCustoPontos() {
        return custoPontos;
    }

    public void setCustoPontos(double custoPontos) {
        this.custoPontos = custoPontos;
    }

    public double getValorMonetario() {
        return valorMonetario;
    }

    public void setValorMonetario(double valorMonetario) {
        this.valorMonetario = valorMonetario;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    // taxaConversao - quantos reais cada ponto vale neste beneficio
    public double taxaConversao() {
        if (custoPontos <= 0) {
            return 0;
        }
        return Math.round((valorMonetario / custoPontos) * 10000) / 10000.0;
    }

    // custoBeneficio - compara beneficios: quanto de real por 100 pontos
    public double custoBeneficio() {
        return Math.round(taxaConversao() * 100 * 100) / 100.0;
    }

    // tipoValido - confere se o tipo respeita o CHECK do banco
    public boolean tipoValido() {
        if (tipoTransporte == null) {
            return false;
        }

        String tipo = tipoTransporte.toUpperCase();

        return tipo.equals(TIPO_ONIBUS) || tipo.equals(TIPO_METRO) || tipo.equals(TIPO_TREM)
                || tipo.equals(TIPO_BICICLETA) || tipo.equals(TIPO_APLICATIVO);
    }

    // acessivelPara - o usuario tem saldo para este beneficio
    public boolean acessivelPara(Usuario usuario) {
        return usuario != null && usuario.isAtivo() && usuario.getSaldoPontos() >= custoPontos;
    }

    @Override
    public String toString() {
        return "\nVoucher (catalogo) " +
                "\nidVoucher=" + idVoucher +
                "\ntipoTransporte='" + tipoTransporte + '\'' +
                "\ncustoPontos=" + custoPontos +
                "\nvalorMonetario=R$ " + valorMonetario +
                "\nvalorPor100Pontos=R$ " + custoBeneficio() +
                "\ndescricao='" + descricao + '\'';
    }
}
