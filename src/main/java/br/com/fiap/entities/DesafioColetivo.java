package br.com.fiap.entities;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Mapeia TB_DESAFIO_COLETIVO.
 * A tabela nao tem coluna de progresso: ele e calculado pela soma das
 * contribuicoes em TB_PARTICIPACAO_DESAFIO.
 * A recompensa tambem nao tem coluna propria - sai do PONTO_BASE da
 * missao vinculada (FK ID_MISSAO).
 */
public class DesafioColetivo {

    private int idDesafio;
    private int idMissao;
    private String tituloDesafio;
    private double metaPontosGrupo;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String statusDesafio;
    private ArrayList<ParticipacaoDesafio> participacoes;

    // Valores aceitos em STATUS_DESAFIO - CHECK IN (EM_ANDAMENTO, CONCLUIDO, EXPIRADO)
    public static final String STATUS_EM_ANDAMENTO = "EM_ANDAMENTO";
    public static final String STATUS_CONCLUIDO = "CONCLUIDO";
    public static final String STATUS_EXPIRADO = "EXPIRADO";

    // Regra de negocio: minimo de participantes
    public static final int MINIMO_PARTICIPANTES = 3;

    public DesafioColetivo() {
        this.participacoes = new ArrayList<ParticipacaoDesafio>();
        this.statusDesafio = STATUS_EM_ANDAMENTO;
    }

    public DesafioColetivo(int idDesafio, int idMissao, String tituloDesafio,
                           double metaPontosGrupo, LocalDate dataInicio, LocalDate dataFim,
                           String statusDesafio) {
        this.idDesafio = idDesafio;
        this.idMissao = idMissao;
        this.tituloDesafio = tituloDesafio;
        this.metaPontosGrupo = metaPontosGrupo;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.statusDesafio = statusDesafio;
        this.participacoes = new ArrayList<ParticipacaoDesafio>();
    }

    public int getIdDesafio() {
        return idDesafio;
    }

    public void setIdDesafio(int idDesafio) {
        this.idDesafio = idDesafio;
    }

    public int getIdMissao() {
        return idMissao;
    }

    public void setIdMissao(int idMissao) {
        this.idMissao = idMissao;
    }

    public String getTituloDesafio() {
        return tituloDesafio;
    }

    public void setTituloDesafio(String tituloDesafio) {
        this.tituloDesafio = tituloDesafio;
    }

    public double getMetaPontosGrupo() {
        return metaPontosGrupo;
    }

    public void setMetaPontosGrupo(double metaPontosGrupo) {
        this.metaPontosGrupo = metaPontosGrupo;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public String getStatusDesafio() {
        return statusDesafio;
    }

    public void setStatusDesafio(String statusDesafio) {
        this.statusDesafio = statusDesafio;
    }

    public ArrayList<ParticipacaoDesafio> getParticipacoes() {
        return participacoes;
    }

    public void setParticipacoes(ArrayList<ParticipacaoDesafio> participacoes) {
        this.participacoes = participacoes;
    }

    // progressoAtual - soma das contribuicoes (equivale ao SUM do banco)
    public double progressoAtual() {
        double total = 0;

        if (participacoes != null) {
            for (ParticipacaoDesafio participacao : participacoes) {
                total = total + participacao.getPontosContribuidos();
            }
        }
        return total;
    }

    // percentualConcluido - progresso do grupo, de 0 a 100
    public double percentualConcluido() {
        if (metaPontosGrupo <= 0) {
            return 0.0;
        }

        double percentual = (progressoAtual() / metaPontosGrupo) * 100;

        if (percentual > 100) {
            percentual = 100;
        }
        return Math.round(percentual * 10) / 10.0;
    }

    // inscrever - adiciona um participante sem duplicar
    public String inscrever(Usuario usuario) {
        if (usuario == null) {
            return "Usuario nao informado!";
        }
        if (!usuario.isAtivo()) {
            return "Usuario inativo nao pode participar de desafios.";
        }
        if (!estaAberto()) {
            return "Este desafio esta " + statusDesafio.toLowerCase() + ".";
        }

        for (ParticipacaoDesafio participacao : participacoes) {
            if (participacao.getIdUsuario() == usuario.getIdUsuario()) {
                return usuario.getNome() + " ja esta inscrito neste desafio.";
            }
        }

        ParticipacaoDesafio nova = new ParticipacaoDesafio();
        nova.setIdDesafio(this.idDesafio);
        nova.setIdUsuario(usuario.getIdUsuario());

        participacoes.add(nova);

        return usuario.getNome() + " inscrito em '" + tituloDesafio
                + "'! Participantes: " + participacoes.size();
    }

    // registrarContribuicao - lanca pontos de um participante na meta do grupo
    public String registrarContribuicao(int idUsuario, double pontos) {
        if (!estaAberto()) {
            return "Desafio " + statusDesafio.toLowerCase() + "! Nao aceita contribuicoes.";
        }
        if (!temParticipantesSuficientes()) {
            return "O desafio precisa de pelo menos " + MINIMO_PARTICIPANTES
                    + " participantes. Atualmente: " + participacoes.size() + ".";
        }

        for (ParticipacaoDesafio participacao : participacoes) {
            if (participacao.getIdUsuario() == idUsuario) {

                String resultado = participacao.contribuir(pontos);

                if (atingiuMeta()) {
                    this.statusDesafio = STATUS_CONCLUIDO;
                    return "Meta alcancada! O grupo concluiu '" + tituloDesafio + "'!";
                }

                return resultado + "\nProgresso do grupo: " + progressoAtual()
                        + "/" + metaPontosGrupo + " (" + percentualConcluido() + "%)";
            }
        }

        return "Usuario nao inscrito neste desafio!";
    }

    // distribuirRecompensa - credita o ponto base da missao a cada participante
    public String distribuirRecompensa(Missao missaoVinculada, ArrayList<Usuario> inscritos) {
        if (missaoVinculada == null) {
            return "Missao vinculada nao informada!";
        }
        if (missaoVinculada.getIdMissao() != this.idMissao) {
            return "A missao informada nao corresponde a este desafio!";
        }
        if (!atingiuMeta()) {
            return "A meta ainda nao foi alcancada. Progresso: " + percentualConcluido() + "%.";
        }
        if (participacoes.isEmpty()) {
            return "Nenhum participante para receber a recompensa.";
        }
        if (inscritos == null || inscritos.isEmpty()) {
            return "Lista de usuarios inscritos nao informada!";
        }

        double recompensa = missaoVinculada.calcularPontos();
        int creditados = 0;

        // percorre as participacoes e localiza o objeto Usuario correspondente
        for (ParticipacaoDesafio participacao : participacoes) {
            for (Usuario usuario : inscritos) {
                if (usuario.getIdUsuario() == participacao.getIdUsuario()) {
                    usuario.creditarPontos(recompensa, "Desafio: " + tituloDesafio);
                    creditados = creditados + 1;
                }
            }
        }

        if (creditados == 0) {
            return "Nenhum participante localizado na lista informada!";
        }

        return "Recompensa de " + recompensa + " pontos creditada para "
                + creditados + " participantes.";
    }

    // atingiuMeta - a meta coletiva foi alcancada
    public boolean atingiuMeta() {
        return metaPontosGrupo > 0 && progressoAtual() >= metaPontosGrupo;
    }

    // estaAberto - dentro do periodo e com status ABERTO
    public boolean estaAberto() {
        if (!STATUS_EM_ANDAMENTO.equalsIgnoreCase(statusDesafio)) {
            return false;
        }
        if (dataInicio == null || dataFim == null) {
            return false;
        }

        LocalDate hoje = LocalDate.now();
        return !hoje.isBefore(dataInicio) && !hoje.isAfter(dataFim);
    }

    // temParticipantesSuficientes - respeita o minimo de inscritos
    public boolean temParticipantesSuficientes() {
        return participacoes.size() >= MINIMO_PARTICIPANTES;
    }

    @Override
    public String toString() {
        return "\nDesafioColetivo " +
                "\nidDesafio=" + idDesafio +
                "\nidMissao=" + idMissao +
                "\ntituloDesafio='" + tituloDesafio + '\'' +
                "\nprogresso=" + progressoAtual() + "/" + metaPontosGrupo +
                "\npercentual=" + percentualConcluido() + "%" +
                "\nparticipantes=" + participacoes.size() +
                "\nperiodo=" + dataInicio + " ate " + dataFim +
                "\nstatusDesafio='" + statusDesafio + '\'';
    }
}
