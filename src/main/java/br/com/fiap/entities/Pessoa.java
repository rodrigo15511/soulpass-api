package br.com.fiap.entities;

public abstract class Pessoa {

    private String nome;
    private String cpf;
    private String email;

    public Pessoa() {
    }

    public Pessoa(String nome, String cpf, String email) {
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // metodo abstrato - cada subclasse informa o seu tipo
    public abstract String tipoPessoa();

    // validarCpf - aceita com ou sem mascara (coluna CPF tem 14 posicoes)
    public boolean validarCpf() {
        if (cpf == null) {
            return false;
        }

        String numeros = cpf.replace(".", "").replace("-", "").trim();

        if (numeros.length() != 11) {
            return false;
        }

        for (int i = 0; i < numeros.length(); i++) {
            if (!Character.isDigit(numeros.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    // validarEmail - verifica o formato basico do e-mail
    public boolean validarEmail() {
        if (email == null) {
            return false;
        }
        return email.contains("@") && email.contains(".") && email.length() >= 6;
    }

    @Override
    public String toString() {
        return "\nnome='" + nome + '\'' +
                "\ncpf='" + cpf + '\'' +
                "\nemail='" + email + '\'';
    }
}
