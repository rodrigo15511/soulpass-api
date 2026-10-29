package br.com.fiap.entities;

import java.time.LocalDate;

/**
 * Mapeia TB_USUARIO.
 * saldo_pontos  -> moeda gastavel (diminui no resgate)
 * pontos_xp     -> acumulado historico (nunca diminui, define o nivel)
 */
public class Usuario extends Pessoa {

    private int idUsuario;
    private double saldoPontos;
    private double pontosXp;
    private Nivel nivel;
    private LocalDate dataCadastro;
    private boolean ativo;

    public Usuario() {
        this.ativo = true;
        this.dataCadastro = LocalDate.now();
    }

    public Usuario(int idUsuario, String nome, String cpf, String email, double saldoPontos,
                   double pontosXp, Nivel nivel, LocalDate dataCadastro, boolean ativo) {
        super(nome, cpf, email);
        this.idUsuario = idUsuario;
        this.saldoPontos = saldoPontos;
        this.pontosXp = pontosXp;
        this.nivel = nivel;
        this.dataCadastro = dataCadastro;
        this.ativo = ativo;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public double getSaldoPontos() {
        return saldoPontos;
    }

    public void setSaldoPontos(double saldoPontos) {
        this.saldoPontos = saldoPontos;
    }

    public double getPontosXp() {
        return pontosXp;
    }

    public void setPontosXp(double pontosXp) {
        this.pontosXp = pontosXp;
    }

    public Nivel getNivel() {
        return nivel;
    }

    public void setNivel(Nivel nivel) {
        this.nivel = nivel;
    }

    public LocalDate getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDate dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public String tipoPessoa() {
        return "Usuario SoulPass";
    }

    // creditarPontos - soma ao saldo e ao XP quando uma acao e aprovada
    public String creditarPontos(double pontos, String origem) {
        if (!ativo) {
            return "Usuario inativo! Nao e possivel creditar pontos.";
        }
        if (pontos <= 0) {
            return "Quantidade invalida! Informe um valor maior que zero.";
        }

        this.saldoPontos = this.saldoPontos + pontos;
        this.pontosXp = this.pontosXp + pontos;

        return "Creditados " + pontos + " pontos (" + origem + ")."
                + "\nSaldo: " + saldoPontos + " | XP: " + pontosXp;
    }

    // debitarPontos - so debita o saldo; o XP permanece intacto
    public String debitarPontos(double pontos, String destino) {
        if (!ativo) {
            return "Usuario inativo! Nao e possivel debitar pontos.";
        }
        if (pontos <= 0) {
            return "Quantidade invalida!";
        }
        if (pontos > this.saldoPontos) {
            return "Saldo insuficiente! Saldo atual: " + saldoPontos + " pontos.";
        }

        this.saldoPontos = this.saldoPontos - pontos;

        return "Debitados " + pontos + " pontos (" + destino + ")."
                + "\nSaldo restante: " + saldoPontos;
    }

    // podeResgatar - verifica se o saldo cobre o custo do voucher
    public boolean podeResgatar(Voucher voucher) {
        return ativo && voucher != null && this.saldoPontos >= voucher.getCustoPontos();
    }

    // subiuDeNivel - compara o XP com o nivel informado e troca se alcancou
    public boolean subiuDeNivel(Nivel candidato) {
        if (candidato == null) {
            return false;
        }
        if (!candidato.atingidoPor(this.pontosXp)) {
            return false;
        }
        if (this.nivel != null && candidato.getNivelOrdem() <= this.nivel.getNivelOrdem()) {
            return false;
        }

        this.nivel = candidato;
        return true;
    }

    // percentualParaProximo - progresso de 0 a 100 dentro da faixa atual
    public double percentualParaProximo(Nivel proximo) {
        if (proximo == null || nivel == null) {
            return 100.0;
        }

        double base = nivel.getPontosXpNecessarios();
        double faixa = proximo.getPontosXpNecessarios() - base;

        if (faixa <= 0) {
            return 100.0;
        }

        double percentual = ((pontosXp - base) / faixa) * 100;

        if (percentual > 100) {
            percentual = 100;
        }
        if (percentual < 0) {
            percentual = 0;
        }

        return Math.round(percentual * 10) / 10.0;
    }

    // getProgressoFormatado - area de progresso descrita na proposta
    public String getProgressoFormatado(Nivel proximo) {
        String texto = "\n===== MEU PROGRESSO ====="
                + "\nNivel atual: " + (nivel != null ? nivel.getNomeNivel() : "Indefinido")
                + "\nPontos XP: " + pontosXp
                + "\nSaldo disponivel: " + saldoPontos;

        if (proximo == null) {
            texto = texto + "\nVoce alcancou o nivel maximo!";
        } else {
            texto = texto + "\nProximo nivel: " + proximo.getNomeNivel()
                    + "\nFaltam " + proximo.pontosFaltantes(pontosXp) + " pontos XP"
                    + "\nProgresso: " + percentualParaProximo(proximo) + "%";
        }

        return texto;
    }

    @Override
    public String toString() {
        return "\nUsuario " +
                "\nidUsuario=" + idUsuario +
                super.toString() +
                "\ntipoPessoa='" + tipoPessoa() + '\'' +
                "\nnivel='" + (nivel != null ? nivel.getNomeNivel() : "Indefinido") + '\'' +
                "\nsaldoPontos=" + saldoPontos +
                "\npontosXp=" + pontosXp +
                "\ndataCadastro=" + dataCadastro +
                "\nativo=" + (ativo ? "Sim" : "Nao");
    }
}
