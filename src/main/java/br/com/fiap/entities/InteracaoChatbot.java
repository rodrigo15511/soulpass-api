package br.com.fiap.entities;

import java.time.LocalDate;

/**
 * Mapeia TB_INTERACAO_CHATBOT.
 * Guarda o historico de perguntas e respostas do assistente de IA.
 */
public class InteracaoChatbot {

    private int idInteracao;
    private int idUsuario;
    private String categoria;
    private String pergunta;
    private String resposta;
    private LocalDate dataInteracao;

    // Categorias aceitas - CHECK IN (DUVIDA, FUNCIONALIDADE, MISSAO, DESAFIO)
    public static final String CATEGORIA_DUVIDA = "DUVIDA";
    public static final String CATEGORIA_FUNCIONALIDADE = "FUNCIONALIDADE";
    public static final String CATEGORIA_MISSAO = "MISSAO";
    public static final String CATEGORIA_DESAFIO = "DESAFIO";

    // Limites das colunas no banco
    public static final int LIMITE_PERGUNTA = 500;
    public static final int LIMITE_RESPOSTA = 2000;

    public InteracaoChatbot() {
        this.dataInteracao = LocalDate.now();
        this.categoria = CATEGORIA_DUVIDA;
    }

    public InteracaoChatbot(int idInteracao, int idUsuario, String categoria, String pergunta,
                            String resposta, LocalDate dataInteracao) {
        this.idInteracao = idInteracao;
        this.idUsuario = idUsuario;
        this.categoria = categoria;
        this.pergunta = pergunta;
        this.resposta = resposta;
        this.dataInteracao = dataInteracao;
    }

    public int getIdInteracao() {
        return idInteracao;
    }

    public void setIdInteracao(int idInteracao) {
        this.idInteracao = idInteracao;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getPergunta() {
        return pergunta;
    }

    public void setPergunta(String pergunta) {
        this.pergunta = pergunta;
    }

    public String getResposta() {
        return resposta;
    }

    public void setResposta(String resposta) {
        this.resposta = resposta;
    }

    public LocalDate getDataInteracao() {
        return dataInteracao;
    }

    public void setDataInteracao(LocalDate dataInteracao) {
        this.dataInteracao = dataInteracao;
    }

    // classificarPergunta - deduz a categoria pelas palavras da pergunta
    public String classificarPergunta() {
        if (pergunta == null) {
            return CATEGORIA_DUVIDA;
        }

        String texto = pergunta.toLowerCase();

        if (texto.contains("desafio") || texto.contains("grupo") || texto.contains("coletivo")) {
            return CATEGORIA_DESAFIO;
        }
        if (texto.contains("missao") || texto.contains("missão") || texto.contains("pedalar")) {
            return CATEGORIA_MISSAO;
        }
        if (texto.contains("como funciona") || texto.contains("nivel") || texto.contains("nível")
                || texto.contains("voucher") || texto.contains("resgate")
                || texto.contains("ponto") || texto.contains("saldo")) {
            return CATEGORIA_FUNCIONALIDADE;
        }

        return CATEGORIA_DUVIDA;
    }

    // categoriaValida - confere se a categoria respeita o CHECK do banco
    public boolean categoriaValida() {
        if (categoria == null) {
            return false;
        }

        String c = categoria.toUpperCase();

        return c.equals(CATEGORIA_DUVIDA) || c.equals(CATEGORIA_FUNCIONALIDADE)
                || c.equals(CATEGORIA_MISSAO) || c.equals(CATEGORIA_DESAFIO);
    }

    // registrar - classifica e valida os limites antes de gravar
    public String registrar(String pergunta, String resposta) {
        if (pergunta == null || pergunta.isBlank()) {
            return "Pergunta nao informada!";
        }
        if (pergunta.length() > LIMITE_PERGUNTA) {
            return "Pergunta excede o limite de " + LIMITE_PERGUNTA + " caracteres.";
        }
        if (resposta != null && resposta.length() > LIMITE_RESPOSTA) {
            return "Resposta excede o limite de " + LIMITE_RESPOSTA + " caracteres.";
        }

        this.pergunta = pergunta;
        this.resposta = resposta;
        this.categoria = classificarPergunta();
        this.dataInteracao = LocalDate.now();

        return "Interacao registrada na categoria " + categoria + ".";
    }

    @Override
    public String toString() {
        return "\nInteracaoChatbot " +
                "\nidInteracao=" + idInteracao +
                "\nidUsuario=" + idUsuario +
                "\ncategoria='" + categoria + '\'' +
                "\npergunta='" + pergunta + '\'' +
                "\nresposta='" + resposta + '\'' +
                "\ndataInteracao=" + dataInteracao;
    }
}
