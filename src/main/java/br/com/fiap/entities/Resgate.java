package br.com.fiap.entities;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Mapeia TB_RESGATE.
 * E o voucher efetivamente resgatado por um usuario: tem codigo unico,
 * validade e status proprios.
 */
public class Resgate {

    private int idResgate;
    private int idUsuario;
    private int idVoucher;
    private int idAcao;
    private String codigoGerado;
    private LocalDate dataResgate;
    private LocalDate dataValidade;
    private String statusVoucher;

    // Valores aceitos em STATUS_VOUCHER - CHECK IN (DISPONIVEL, UTILIZADO, EXPIRADO)
    public static final String STATUS_DISPONIVEL = "DISPONIVEL";
    public static final String STATUS_UTILIZADO = "UTILIZADO";
    public static final String STATUS_EXPIRADO = "EXPIRADO";

    // Regra de negocio: validade padrao de 90 dias
    public static final int DIAS_VALIDADE = 90;

    public Resgate() {
        this.statusVoucher = STATUS_DISPONIVEL;
        this.dataResgate = LocalDate.now();
        this.dataValidade = LocalDate.now().plusDays(DIAS_VALIDADE);
    }

    public Resgate(int idResgate, int idUsuario, int idVoucher, int idAcao, String codigoGerado,
                   LocalDate dataResgate, LocalDate dataValidade, String statusVoucher) {
        this.idResgate = idResgate;
        this.idUsuario = idUsuario;
        this.idVoucher = idVoucher;
        this.idAcao = idAcao;
        this.codigoGerado = codigoGerado;
        this.dataResgate = dataResgate;
        this.dataValidade = dataValidade;
        this.statusVoucher = statusVoucher;
    }

    public int getIdResgate() {
        return idResgate;
    }

    public void setIdResgate(int idResgate) {
        this.idResgate = idResgate;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdVoucher() {
        return idVoucher;
    }

    public void setIdVoucher(int idVoucher) {
        this.idVoucher = idVoucher;
    }

    public int getIdAcao() {
        return idAcao;
    }

    public void setIdAcao(int idAcao) {
        this.idAcao = idAcao;
    }

    public String getCodigoGerado() {
        return codigoGerado;
    }

    public void setCodigoGerado(String codigoGerado) {
        this.codigoGerado = codigoGerado;
    }

    public LocalDate getDataResgate() {
        return dataResgate;
    }

    public void setDataResgate(LocalDate dataResgate) {
        this.dataResgate = dataResgate;
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(LocalDate dataValidade) {
        this.dataValidade = dataValidade;
    }

    public String getStatusVoucher() {
        return statusVoucher;
    }

    public void setStatusVoucher(String statusVoucher) {
        this.statusVoucher = statusVoucher;
    }

    // gerarCodigo - codigo unico do resgate (coluna UNIQUE no banco)
    public static String gerarCodigo(int idUsuario, int idVoucher) {
        return "SP-" + idUsuario + "-" + idVoucher + "-" + System.currentTimeMillis();
    }

    // estaVencido - compara a validade com a data de hoje
    public boolean estaVencido() {
        if (dataValidade == null) {
            return true;
        }
        return LocalDate.now().isAfter(dataValidade);
    }

    // estaValido - precisa estar ATIVO e dentro da validade
    public boolean estaValido() {
        return STATUS_DISPONIVEL.equalsIgnoreCase(statusVoucher) && !estaVencido();
    }

    // diasRestantes - quantos dias faltam para vencer
    public long diasRestantes() {
        if (dataValidade == null || estaVencido()) {
            return 0;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), dataValidade);
    }

    // utilizar - marca o resgate como usado, respeitando as regras
    public String utilizar() {
        if (STATUS_UTILIZADO.equalsIgnoreCase(statusVoucher)) {
            return "Este voucher ja foi utilizado!";
        }
        if (estaVencido()) {
            this.statusVoucher = STATUS_EXPIRADO;
            return "Voucher expirado em " + dataValidade + ". Nao pode mais ser usado.";
        }

        this.statusVoucher = STATUS_UTILIZADO;
        return "Voucher " + codigoGerado + " utilizado com sucesso!";
    }

    // atualizarStatusExpirado - rotina que marca vencidos como EXPIRADO
    public boolean atualizarStatusExpirado() {
        if (STATUS_DISPONIVEL.equalsIgnoreCase(statusVoucher) && estaVencido()) {
            this.statusVoucher = STATUS_EXPIRADO;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "\nResgate " +
                "\nidResgate=" + idResgate +
                "\nidUsuario=" + idUsuario +
                "\nidVoucher=" + idVoucher +
                "\ncodigoGerado='" + codigoGerado + '\'' +
                "\ndataResgate=" + dataResgate +
                "\ndataValidade=" + dataValidade +
                "\ndiasRestantes=" + diasRestantes() +
                "\nstatusVoucher='" + statusVoucher + '\'';
    }
}
