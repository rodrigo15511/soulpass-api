package br.com.fiap.dao;

import br.com.fiap.conexoes.ConexaoFactory;
import br.com.fiap.entities.Resgate;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

/**
 * CRUD completo de TB_RESGATE.
 * ID_ACAO e a unica FK opcional do modelo: quando vale 0, grava NULL.
 */
public class ResgateDao {

    public Connection minhaConexao;

    public ResgateDao() throws SQLException, ClassNotFoundException {
        this.minhaConexao = new ConexaoFactory().conexao();
    }

    // Fechar a conexao - chamado pelo BO ao final de cada requisicao da API
    public void fecharConexao() throws SQLException {
        if (minhaConexao != null && !minhaConexao.isClosed()) {
            minhaConexao.close();
        }
    }

    // Inserir -> Insert
    public String inserir(Resgate resgate) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Insert Into TB_RESGATE (ID_RESGATE, ID_USUARIO, ID_VOUCHER, ID_ACAO, "
                        + "CODIGO_GERADO, DATA_RESGATE, DATA_VALIDADE, STATUS_VOUCHER) "
                        + "values (?,?,?,?,?,?,?,?)");

        stmt.setInt(1, resgate.getIdResgate());
        stmt.setInt(2, resgate.getIdUsuario());
        stmt.setInt(3, resgate.getIdVoucher());

        // FK opcional - zero significa "sem acao vinculada"
        if (resgate.getIdAcao() > 0) {
            stmt.setInt(4, resgate.getIdAcao());
        } else {
            stmt.setNull(4, Types.NUMERIC);
        }

        stmt.setString(5, resgate.getCodigoGerado());
        stmt.setDate(6, Date.valueOf(resgate.getDataResgate()));
        stmt.setDate(7, Date.valueOf(resgate.getDataValidade()));
        stmt.setString(8, resgate.getStatusVoucher().toUpperCase());

        stmt.execute();
        stmt.close();

        return "Resgate Realizado com Sucesso!!!";
    }

    // Deletar -> Delete
    public String deletar(int idResgate) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Delete From TB_RESGATE where ID_RESGATE =?");
        stmt.setInt(1, idResgate);

        stmt.execute();
        stmt.close();

        return "Resgate Deletado com Sucesso!!!";
    }

    // Atualizar -> UpDate
    public String atualizar(Resgate resgate) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Update TB_RESGATE set ID_USUARIO =?, ID_VOUCHER =?, CODIGO_GERADO =?, "
                        + "DATA_VALIDADE =?, STATUS_VOUCHER =? where ID_RESGATE =?");

        stmt.setInt(1, resgate.getIdUsuario());
        stmt.setInt(2, resgate.getIdVoucher());
        stmt.setString(3, resgate.getCodigoGerado());
        stmt.setDate(4, Date.valueOf(resgate.getDataValidade()));
        stmt.setString(5, resgate.getStatusVoucher().toUpperCase());
        stmt.setInt(6, resgate.getIdResgate());

        stmt.executeUpdate();
        stmt.close();

        return "Resgate Atualizado com Sucesso!!!";
    }

    // Selecionar -> Select
    public ArrayList<Resgate> selecionar() throws SQLException {
        ArrayList<Resgate> lista = new ArrayList<Resgate>();

        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Select * From TB_RESGATE Order By DATA_RESGATE Desc");

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            lista.add(montarResgate(rs));
        }

        rs.close();
        stmt.close();

        return lista;
    }

    // Selecionar -> buscarPorId
    public Resgate buscarPorId(int idResgate) throws SQLException {
        Resgate resgate = null;

        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Select * From TB_RESGATE Where ID_RESGATE =?");
        stmt.setInt(1, idResgate);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            resgate = montarResgate(rs);
        }

        rs.close();
        stmt.close();

        return resgate;
    }

    // Selecionar -> buscarPorCodigo (coluna UNIQUE)
    public Resgate buscarPorCodigo(String codigoGerado) throws SQLException {
        Resgate resgate = null;

        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Select * From TB_RESGATE Where CODIGO_GERADO =?");
        stmt.setString(1, codigoGerado);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            resgate = montarResgate(rs);
        }

        rs.close();
        stmt.close();

        return resgate;
    }

    // Selecionar -> vouchers de um usuario
    public ArrayList<Resgate> buscarPorUsuario(int idUsuario) throws SQLException {
        ArrayList<Resgate> lista = new ArrayList<Resgate>();

        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Select * From TB_RESGATE Where ID_USUARIO =? Order By DATA_RESGATE Desc");
        stmt.setInt(1, idUsuario);

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            lista.add(montarResgate(rs));
        }

        rs.close();
        stmt.close();

        return lista;
    }

    // Marcar como utilizado
    public String marcarComoUtilizado(String codigoGerado) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Update TB_RESGATE set STATUS_VOUCHER ='UTILIZADO' "
                        + "Where CODIGO_GERADO =? And STATUS_VOUCHER ='DISPONIVEL'");
        stmt.setString(1, codigoGerado);

        int linhas = stmt.executeUpdate();
        stmt.close();

        if (linhas == 0) {
            return "Nenhum voucher disponivel com este codigo!";
        }

        return "Voucher Utilizado com Sucesso!!!";
    }

    // Rotina de manutencao - expira os vencidos que ainda estao DISPONIVEL
    public String expirarVencidos() throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Update TB_RESGATE set STATUS_VOUCHER ='EXPIRADO' "
                        + "Where STATUS_VOUCHER ='DISPONIVEL' And DATA_VALIDADE < Sysdate");

        int linhas = stmt.executeUpdate();
        stmt.close();

        return linhas + " voucher(s) marcado(s) como EXPIRADO.";
    }

    // Proximo ID disponivel
    public int proximoId() throws SQLException {
        int proximo = 1;

        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Select Nvl(Max(ID_RESGATE), 0) + 1 From TB_RESGATE");

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            proximo = rs.getInt(1);
        }

        rs.close();
        stmt.close();

        return proximo;
    }

    // metodo auxiliar
    private Resgate montarResgate(ResultSet rs) throws SQLException {
        Resgate resgate = new Resgate();
        resgate.setIdResgate(rs.getInt("ID_RESGATE"));
        resgate.setIdUsuario(rs.getInt("ID_USUARIO"));
        resgate.setIdVoucher(rs.getInt("ID_VOUCHER"));
        resgate.setIdAcao(rs.getInt("ID_ACAO"));   // NULL vira 0
        resgate.setCodigoGerado(rs.getString("CODIGO_GERADO"));
        resgate.setDataResgate(rs.getDate("DATA_RESGATE").toLocalDate());
        resgate.setDataValidade(rs.getDate("DATA_VALIDADE").toLocalDate());
        resgate.setStatusVoucher(rs.getString("STATUS_VOUCHER"));

        return resgate;
    }
}
