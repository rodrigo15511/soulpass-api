package br.com.fiap.dao;

import br.com.fiap.conexoes.ConexaoFactory;
import br.com.fiap.entities.Voucher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * CRUD completo de TB_VOUCHER (catalogo de beneficios).
 */
public class VoucherDao {

    public Connection minhaConexao;

    public VoucherDao() throws SQLException, ClassNotFoundException {
        this.minhaConexao = new ConexaoFactory().conexao();
    }

    // Fechar a conexao - chamado pelo BO ao final de cada requisicao da API
    public void fecharConexao() throws SQLException {
        if (minhaConexao != null && !minhaConexao.isClosed()) {
            minhaConexao.close();
        }
    }

    // Inserir -> Insert
    public String inserir(Voucher voucher) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Insert Into TB_VOUCHER (ID_VOUCHER, TIPO_TRANSPORTE, CUSTO_PONTOS, "
                        + "VALOR_MONETARIO, DESCRICAO) values (?,?,?,?,?)");

        stmt.setInt(1, voucher.getIdVoucher());
        stmt.setString(2, voucher.getTipoTransporte().toUpperCase());
        stmt.setDouble(3, voucher.getCustoPontos());
        stmt.setDouble(4, voucher.getValorMonetario());
        stmt.setString(5, voucher.getDescricao());

        stmt.execute();
        stmt.close();

        return "Voucher Cadastrado com Sucesso!!!";
    }

    // Deletar -> Delete
    public String deletar(int idVoucher) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Delete From TB_VOUCHER where ID_VOUCHER =?");
        stmt.setInt(1, idVoucher);

        stmt.execute();
        stmt.close();

        return "Voucher Deletado com Sucesso!!!";
    }

    // Atualizar -> UpDate
    public String atualizar(Voucher voucher) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Update TB_VOUCHER set TIPO_TRANSPORTE =?, CUSTO_PONTOS =?, "
                        + "VALOR_MONETARIO =?, DESCRICAO =? where ID_VOUCHER =?");

        stmt.setString(1, voucher.getTipoTransporte().toUpperCase());
        stmt.setDouble(2, voucher.getCustoPontos());
        stmt.setDouble(3, voucher.getValorMonetario());
        stmt.setString(4, voucher.getDescricao());
        stmt.setInt(5, voucher.getIdVoucher());

        stmt.executeUpdate();
        stmt.close();

        return "Voucher Atualizado com Sucesso!!!";
    }

    // Selecionar -> Select (do mais barato para o mais caro)
    public ArrayList<Voucher> selecionar() throws SQLException {
        ArrayList<Voucher> lista = new ArrayList<Voucher>();

        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Select * From TB_VOUCHER Order By CUSTO_PONTOS, ID_VOUCHER");

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            lista.add(montarVoucher(rs));
        }

        rs.close();
        stmt.close();

        return lista;
    }

    // Selecionar -> buscarPorId
    public Voucher buscarPorId(int idVoucher) throws SQLException {
        Voucher voucher = null;

        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Select * From TB_VOUCHER Where ID_VOUCHER =?");
        stmt.setInt(1, idVoucher);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            voucher = montarVoucher(rs);
        }

        rs.close();
        stmt.close();

        return voucher;
    }

    // Proximo ID disponivel - o modelo nao possui sequences
    public int proximoId() throws SQLException {
        int proximo = 1;

        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Select Nvl(Max(ID_VOUCHER), 0) + 1 From TB_VOUCHER");

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            proximo = rs.getInt(1);
        }

        rs.close();
        stmt.close();

        return proximo;
    }

    // metodo auxiliar
    private Voucher montarVoucher(ResultSet rs) throws SQLException {
        Voucher voucher = new Voucher();
        voucher.setIdVoucher(rs.getInt("ID_VOUCHER"));
        voucher.setTipoTransporte(rs.getString("TIPO_TRANSPORTE"));
        voucher.setCustoPontos(rs.getDouble("CUSTO_PONTOS"));
        voucher.setValorMonetario(rs.getDouble("VALOR_MONETARIO"));
        voucher.setDescricao(rs.getString("DESCRICAO"));

        return voucher;
    }
}
