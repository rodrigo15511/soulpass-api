package br.com.fiap.dao;

import br.com.fiap.conexoes.ConexaoFactory;
import br.com.fiap.entities.AcaoRealizada;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * CRUD completo de TB_ACAO_REALIZADA.
 */
public class AcaoRealizadaDao {

    public Connection minhaConexao;

    public AcaoRealizadaDao() throws SQLException, ClassNotFoundException {
        this.minhaConexao = new ConexaoFactory().conexao();
    }

    // Fechar a conexao - chamado pelo BO ao final de cada requisicao da API
    public void fecharConexao() throws SQLException {
        if (minhaConexao != null && !minhaConexao.isClosed()) {
            minhaConexao.close();
        }
    }

    // Inserir -> Insert
    public String inserir(AcaoRealizada acao) throws SQLException {
        if (!acao.metodoValido()) {
            return "Metodo de validacao invalido! Use CHECKIN, CODIGO ou QRCODE.";
        }

        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Insert Into TB_ACAO_REALIZADA (ID_ACAO, ID_USUARIO, ID_MISSAO, "
                        + "METODO_VALIDACAO, CODIGO_VALIDACAO, DATA_CONCLUSAO, "
                        + "STATUS_APROVACAO, PONTOS_CONQUISTADOS) values (?,?,?,?,?,?,?,?)");

        stmt.setInt(1, acao.getIdAcao());
        stmt.setInt(2, acao.getIdUsuario());
        stmt.setInt(3, acao.getIdMissao());
        stmt.setString(4, acao.getMetodoValidacao().toUpperCase());
        stmt.setString(5, acao.getCodigoValidacao());
        stmt.setDate(6, Date.valueOf(acao.getDataConclusao()));
        stmt.setString(7, acao.getStatusAprovacao().toUpperCase());
        stmt.setDouble(8, acao.getPontosConquistados());

        stmt.execute();
        stmt.close();

        return "Acao Registrada com Sucesso!!!";
    }

    // Deletar -> Delete
    public String deletar(int idAcao) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Delete From TB_ACAO_REALIZADA where ID_ACAO =?");
        stmt.setInt(1, idAcao);

        stmt.execute();
        stmt.close();

        return "Acao Deletada com Sucesso!!!";
    }

    // Atualizar -> UpDate
    public String atualizar(AcaoRealizada acao) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Update TB_ACAO_REALIZADA set ID_USUARIO =?, ID_MISSAO =?, METODO_VALIDACAO =?, "
                        + "CODIGO_VALIDACAO =?, DATA_CONCLUSAO =?, STATUS_APROVACAO =?, "
                        + "PONTOS_CONQUISTADOS =? where ID_ACAO =?");

        stmt.setInt(1, acao.getIdUsuario());
        stmt.setInt(2, acao.getIdMissao());
        stmt.setString(3, acao.getMetodoValidacao().toUpperCase());
        stmt.setString(4, acao.getCodigoValidacao());
        stmt.setDate(5, Date.valueOf(acao.getDataConclusao()));
        stmt.setString(6, acao.getStatusAprovacao().toUpperCase());
        stmt.setDouble(7, acao.getPontosConquistados());
        stmt.setInt(8, acao.getIdAcao());

        stmt.executeUpdate();
        stmt.close();

        return "Acao Atualizada com Sucesso!!!";
    }

    // Selecionar -> Select
    public ArrayList<AcaoRealizada> selecionar() throws SQLException {
        ArrayList<AcaoRealizada> lista = new ArrayList<AcaoRealizada>();

        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Select * From TB_ACAO_REALIZADA Order By ID_ACAO");

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            lista.add(montarAcao(rs));
        }

        rs.close();
        stmt.close();

        return lista;
    }

    // Selecionar -> buscarPorId
    public AcaoRealizada buscarPorId(int idAcao) throws SQLException {
        AcaoRealizada acao = null;

        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Select * From TB_ACAO_REALIZADA Where ID_ACAO =?");
        stmt.setInt(1, idAcao);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            acao = montarAcao(rs);
        }

        rs.close();
        stmt.close();

        return acao;
    }

    // Selecionar -> historico de um usuario
    public ArrayList<AcaoRealizada> buscarPorUsuario(int idUsuario) throws SQLException {
        ArrayList<AcaoRealizada> lista = new ArrayList<AcaoRealizada>();

        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Select * From TB_ACAO_REALIZADA Where ID_USUARIO =? Order By DATA_CONCLUSAO Desc");
        stmt.setInt(1, idUsuario);

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            lista.add(montarAcao(rs));
        }

        rs.close();
        stmt.close();

        return lista;
    }

    // Selecionar -> acoes aguardando validacao
    public ArrayList<AcaoRealizada> selecionarPendentes() throws SQLException {
        ArrayList<AcaoRealizada> lista = new ArrayList<AcaoRealizada>();

        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Select * From TB_ACAO_REALIZADA Where STATUS_APROVACAO ='PENDENTE' "
                        + "Order By DATA_CONCLUSAO");

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            lista.add(montarAcao(rs));
        }

        rs.close();
        stmt.close();

        return lista;
    }

    // Gravar o resultado da validacao
    public String atualizarStatus(int idAcao, String status, double pontos) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Update TB_ACAO_REALIZADA set STATUS_APROVACAO =?, PONTOS_CONQUISTADOS =? "
                        + "where ID_ACAO =?");

        stmt.setString(1, status.toUpperCase());
        stmt.setDouble(2, pontos);
        stmt.setInt(3, idAcao);

        stmt.executeUpdate();
        stmt.close();

        return "Status da Acao Atualizado com Sucesso!!!";
    }

    // Total de missoes aprovadas de um usuario
    public int contarAprovadasPorUsuario(int idUsuario) throws SQLException {
        int total = 0;

        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Select Count(*) From TB_ACAO_REALIZADA "
                        + "Where ID_USUARIO =? And STATUS_APROVACAO ='APROVADO'");
        stmt.setInt(1, idUsuario);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            total = rs.getInt(1);
        }

        rs.close();
        stmt.close();

        return total;
    }

    // Proximo ID disponivel
    public int proximoId() throws SQLException {
        int proximo = 1;

        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Select Nvl(Max(ID_ACAO), 0) + 1 From TB_ACAO_REALIZADA");

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            proximo = rs.getInt(1);
        }

        rs.close();
        stmt.close();

        return proximo;
    }

    // metodo auxiliar
    private AcaoRealizada montarAcao(ResultSet rs) throws SQLException {
        AcaoRealizada acao = new AcaoRealizada();
        acao.setIdAcao(rs.getInt("ID_ACAO"));
        acao.setIdUsuario(rs.getInt("ID_USUARIO"));
        acao.setIdMissao(rs.getInt("ID_MISSAO"));
        acao.setMetodoValidacao(rs.getString("METODO_VALIDACAO"));
        acao.setCodigoValidacao(rs.getString("CODIGO_VALIDACAO"));
        acao.setDataConclusao(rs.getDate("DATA_CONCLUSAO").toLocalDate());
        acao.setStatusAprovacao(rs.getString("STATUS_APROVACAO"));
        acao.setPontosConquistados(rs.getDouble("PONTOS_CONQUISTADOS"));

        return acao;
    }
}
