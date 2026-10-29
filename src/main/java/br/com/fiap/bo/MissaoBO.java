package br.com.fiap.bo;

import br.com.fiap.dao.MissaoDao;
import br.com.fiap.entities.Missao;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Regras de negocio da Missao.
 * Fica entre o Resource (API) e o DAO (banco): valida os dados antes de gravar.
 * Cada metodo abre um DAO, usa e fecha a conexao no finally.
 */
public class MissaoBO {

    // Selecionar todas
    public ArrayList<Missao> selecionarBo() throws SQLException, ClassNotFoundException {
        MissaoDao missaoDao = new MissaoDao();
        try {
            return missaoDao.selecionar();
        } finally {
            missaoDao.fecharConexao();
        }
    }

    // Selecionar apenas as missoes ativas
    public ArrayList<Missao> selecionarAtivasBo() throws SQLException, ClassNotFoundException {
        MissaoDao missaoDao = new MissaoDao();
        try {
            return missaoDao.selecionarAtivas();
        } finally {
            missaoDao.fecharConexao();
        }
    }

    // Buscar missoes ativas de um tipo (ex.: BICICLETA)
    public ArrayList<Missao> buscarPorTipoBo(String tipoMissao) throws SQLException, ClassNotFoundException {
        MissaoDao missaoDao = new MissaoDao();
        try {
            return missaoDao.buscarPorTipo(tipoMissao.toUpperCase());
        } finally {
            missaoDao.fecharConexao();
        }
    }

    // Buscar por id - retorna null se nao existir
    public Missao buscarPorIdBo(int idMissao) throws SQLException, ClassNotFoundException {
        MissaoDao missaoDao = new MissaoDao();
        try {
            return missaoDao.buscarPorId(idMissao);
        } finally {
            missaoDao.fecharConexao();
        }
    }

    // Inserir - valida e gera o id
    public Missao inserirBo(Missao missao) throws SQLException, ClassNotFoundException {
        validar(missao);

        MissaoDao missaoDao = new MissaoDao();
        try {
            missao.setIdMissao(missaoDao.proximoId());
            missaoDao.inserir(missao);
            return missaoDao.buscarPorId(missao.getIdMissao());
        } finally {
            missaoDao.fecharConexao();
        }
    }

    // Atualizar - retorna null se a missao nao existir
    public Missao atualizarBo(int idMissao, Missao missao) throws SQLException, ClassNotFoundException {
        validar(missao);

        MissaoDao missaoDao = new MissaoDao();
        try {
            if (missaoDao.buscarPorId(idMissao) == null) {
                return null;
            }

            missao.setIdMissao(idMissao);
            missaoDao.atualizar(missao);
            return missaoDao.buscarPorId(idMissao);
        } finally {
            missaoDao.fecharConexao();
        }
    }

    // Deletar - retorna false se a missao nao existir
    public boolean deletarBo(int idMissao) throws SQLException, ClassNotFoundException {
        MissaoDao missaoDao = new MissaoDao();
        try {
            if (missaoDao.buscarPorId(idMissao) == null) {
                return false;
            }
            missaoDao.deletar(idMissao);
            return true;
        } finally {
            missaoDao.fecharConexao();
        }
    }

    // validar - regras de entrada; lanca IllegalArgumentException com a mensagem do erro
    private void validar(Missao missao) {
        if (missao == null) {
            throw new IllegalArgumentException("Dados da missao nao informados.");
        }
        if (missao.getTituloMissao() == null || missao.getTituloMissao().trim().isEmpty()) {
            throw new IllegalArgumentException("Titulo da missao e obrigatorio.");
        }
        if (missao.getPontoBase() <= 0) {
            throw new IllegalArgumentException("Ponto base deve ser maior que zero.");
        }
        // o DAO so devolve um texto quando o tipo e invalido - por isso a checagem fica aqui
        if (!missao.tipoValido()) {
            throw new IllegalArgumentException("Tipo de missao invalido! Use: TRANSPORTE_PUBLICO, "
                    + "CAMINHADA, BICICLETA, DESAFIO_SEMANAL ou DESAFIO_COLETIVO.");
        }
    }
}
