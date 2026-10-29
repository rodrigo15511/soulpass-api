package br.com.fiap.conexoes;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Fabrica de conexoes com o Oracle.
 * Usuario e senha NAO ficam no codigo (o projeto vai para o GitHub):
 *  - no Render (deploy): variaveis de ambiente DB_URL, DB_USER e DB_PASSWORD
 *  - no seu computador: arquivo db-local.properties na raiz do projeto (fora do Git)
 */
public class ConexaoFactory {

    private static final String URL_PADRAO = "jdbc:oracle:thin:@oracle.fiap.com.br:1521:orcl";
    private static final String ARQUIVO_LOCAL = "db-local.properties";

    // metodo de conexao com o banco de dados
    public Connection conexao() throws ClassNotFoundException, SQLException {

        // Driver
        Class.forName("oracle.jdbc.driver.OracleDriver");

        Properties local = lerArquivoLocal();

        String url = valor("DB_URL", "db.url", local, URL_PADRAO);
        String usuario = valor("DB_USER", "db.user", local, null);
        String senha = valor("DB_PASSWORD", "db.password", local, null);

        if (usuario == null || senha == null) {
            throw new SQLException("Credenciais do banco nao configuradas. Defina DB_USER e DB_PASSWORD "
                    + "ou crie o arquivo " + ARQUIVO_LOCAL + ".");
        }

        // retornar a conexao
        return DriverManager.getConnection(url, usuario, senha);
    }

    // valor - a variavel de ambiente tem prioridade; senao usa o arquivo local; senao o padrao
    private String valor(String variavel, String chave, Properties local, String padrao) {
        String doAmbiente = System.getenv(variavel);
        if (doAmbiente != null && !doAmbiente.isBlank()) {
            return doAmbiente;
        }
        return local.getProperty(chave, padrao);
    }

    // lerArquivoLocal - devolve vazio se o arquivo nao existir (caso do deploy)
    private Properties lerArquivoLocal() {
        Properties props = new Properties();
        try (FileInputStream arquivo = new FileInputStream(ARQUIVO_LOCAL)) {
            props.load(arquivo);
        } catch (IOException e) {
            // sem arquivo local: segue so com as variaveis de ambiente
        }
        return props;
    }
}
