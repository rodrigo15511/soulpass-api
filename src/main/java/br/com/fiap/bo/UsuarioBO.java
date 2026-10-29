package br.com.fiap.bo;

import br.com.fiap.dao.UsuarioDao;
import br.com.fiap.entities.Nivel;
import br.com.fiap.entities.Usuario;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Regras de negocio do Usuario.
 * Fica entre o Resource (API) e o DAO (banco): valida os dados antes de gravar.
 * Cada metodo abre um DAO, usa e fecha a conexao no finally.
 */
public class UsuarioBO {

    // id do nivel inicial (TB_NIVEL) dado a todo usuario novo
    private static final int NIVEL_INICIAL = 1;

    // Selecionar todos
    public ArrayList<Usuario> selecionarBo() throws SQLException, ClassNotFoundException {
        UsuarioDao usuarioDao = new UsuarioDao();
        try {
            return usuarioDao.selecionar();
        } finally {
            usuarioDao.fecharConexao();
        }
    }

    // Buscar por id - retorna null se nao existir
    public Usuario buscarPorIdBo(int idUsuario) throws SQLException, ClassNotFoundException {
        UsuarioDao usuarioDao = new UsuarioDao();
        try {
            return usuarioDao.buscarPorId(idUsuario);
        } finally {
            usuarioDao.fecharConexao();
        }
    }

    // Inserir - valida, gera o id e define os valores iniciais
    public Usuario inserirBo(Usuario usuario) throws SQLException, ClassNotFoundException {
        validar(usuario);

        UsuarioDao usuarioDao = new UsuarioDao();
        try {
            usuario.setIdUsuario(usuarioDao.proximoId());
            usuario.setSaldoPontos(0);
            usuario.setPontosXp(0);
            usuario.setAtivo(true);
            usuario.setDataCadastro(LocalDate.now());

            Nivel nivel = new Nivel();
            nivel.setIdNivel(NIVEL_INICIAL);
            usuario.setNivel(nivel);

            usuarioDao.inserir(usuario);
            return usuarioDao.buscarPorId(usuario.getIdUsuario());
        } finally {
            usuarioDao.fecharConexao();
        }
    }

    // Atualizar - retorna null se o usuario nao existir
    public Usuario atualizarBo(int idUsuario, Usuario usuario) throws SQLException, ClassNotFoundException {
        validar(usuario);

        UsuarioDao usuarioDao = new UsuarioDao();
        try {
            Usuario existente = usuarioDao.buscarPorId(idUsuario);
            if (existente == null) {
                return null;
            }

            // pontos, nivel e data nao sao alterados por aqui - mudam pelas regras de pontuacao
            usuario.setIdUsuario(idUsuario);
            usuario.setSaldoPontos(existente.getSaldoPontos());
            usuario.setPontosXp(existente.getPontosXp());
            usuario.setNivel(existente.getNivel());
            usuario.setDataCadastro(existente.getDataCadastro());

            usuarioDao.atualizar(usuario);
            return usuarioDao.buscarPorId(idUsuario);
        } finally {
            usuarioDao.fecharConexao();
        }
    }

    // Deletar - retorna false se o usuario nao existir
    public boolean deletarBo(int idUsuario) throws SQLException, ClassNotFoundException {
        UsuarioDao usuarioDao = new UsuarioDao();
        try {
            if (usuarioDao.buscarPorId(idUsuario) == null) {
                return false;
            }
            usuarioDao.deletar(idUsuario);
            return true;
        } finally {
            usuarioDao.fecharConexao();
        }
    }

    // validar - regras de entrada; lanca IllegalArgumentException com a mensagem do erro
    private void validar(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Dados do usuario nao informados.");
        }
        if (usuario.getNome() == null || usuario.getNome().trim().length() < 3) {
            throw new IllegalArgumentException("Nome deve ter pelo menos 3 caracteres.");
        }
        if (!usuario.validarCpf()) {
            throw new IllegalArgumentException("CPF invalido! Informe 11 digitos.");
        }
        if (!usuario.validarEmail()) {
            throw new IllegalArgumentException("E-mail invalido!");
        }
    }
}
