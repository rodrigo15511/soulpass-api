package br.com.fiap.dao;

import br.com.fiap.conexoes.ConexaoFactory;
import br.com.fiap.entities.Missao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * CRUD completo de TB_MISSAO.
 */
public class MissaoDao {

    public Connection minhaConexao;

    public MissaoDao() throws SQLException, ClassNotFoundException {
        this.minhaConexao = new ConexaoFactory().conexao();
    }

    // Fechar a conexao - chamado pelo BO ao final de cada requisicao da API
    public void fecharConexao() throws SQLException {
        if (minhaConexao != null && !minhaConexao.isClosed()) {
            minhaConexao.close();
        }
    }

    // Inserir -> Insert
    public String inserir(Missao missao) throws SQLException {
        if (!missao.tipoValido()) {
            return "Tipo de missao invalido! O banco aceita apenas os tipos previstos.";
        }
        if (missao.getPontoBase() <= 0) {
            return "PONTO_BASE precisa ser maior que zero!";
        }

        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Insert Into TB_MISSAO (ID_MISSAO, TITULO_MISSAO, TIPO_MISSAO, DESCRICAO, "
                        + "PONTO_BASE, ATIVA) values (?,?,?,?,?,?)");

        stmt.setInt(1, missao.getIdMissao());
        stmt.setString(2, missao.getTituloMissao());
        stmt.setString(3, missao.getTipoMissao().toUpperCase());
        stmt.setString(4, missao.getDescricao());
        stmt.setDouble(5, missao.getPontoBase());
        stmt.setString(6, missao.isAtiva() ? "S" : "N");

        stmt.execute();
        stmt.close();

        return "Missao Cadastrada com Sucesso!!!";
    }

    // Deletar -> Delete
    public String deletar(int idMissao) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Delete From TB_MISSAO where ID_MISSAO =?");
        stmt.setInt(1, idMissao);

        stmt.execute();
        stmt.close();

        return "Missao Deletada com Sucesso!!!";
    }

    // Atualizar -> UpDate
    public String atualizar(Missao missao) throws SQLException {
        if (!missao.tipoValido()) {
            return "Tipo de missao invalido!";
        }

        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Update TB_MISSAO set TITULO_MISSAO =?, TIPO_MISSAO =?, DESCRICAO =?, "
                        + "PONTO_BASE =?, ATIVA =? where ID_MISSAO =?");

        stmt.setString(1, missao.getTituloMissao());
        stmt.setString(2, missao.getTipoMissao().toUpperCase());
        stmt.setString(3, missao.getDescricao());
        stmt.setDouble(4, missao.getPontoBase());
        stmt.setString(5, missao.isAtiva() ? "S" : "N");
        stmt.setInt(6, missao.getIdMissao());

        stmt.executeUpdate();
        stmt.close();

        return "Missao Atualizada com Sucesso!!!";
    }

    // Selecionar -> Select
    public ArrayList<Missao> selecionar() throws SQLException {
        ArrayList<Missao> listaMissao = new ArrayList<Missao>();

        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Select * From TB_MISSAO Order By ID_MISSAO");

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            listaMissao.add(montarMissao(rs));
        }

        rs.close();
        stmt.close();

        return listaMissao;
    }

    // Selecionar -> buscarPorId
    public Missao buscarPorId(int idMissao) throws SQLException {
        Missao missao = null;

        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Select * From TB_MISSAO Where ID_MISSAO =?");
        stmt.setInt(1, idMissao);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            missao = montarMissao(rs);
        }

        rs.close();
        stmt.close();

        return missao;
    }

    // Selecionar -> somente as missoes ativas
    public ArrayList<Missao> selecionarAtivas() throws SQLException {
        ArrayList<Missao> listaMissao = new ArrayList<Missao>();

        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Select * From TB_MISSAO Where ATIVA ='S' Order By TIPO_MISSAO, ID_MISSAO");

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            listaMissao.add(montarMissao(rs));
        }

        rs.close();
        stmt.close();

        return listaMissao;
    }

    // Selecionar -> missoes de um tipo especifico
    public ArrayList<Missao> buscarPorTipo(String tipoMissao) throws SQLException {
        ArrayList<Missao> listaMissao = new ArrayList<Missao>();

        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Select * From TB_MISSAO Where TIPO_MISSAO =? And ATIVA ='S' Order By ID_MISSAO");
        stmt.setString(1, tipoMissao.toUpperCase());

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            listaMissao.add(montarMissao(rs));
        }

        rs.close();
        stmt.close();

        return listaMissao;
    }

    // Proximo ID disponivel
    public int proximoId() throws SQLException {
        int proximo = 1;

        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Select Nvl(Max(ID_MISSAO), 0) + 1 From TB_MISSAO");

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            proximo = rs.getInt(1);
        }

        rs.close();
        stmt.close();

        return proximo;
    }

    // metodo auxiliar
    private Missao montarMissao(ResultSet rs) throws SQLException {
        Missao missao = new Missao();
        missao.setIdMissao(rs.getInt("ID_MISSAO"));
        missao.setTituloMissao(rs.getString("TITULO_MISSAO"));
        missao.setTipoMissao(rs.getString("TIPO_MISSAO"));
        missao.setDescricao(rs.getString("DESCRICAO"));
        missao.setPontoBase(rs.getDouble("PONTO_BASE"));
        missao.setAtiva("S".equals(rs.getString("ATIVA")));

        return missao;
    }
}
