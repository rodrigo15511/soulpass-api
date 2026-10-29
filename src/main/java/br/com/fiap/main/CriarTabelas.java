package br.com.fiap.main;

import br.com.fiap.conexoes.ConexaoFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Cria as 11 tabelas, as 2 views e a carga inicial dos niveis.
 * Substitui o SQL Developer - basta rodar esta classe uma vez.
 * Pode ser executada de novo: os DROP limpam tudo antes de recriar.
 */
public class CriarTabelas {

    // Comandos de limpeza - erro aqui e normal na primeira execucao
    static String[] limpeza = {
            "DROP VIEW VW_PROGRESSO_USUARIO",
            "DROP VIEW VW_RANKING_USUARIOS",
            "DROP TABLE TB_INTERACAO_CHATBOT",
            "DROP TABLE TB_USUARIO_CONQUISTA",
            "DROP TABLE TB_PARTICIPACAO_DESAFIO",
            "DROP TABLE TB_DESAFIO_COLETIVO",
            "DROP TABLE TB_RESGATE",
            "DROP TABLE TB_ACAO_REALIZADA",
            "DROP TABLE TB_CONQUISTA",
            "DROP TABLE TB_VOUCHER",
            "DROP TABLE TB_MISSAO",
            "DROP TABLE TB_USUARIO",
            "DROP TABLE TB_NIVEL"
    };

    // Criacao das tabelas, views e carga inicial
    static String[] criacao = {

            "CREATE TABLE TB_NIVEL ("
                    + "ID_NIVEL NUMBER NOT NULL, "
                    + "NOME_NIVEL VARCHAR2(50) NOT NULL, "
                    + "NIVEL_ORDEM NUMBER NOT NULL, "
                    + "PONTOS_XP_NECESSARIOS NUMBER DEFAULT 0 NOT NULL, "
                    + "DESCRICAO VARCHAR2(255), "
                    + "CONSTRAINT PK_TB_NIVEL PRIMARY KEY (ID_NIVEL), "
                    + "CONSTRAINT UK_TB_NIVEL_NOME UNIQUE (NOME_NIVEL), "
                    + "CONSTRAINT UK_TB_NIVEL_ORDEM UNIQUE (NIVEL_ORDEM), "
                    + "CONSTRAINT CK_TB_NIVEL_ORDEM CHECK (NIVEL_ORDEM > 0), "
                    + "CONSTRAINT CK_TB_NIVEL_XP CHECK (PONTOS_XP_NECESSARIOS >= 0))",

            "CREATE TABLE TB_USUARIO ("
                    + "ID_USUARIO NUMBER NOT NULL, "
                    + "NOME VARCHAR2(100) NOT NULL, "
                    + "CPF VARCHAR2(14) NOT NULL, "
                    + "EMAIL VARCHAR2(100) NOT NULL, "
                    + "SALDO_PONTOS NUMBER(10,2) DEFAULT 0 NOT NULL, "
                    + "PONTOS_XP NUMBER(10,2) DEFAULT 0 NOT NULL, "
                    + "ID_NIVEL NUMBER NOT NULL, "
                    + "DATA_CADASTRO DATE DEFAULT SYSDATE NOT NULL, "
                    + "ATIVO CHAR(1) DEFAULT 'S' NOT NULL, "
                    + "CONSTRAINT PK_TB_USUARIO PRIMARY KEY (ID_USUARIO), "
                    + "CONSTRAINT UK_TB_USUARIO_CPF UNIQUE (CPF), "
                    + "CONSTRAINT UK_TB_USUARIO_EMAIL UNIQUE (EMAIL), "
                    + "CONSTRAINT FK_TB_USUARIO_NIVEL FOREIGN KEY (ID_NIVEL) "
                    + "REFERENCES TB_NIVEL (ID_NIVEL), "
                    + "CONSTRAINT CK_TB_USUARIO_SALDO CHECK (SALDO_PONTOS >= 0), "
                    + "CONSTRAINT CK_TB_USUARIO_XP CHECK (PONTOS_XP >= 0), "
                    + "CONSTRAINT CK_TB_USUARIO_ATIVO CHECK (ATIVO IN ('S','N')))",

            "CREATE TABLE TB_MISSAO ("
                    + "ID_MISSAO NUMBER NOT NULL, "
                    + "TITULO_MISSAO VARCHAR2(100) NOT NULL, "
                    + "TIPO_MISSAO VARCHAR2(30) NOT NULL, "
                    + "DESCRICAO VARCHAR2(255), "
                    + "PONTO_BASE NUMBER NOT NULL, "
                    + "ATIVA CHAR(1) DEFAULT 'S' NOT NULL, "
                    + "CONSTRAINT PK_TB_MISSAO PRIMARY KEY (ID_MISSAO), "
                    + "CONSTRAINT CK_TB_MISSAO_TIPO CHECK (TIPO_MISSAO IN "
                    + "('TRANSPORTE_PUBLICO','CAMINHADA','BICICLETA','DESAFIO_SEMANAL','DESAFIO_COLETIVO')), "
                    + "CONSTRAINT CK_TB_MISSAO_PONTO CHECK (PONTO_BASE > 0), "
                    + "CONSTRAINT CK_TB_MISSAO_ATIVA CHECK (ATIVA IN ('S','N')))",

            "CREATE TABLE TB_VOUCHER ("
                    + "ID_VOUCHER NUMBER NOT NULL, "
                    + "TIPO_TRANSPORTE VARCHAR2(60) NOT NULL, "
                    + "CUSTO_PONTOS NUMBER NOT NULL, "
                    + "VALOR_MONETARIO NUMBER(10,2) NOT NULL, "
                    + "DESCRICAO VARCHAR2(255), "
                    + "CONSTRAINT PK_TB_VOUCHER PRIMARY KEY (ID_VOUCHER), "
                    + "CONSTRAINT CK_TB_VOUCHER_TIPO CHECK (TIPO_TRANSPORTE IN "
                    + "('ONIBUS','METRO','TREM','BICICLETA','APLICATIVO')), "
                    + "CONSTRAINT CK_TB_VOUCHER_CUSTO CHECK (CUSTO_PONTOS > 0), "
                    + "CONSTRAINT CK_TB_VOUCHER_VALOR CHECK (VALOR_MONETARIO > 0))",

            "CREATE TABLE TB_CONQUISTA ("
                    + "ID_CONQUISTA NUMBER NOT NULL, "
                    + "NOME_CONQUISTA VARCHAR2(100) NOT NULL, "
                    + "DESCRICAO VARCHAR2(255), "
                    + "CRITERIO VARCHAR2(255) NOT NULL, "
                    + "PONTOS_BONUS NUMBER DEFAULT 0 NOT NULL, "
                    + "CONSTRAINT PK_TB_CONQUISTA PRIMARY KEY (ID_CONQUISTA), "
                    + "CONSTRAINT UK_TB_CONQUISTA_NOME UNIQUE (NOME_CONQUISTA), "
                    + "CONSTRAINT CK_TB_CONQUISTA_BONUS CHECK (PONTOS_BONUS >= 0))",

            "CREATE TABLE TB_ACAO_REALIZADA ("
                    + "ID_ACAO NUMBER NOT NULL, "
                    + "ID_USUARIO NUMBER NOT NULL, "
                    + "ID_MISSAO NUMBER NOT NULL, "
                    + "METODO_VALIDACAO VARCHAR2(20) DEFAULT 'CODIGO' NOT NULL, "
                    + "CODIGO_VALIDACAO VARCHAR2(65), "
                    + "DATA_CONCLUSAO DATE DEFAULT SYSDATE NOT NULL, "
                    + "STATUS_APROVACAO VARCHAR2(20) NOT NULL, "
                    + "PONTOS_CONQUISTADOS NUMBER DEFAULT 0 NOT NULL, "
                    + "CONSTRAINT PK_TB_ACAO_REALIZADA PRIMARY KEY (ID_ACAO), "
                    + "CONSTRAINT FK_TB_ACAO_USUARIO FOREIGN KEY (ID_USUARIO) "
                    + "REFERENCES TB_USUARIO (ID_USUARIO), "
                    + "CONSTRAINT FK_TB_ACAO_MISSAO FOREIGN KEY (ID_MISSAO) "
                    + "REFERENCES TB_MISSAO (ID_MISSAO), "
                    + "CONSTRAINT CK_TB_ACAO_METODO CHECK (METODO_VALIDACAO IN "
                    + "('CHECKIN','CODIGO','QRCODE')), "
                    + "CONSTRAINT CK_TB_ACAO_STATUS CHECK (STATUS_APROVACAO IN "
                    + "('PENDENTE','APROVADO','REJEITADO')), "
                    + "CONSTRAINT CK_TB_ACAO_PONTOS CHECK (PONTOS_CONQUISTADOS >= 0))",

            "CREATE TABLE TB_RESGATE ("
                    + "ID_RESGATE NUMBER NOT NULL, "
                    + "ID_USUARIO NUMBER NOT NULL, "
                    + "ID_VOUCHER NUMBER NOT NULL, "
                    + "ID_ACAO NUMBER, "
                    + "CODIGO_GERADO VARCHAR2(65) NOT NULL, "
                    + "DATA_RESGATE DATE DEFAULT SYSDATE NOT NULL, "
                    + "DATA_VALIDADE DATE NOT NULL, "
                    + "STATUS_VOUCHER VARCHAR2(20) NOT NULL, "
                    + "CONSTRAINT PK_TB_RESGATE PRIMARY KEY (ID_RESGATE), "
                    + "CONSTRAINT UK_TB_RESGATE_CODIGO UNIQUE (CODIGO_GERADO), "
                    + "CONSTRAINT FK_TB_RESGATE_USUARIO FOREIGN KEY (ID_USUARIO) "
                    + "REFERENCES TB_USUARIO (ID_USUARIO), "
                    + "CONSTRAINT FK_TB_RESGATE_VOUCHER FOREIGN KEY (ID_VOUCHER) "
                    + "REFERENCES TB_VOUCHER (ID_VOUCHER), "
                    + "CONSTRAINT FK_TB_RESGATE_ACAO FOREIGN KEY (ID_ACAO) "
                    + "REFERENCES TB_ACAO_REALIZADA (ID_ACAO), "
                    + "CONSTRAINT CK_TB_RESGATE_VALIDADE CHECK (DATA_VALIDADE >= DATA_RESGATE), "
                    + "CONSTRAINT CK_TB_RESGATE_STATUS CHECK (STATUS_VOUCHER IN "
                    + "('DISPONIVEL','UTILIZADO','EXPIRADO')))",

            "CREATE TABLE TB_DESAFIO_COLETIVO ("
                    + "ID_DESAFIO NUMBER NOT NULL, "
                    + "ID_MISSAO NUMBER NOT NULL, "
                    + "TITULO_DESAFIO VARCHAR2(100) NOT NULL, "
                    + "META_PONTOS_GRUPO NUMBER NOT NULL, "
                    + "DATA_INICIO DATE DEFAULT SYSDATE NOT NULL, "
                    + "DATA_FIM DATE NOT NULL, "
                    + "STATUS_DESAFIO VARCHAR2(20) DEFAULT 'EM_ANDAMENTO' NOT NULL, "
                    + "CONSTRAINT PK_TB_DESAFIO_COLETIVO PRIMARY KEY (ID_DESAFIO), "
                    + "CONSTRAINT FK_TB_DESAFIO_MISSAO FOREIGN KEY (ID_MISSAO) "
                    + "REFERENCES TB_MISSAO (ID_MISSAO), "
                    + "CONSTRAINT CK_TB_DESAFIO_META CHECK (META_PONTOS_GRUPO > 0), "
                    + "CONSTRAINT CK_TB_DESAFIO_DATAS CHECK (DATA_FIM >= DATA_INICIO), "
                    + "CONSTRAINT CK_TB_DESAFIO_STATUS CHECK (STATUS_DESAFIO IN "
                    + "('EM_ANDAMENTO','CONCLUIDO','EXPIRADO')))",

            "CREATE TABLE TB_PARTICIPACAO_DESAFIO ("
                    + "ID_PARTICIPACAO NUMBER NOT NULL, "
                    + "ID_DESAFIO NUMBER NOT NULL, "
                    + "ID_USUARIO NUMBER NOT NULL, "
                    + "PONTOS_CONTRIBUIDOS NUMBER DEFAULT 0 NOT NULL, "
                    + "DATA_INGRESSO DATE DEFAULT SYSDATE NOT NULL, "
                    + "CONSTRAINT PK_TB_PARTICIPACAO_DESAFIO PRIMARY KEY (ID_PARTICIPACAO), "
                    + "CONSTRAINT UK_TB_PARTICIPACAO UNIQUE (ID_DESAFIO, ID_USUARIO), "
                    + "CONSTRAINT FK_TB_PARTICIPACAO_DESAFIO FOREIGN KEY (ID_DESAFIO) "
                    + "REFERENCES TB_DESAFIO_COLETIVO (ID_DESAFIO), "
                    + "CONSTRAINT FK_TB_PARTICIPACAO_USUARIO FOREIGN KEY (ID_USUARIO) "
                    + "REFERENCES TB_USUARIO (ID_USUARIO), "
                    + "CONSTRAINT CK_TB_PARTICIPACAO_PONTOS CHECK (PONTOS_CONTRIBUIDOS >= 0))",

            "CREATE TABLE TB_USUARIO_CONQUISTA ("
                    + "ID_USUARIO_CONQUISTA NUMBER NOT NULL, "
                    + "ID_USUARIO NUMBER NOT NULL, "
                    + "ID_CONQUISTA NUMBER NOT NULL, "
                    + "DATA_CONQUISTA DATE DEFAULT SYSDATE NOT NULL, "
                    + "CONSTRAINT PK_TB_USUARIO_CONQUISTA PRIMARY KEY (ID_USUARIO_CONQUISTA), "
                    + "CONSTRAINT UK_TB_USUARIO_CONQUISTA UNIQUE (ID_USUARIO, ID_CONQUISTA), "
                    + "CONSTRAINT FK_TB_USU_CONQ_USUARIO FOREIGN KEY (ID_USUARIO) "
                    + "REFERENCES TB_USUARIO (ID_USUARIO), "
                    + "CONSTRAINT FK_TB_USU_CONQ_CONQUISTA FOREIGN KEY (ID_CONQUISTA) "
                    + "REFERENCES TB_CONQUISTA (ID_CONQUISTA))",

            "CREATE TABLE TB_INTERACAO_CHATBOT ("
                    + "ID_INTERACAO NUMBER NOT NULL, "
                    + "ID_USUARIO NUMBER NOT NULL, "
                    + "CATEGORIA VARCHAR2(30) DEFAULT 'DUVIDA' NOT NULL, "
                    + "PERGUNTA VARCHAR2(500) NOT NULL, "
                    + "RESPOSTA VARCHAR2(2000), "
                    + "DATA_INTERACAO DATE DEFAULT SYSDATE NOT NULL, "
                    + "CONSTRAINT PK_TB_INTERACAO_CHATBOT PRIMARY KEY (ID_INTERACAO), "
                    + "CONSTRAINT FK_TB_INTERACAO_USUARIO FOREIGN KEY (ID_USUARIO) "
                    + "REFERENCES TB_USUARIO (ID_USUARIO), "
                    + "CONSTRAINT CK_TB_INTERACAO_CATEGORIA CHECK (CATEGORIA IN "
                    + "('DUVIDA','FUNCIONALIDADE','MISSAO','DESAFIO')))",

            "CREATE OR REPLACE VIEW VW_RANKING_USUARIOS AS "
                    + "SELECT U.ID_USUARIO, U.NOME, U.PONTOS_XP, N.NOME_NIVEL, N.NIVEL_ORDEM "
                    + "FROM TB_USUARIO U INNER JOIN TB_NIVEL N ON U.ID_NIVEL = N.ID_NIVEL "
                    + "WHERE U.ATIVO = 'S' ORDER BY U.PONTOS_XP DESC",

            "CREATE OR REPLACE VIEW VW_PROGRESSO_USUARIO AS "
                    + "SELECT U.ID_USUARIO, U.NOME, U.SALDO_PONTOS, U.PONTOS_XP, "
                    + "N.NOME_NIVEL, N.NIVEL_ORDEM, "
                    + "(SELECT COUNT(*) FROM TB_ACAO_REALIZADA A WHERE A.ID_USUARIO = U.ID_USUARIO "
                    + "AND A.STATUS_APROVACAO = 'APROVADO') AS MISSOES_CONCLUIDAS, "
                    + "(SELECT MIN(N2.PONTOS_XP_NECESSARIOS) - U.PONTOS_XP FROM TB_NIVEL N2 "
                    + "WHERE N2.PONTOS_XP_NECESSARIOS > U.PONTOS_XP) AS XP_PARA_PROXIMO_NIVEL "
                    + "FROM TB_USUARIO U INNER JOIN TB_NIVEL N ON U.ID_NIVEL = N.ID_NIVEL "
                    + "WHERE U.ATIVO = 'S'",

            "INSERT INTO TB_NIVEL VALUES (1, 'Iniciante', 1, 0, 'Comecando na plataforma')",
            "INSERT INTO TB_NIVEL VALUES (2, 'Explorador', 2, 500, 'Ja concluiu varias missoes')",
            "INSERT INTO TB_NIVEL VALUES (3, 'Mobilizador', 3, 1500, 'Participa de desafios coletivos')",
            "INSERT INTO TB_NIVEL VALUES (4, 'Agente Sustentavel', 4, 3000, 'Referencia em mobilidade')"
    };

    public static void main(String[] args) throws SQLException, ClassNotFoundException {

        Connection conexao = new ConexaoFactory().conexao();
        Statement stmt = conexao.createStatement();

        System.out.println("===== CRIANDO O BANCO DA SOULPASS 2.0 =====");

        // Limpeza - erros sao esperados e ignorados
        System.out.println("\n--- Limpando estruturas antigas ---");
        for (String comando : limpeza) {
            try {
                stmt.execute(comando);
                System.out.println("OK   " + comando);
            } catch (SQLException e) {
                System.out.println("--   " + comando + " (nao existia)");
            }
        }

        // Criacao - aqui erro e problema de verdade
        System.out.println("\n--- Criando estruturas ---");
        int sucesso = 0;
        int falha = 0;

        for (String comando : criacao) {
            String resumo = comando.length() > 60 ? comando.substring(0, 60) + "..." : comando;

            try {
                stmt.execute(comando);
                System.out.println("OK   " + resumo);
                sucesso = sucesso + 1;
            } catch (SQLException e) {
                System.out.println("ERRO " + resumo);
                System.out.println("     >>> " + e.getMessage());
                falha = falha + 1;
            }
        }

        stmt.close();
        conexao.close();

        System.out.println("\n===== RESULTADO =====");
        System.out.println("Comandos executados com sucesso: " + sucesso);
        System.out.println("Comandos com falha: " + falha);

        if (falha == 0) {
            System.out.println("\nBanco criado! Agora pode rodar o TesteCrud.");
        } else {
            System.out.println("\nAlgo falhou. Veja as mensagens ERRO acima.");
        }
    }
}