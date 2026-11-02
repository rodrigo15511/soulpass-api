package br.com.fiap.bo;

import br.com.fiap.dao.UsuarioDao;
import br.com.fiap.entities.Nivel;
import br.com.fiap.entities.Usuario;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Regras de negocio do Usuario.
 * Fica entre o Resource (API) e o DAO (banco): valida os dados antes de gravar.
 * Cada metodo abre um DAO, usa e fecha a conexao no finally.
 * A senha e gravada como hash BCrypt: nem quem acessa o banco ve a senha original.
 */
public class UsuarioBO {

    // id do nivel inicial (TB_NIVEL) dado a todo usuario novo
    private static final int NIVEL_INICIAL = 1;

    // BCrypt: gera um hash diferente a cada encode e confere com matches()
    private final BCryptPasswordEncoder criptografia = new BCryptPasswordEncoder();

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
        validarSenha(usuario.getSenha());

        UsuarioDao usuarioDao = new UsuarioDao();
        try {
            usuario.setIdUsuario(usuarioDao.proximoId());
            usuario.setSenha(criptografia.encode(usuario.getSenha()));
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

            // senha em branco no PUT = manter a atual; preenchida = trocar
            if (usuario.getSenha() == null || usuario.getSenha().isBlank()) {
                usuario.setSenha(existente.getSenha());
            } else {
                validarSenha(usuario.getSenha());
                usuario.setSenha(criptografia.encode(usuario.getSenha()));
            }

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

    // Autenticar - login por e-mail e senha; retorna null se nao conferir
    public Usuario autenticarBo(String email, String senha) throws SQLException, ClassNotFoundException {
        if (email == null || email.isBlank() || senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Informe e-mail e senha.");
        }

        UsuarioDao usuarioDao = new UsuarioDao();
        try {
            Usuario usuario = usuarioDao.buscarPorEmail(email);

            // mesma resposta para e-mail inexistente e senha errada: nao revela quais e-mails existem
            if (usuario == null || usuario.getSenha() == null
                    || !criptografia.matches(senha, usuario.getSenha())) {
                return null;
            }
            if (!usuario.isAtivo()) {
                throw new IllegalStateException("Usuario inativo.");
            }
            return usuario;
        } finally {
            usuarioDao.fecharConexao();
        }
    }

    // validarSenha - regra minima para o cadastro e a troca de senha
    private void validarSenha(String senha) {
        if (senha == null || senha.length() < 6) {
            throw new IllegalArgumentException("Senha deve ter pelo menos 6 caracteres.");
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
        // CPF e opcional; se vier preenchido, precisa ser valido. Vazio vira NULL (CPF e UNIQUE)
        if (usuario.getCpf() == null || usuario.getCpf().isBlank()) {
            usuario.setCpf(null);
        } else if (!usuario.validarCpf()) {
            throw new IllegalArgumentException("CPF invalido! Informe 11 digitos.");
        }
        if (!usuario.validarEmail()) {
            throw new IllegalArgumentException("E-mail invalido!");
        }
    }
}
