package br.com.fiap.entities;

import java.time.LocalDate;

/**
 * Mapeia TB_ACAO_REALIZADA.
 * E o coracao da validacao da SoulPass 2.0: a acao nasce PENDENTE e so
 * gera pontos depois de aprovada pelo codigo / QR Code.
 */
public class AcaoRealizada {

    private int idAcao;
    private int idUsuario;
    private int idMissao;
    private String metodoValidacao;
    private String codigoValidacao;
    private LocalDate dataConclusao;
    private String statusAprovacao;
    private double pontosConquistados;

    // Valores aceitos em METODO_VALIDACAO (VARCHAR2(20))
    public static final String METODO_CHECKIN = "CHECKIN";
    public static final String METODO_QRCODE = "QRCODE";
    public static final String METODO_CODIGO = "CODIGO";

    // Valores aceitos em STATUS_APROVACAO (VARCHAR2(20))
    public static final String STATUS_PENDENTE = "PENDENTE";
    public static final String STATUS_APROVADO = "APROVADO";
    public static final String STATUS_REJEITADO = "REJEITADO";

    public AcaoRealizada() {
        this.statusAprovacao = STATUS_PENDENTE;
        this.dataConclusao = LocalDate.now();
        this.pontosConquistados = 0;
    }

    public AcaoRealizada(int idAcao, int idUsuario, int idMissao, String metodoValidacao,
                         String codigoValidacao, LocalDate dataConclusao,
                         String statusAprovacao, double pontosConquistados) {
        this.idAcao = idAcao;
        this.idUsuario = idUsuario;
        this.idMissao = idMissao;
        this.metodoValidacao = metodoValidacao;
        this.codigoValidacao = codigoValidacao;
        this.dataConclusao = dataConclusao;
        this.statusAprovacao = statusAprovacao;
        this.pontosConquistados = pontosConquistados;
    }

    public int getIdAcao() {
        return idAcao;
    }

    public void setIdAcao(int idAcao) {
        this.idAcao = idAcao;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdMissao() {
        return idMissao;
    }

    public void setIdMissao(int idMissao) {
        this.idMissao = idMissao;
    }

    public String getMetodoValidacao() {
        return metodoValidacao;
    }

    public void setMetodoValidacao(String metodoValidacao) {
        this.metodoValidacao = metodoValidacao;
    }

    public String getCodigoValidacao() {
        return codigoValidacao;
    }

    public void setCodigoValidacao(String codigoValidacao) {
        this.codigoValidacao = codigoValidacao;
    }

    public LocalDate getDataConclusao() {
        return dataConclusao;
    }

    public void setDataConclusao(LocalDate dataConclusao) {
        this.dataConclusao = dataConclusao;
    }

    public String getStatusAprovacao() {
        return statusAprovacao;
    }

    public void setStatusAprovacao(String statusAprovacao) {
        this.statusAprovacao = statusAprovacao;
    }

    public double getPontosConquistados() {
        return pontosConquistados;
    }

    public void setPontosConquistados(double pontosConquistados) {
        this.pontosConquistados = pontosConquistados;
    }

    // gerarCodigoValidacao - codigo exibido pelo ponto parceiro
    public static String gerarCodigoValidacao(int idMissao, String metodo) {
        return metodo + "-" + idMissao + "-" + (System.currentTimeMillis() % 1000000);
    }

    // estaPendente - aguardando confirmacao
    public boolean estaPendente() {
        return STATUS_PENDENTE.equalsIgnoreCase(statusAprovacao);
    }

    // foiAprovada - ja validada e apta a gerar pontos
    public boolean foiAprovada() {
        return STATUS_APROVADO.equalsIgnoreCase(statusAprovacao);
    }

    // metodoValido - confere se o metodo gravado e um dos previstos
    public boolean metodoValido() {
        if (metodoValidacao == null) {
            return false;
        }

        String metodo = metodoValidacao.toUpperCase();

        return metodo.equals(METODO_CHECKIN) || metodo.equals(METODO_QRCODE)
                || metodo.equals(METODO_CODIGO);
    }

    // aprovar - valida o codigo informado e calcula os pontos da missao
    public String aprovar(String codigoInformado, Missao missao) {
        if (!estaPendente()) {
            return "Esta acao ja esta com status " + statusAprovacao + "!";
        }
        if (missao == null) {
            return "Missao nao informada!";
        }
        if (missao.getIdMissao() != this.idMissao) {
            return "A missao informada nao corresponde a esta acao!";
        }
        if (!missao.estaDisponivel()) {
            return "A missao '" + missao.getTituloMissao() + "' nao esta disponivel.";
        }
        if (!metodoValido()) {
            return "Metodo de validacao invalido: " + metodoValidacao;
        }
        if (codigoInformado == null || codigoInformado.isBlank()) {
            return "Codigo nao informado!";
        }
        if (!codigoInformado.trim().equalsIgnoreCase(this.codigoValidacao)) {
            this.statusAprovacao = STATUS_REJEITADO;
            return "Codigo invalido! Acao REJEITADA.";
        }

        this.statusAprovacao = STATUS_APROVADO;
        this.pontosConquistados = missao.calcularPontos();

        return "Acao APROVADA via " + metodoValidacao + "! Pontos: " + pontosConquistados;
    }

    // rejeitar - recusa manual da acao
    public String rejeitar(String motivo) {
        if (!estaPendente()) {
            return "Esta acao ja esta com status " + statusAprovacao + "!";
        }

        this.statusAprovacao = STATUS_REJEITADO;
        this.pontosConquistados = 0;

        return "Acao rejeitada. Motivo: " + motivo;
    }

    @Override
    public String toString() {
        return "\nAcaoRealizada " +
                "\nidAcao=" + idAcao +
                "\nidUsuario=" + idUsuario +
                "\nidMissao=" + idMissao +
                "\nmetodoValidacao='" + metodoValidacao + '\'' +
                "\ncodigoValidacao='" + codigoValidacao + '\'' +
                "\ndataConclusao=" + dataConclusao +
                "\nstatusAprovacao='" + statusAprovacao + '\'' +
                "\npontosConquistados=" + pontosConquistados;
    }
}
