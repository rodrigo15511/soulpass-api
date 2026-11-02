package br.com.fiap.dao;

import br.com.fiap.conexoes.ConexaoFactory;
import br.com.fiap.entities.Nivel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Leitura de TB_NIVEL.
 * Os niveis sao dados fixos cadastrados no banco - a API so consulta.
 */
public class NivelDao {

    public Connection minhaConexao;

    public NivelDao() throws SQLException, ClassNotFoundException {
        this.minhaConexao = new ConexaoFactory().conexao();
    }

    // Fechar a conexao - chamado pelo BO ao final de cada requisicao da API
    public void fecharConexao() throws SQLException {
        if (minhaConexao != null && !minhaConexao.isClosed()) {
            minhaConexao.close();
        }
    }

    // Selecionar -> todos os niveis, do mais baixo para o mais alto
    public ArrayList<Nivel> selecionar() throws SQLException {
        ArrayList<Nivel> lista = new ArrayList<Nivel>();

        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Select * From TB_NIVEL Order By NIVEL_ORDEM");

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            lista.add(new Nivel(
                    rs.getInt("ID_NIVEL"),
                    rs.getString("NOME_NIVEL"),
                    rs.getInt("NIVEL_ORDEM"),
                    rs.getDouble("PONTOS_XP_NECESSARIOS"),
                    rs.getString("DESCRICAO")));
        }

        rs.close();
        stmt.close();

        return lista;
    }
}
