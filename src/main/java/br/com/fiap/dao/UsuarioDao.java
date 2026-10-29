package br.com.fiap.dao;

import br.com.fiap.conexoes.ConexaoFactory;
import br.com.fiap.entities.Nivel;
import br.com.fiap.entities.Usuario;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * CRUD completo de TB_USUARIO.
 * O select traz o nivel junto (JOIN com TB_NIVEL) para montar o objeto completo.
 */
public class UsuarioDao {

    public Connection minhaConexao;

    public UsuarioDao() throws SQLException, ClassNotFoundException {
        this.minhaConexao = new ConexaoFactory().conexao();
    }

    // Fechar a conexao - chamado pelo BO ao final de cada requisicao da API
    public void fecharConexao() throws SQLException {
        if (minhaConexao != null && !minhaConexao.isClosed()) {
            minhaConexao.close();
        }
    }

    // Inserir -> Insert
    public String inserir(Usuario usuario) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Insert Into TB_USUARIO (ID_USUARIO, NOME, CPF, EMAIL, SALDO_PONTOS, "
                        + "PONTOS_XP, ID_NIVEL, DATA_CADASTRO, ATIVO) values (?,?,?,?,?,?,?,?,?)");

        stmt.setInt(1, usuario.getIdUsuario());
        stmt.setString(2, usuario.getNome());
        stmt.setString(3, usuario.getCpf());
        stmt.setString(4, usuario.getEmail());
        stmt.setDouble(5, usuario.getSaldoPontos());
        stmt.setDouble(6, usuario.getPontosXp());
        stmt.setInt(7, usuario.getNivel().getIdNivel());
        stmt.setDate(8, Date.valueOf(usuario.getDataCadastro()));
        stmt.setString(9, usuario.isAtivo() ? "S" : "N");

        stmt.execute();
        stmt.close();

        return "Usuario Cadastrado com Sucesso!!!";
    }

    // Deletar -> Delete
    public String deletar(int idUsuario) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Delete From TB_USUARIO where ID_USUARIO =?");
        stmt.setInt(1, idUsuario);

        stmt.execute();
        stmt.close();

        return "Usuario Deletado com Sucesso!!!";
    }

    // Atualizar -> UpDate
    public String atualizar(Usuario usuario) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Update TB_USUARIO set NOME =?, CPF =?, EMAIL =?, SALDO_PONTOS =?, "
                        + "PONTOS_XP =?, ID_NIVEL =?, ATIVO =? where ID_USUARIO =?");

        stmt.setString(1, usuario.getNome());
        stmt.setString(2, usuario.getCpf());
        stmt.setString(3, usuario.getEmail());
        stmt.setDouble(4, usuario.getSaldoPontos());
        stmt.setDouble(5, usuario.getPontosXp());
        stmt.setInt(6, usuario.getNivel().getIdNivel());
        stmt.setString(7, usuario.isAtivo() ? "S" : "N");
        stmt.setInt(8, usuario.getIdUsuario());

        stmt.executeUpdate();
        stmt.close();

        return "Usuario Atualizado com Sucesso!!!";
    }

    // Selecionar -> Select
    public ArrayList<Usuario> selecionar() throws SQLException {
        ArrayList<Usuario> listaUsuario = new ArrayList<Usuario>();

        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Select U.ID_USUARIO, U.NOME, U.CPF, U.EMAIL, U.SALDO_PONTOS, U.PONTOS_XP, "
                        + "U.DATA_CADASTRO, U.ATIVO, N.ID_NIVEL, N.NOME_NIVEL, N.NIVEL_ORDEM, "
                        + "N.PONTOS_XP_NECESSARIOS, N.DESCRICAO "
                        + "From TB_USUARIO U Inner Join TB_NIVEL N On U.ID_NIVEL = N.ID_NIVEL "
                        + "Order By U.ID_USUARIO");

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            listaUsuario.add(montarUsuario(rs));
        }

        rs.close();
        stmt.close();

        return listaUsuario;
    }

    // Selecionar -> buscarPorId
    public Usuario buscarPorId(int idUsuario) throws SQLException {
        Usuario usuario = null;

        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Select U.ID_USUARIO, U.NOME, U.CPF, U.EMAIL, U.SALDO_PONTOS, U.PONTOS_XP, "
                        + "U.DATA_CADASTRO, U.ATIVO, N.ID_NIVEL, N.NOME_NIVEL, N.NIVEL_ORDEM, "
                        + "N.PONTOS_XP_NECESSARIOS, N.DESCRICAO "
                        + "From TB_USUARIO U Inner Join TB_NIVEL N On U.ID_NIVEL = N.ID_NIVEL "
                        + "Where U.ID_USUARIO =?");
        stmt.setInt(1, idUsuario);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            usuario = montarUsuario(rs);
        }

        rs.close();
        stmt.close();

        return usuario;
    }

    // Atualizar apenas o saldo e o XP (usado apos aprovar acao ou resgatar voucher)
    public String atualizarPontos(int idUsuario, double saldoPontos, double pontosXp)
            throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Update TB_USUARIO set SALDO_PONTOS =?, PONTOS_XP =? where ID_USUARIO =?");

        stmt.setDouble(1, saldoPontos);
        stmt.setDouble(2, pontosXp);
        stmt.setInt(3, idUsuario);

        stmt.executeUpdate();
        stmt.close();

        return "Pontos Atualizados com Sucesso!!!";
    }

    // Promover o usuario para um novo nivel
    public String atualizarNivel(int idUsuario, int idNivel) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Update TB_USUARIO set ID_NIVEL =? where ID_USUARIO =?");

        stmt.setInt(1, idNivel);
        stmt.setInt(2, idUsuario);

        stmt.executeUpdate();
        stmt.close();

        return "Nivel Atualizado com Sucesso!!!";
    }

    // Proximo ID disponivel - o modelo nao possui sequences
    public int proximoId() throws SQLException {
        int proximo = 1;

        PreparedStatement stmt = minhaConexao.prepareStatement(
                "Select Nvl(Max(ID_USUARIO), 0) + 1 From TB_USUARIO");

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            proximo = rs.getInt(1);
        }

        rs.close();
        stmt.close();

        return proximo;
    }

    // metodo auxiliar - evita repetir o mapeamento em cada select
    private Usuario montarUsuario(ResultSet rs) throws SQLException {
        Nivel nivel = new Nivel(
                rs.getInt("ID_NIVEL"),
                rs.getString("NOME_NIVEL"),
                rs.getInt("NIVEL_ORDEM"),
                rs.getDouble("PONTOS_XP_NECESSARIOS"),
                rs.getString("DESCRICAO"));

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("ID_USUARIO"));
        usuario.setNome(rs.getString("NOME"));
        usuario.setCpf(rs.getString("CPF"));
        usuario.setEmail(rs.getString("EMAIL"));
        usuario.setSaldoPontos(rs.getDouble("SALDO_PONTOS"));
        usuario.setPontosXp(rs.getDouble("PONTOS_XP"));
        usuario.setDataCadastro(rs.getDate("DATA_CADASTRO").toLocalDate());
        usuario.setAtivo("S".equals(rs.getString("ATIVO")));
        usuario.setNivel(nivel);

        return usuario;
    }
}
