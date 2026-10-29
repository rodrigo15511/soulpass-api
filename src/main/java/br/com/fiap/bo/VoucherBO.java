package br.com.fiap.bo;

import br.com.fiap.dao.VoucherDao;
import br.com.fiap.entities.Voucher;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Regras de negocio do Voucher (catalogo de beneficios).
 * Cada metodo abre um DAO, usa e fecha a conexao no finally.
 */
public class VoucherBO {

    // Selecionar todos
    public ArrayList<Voucher> selecionarBo() throws SQLException, ClassNotFoundException {
        VoucherDao voucherDao = new VoucherDao();
        try {
            return voucherDao.selecionar();
        } finally {
            voucherDao.fecharConexao();
        }
    }

    // Buscar por id - retorna null se nao existir
    public Voucher buscarPorIdBo(int idVoucher) throws SQLException, ClassNotFoundException {
        VoucherDao voucherDao = new VoucherDao();
        try {
            return voucherDao.buscarPorId(idVoucher);
        } finally {
            voucherDao.fecharConexao();
        }
    }

    // Inserir - valida e gera o id
    public Voucher inserirBo(Voucher voucher) throws SQLException, ClassNotFoundException {
        validar(voucher);

        VoucherDao voucherDao = new VoucherDao();
        try {
            voucher.setIdVoucher(voucherDao.proximoId());
            voucherDao.inserir(voucher);
            return voucherDao.buscarPorId(voucher.getIdVoucher());
        } finally {
            voucherDao.fecharConexao();
        }
    }

    // Atualizar - retorna null se o voucher nao existir
    public Voucher atualizarBo(int idVoucher, Voucher voucher) throws SQLException, ClassNotFoundException {
        validar(voucher);

        VoucherDao voucherDao = new VoucherDao();
        try {
            if (voucherDao.buscarPorId(idVoucher) == null) {
                return null;
            }

            voucher.setIdVoucher(idVoucher);
            voucherDao.atualizar(voucher);
            return voucherDao.buscarPorId(idVoucher);
        } finally {
            voucherDao.fecharConexao();
        }
    }

    // Deletar - retorna false se o voucher nao existir
    public boolean deletarBo(int idVoucher) throws SQLException, ClassNotFoundException {
        VoucherDao voucherDao = new VoucherDao();
        try {
            if (voucherDao.buscarPorId(idVoucher) == null) {
                return false;
            }
            voucherDao.deletar(idVoucher);
            return true;
        } finally {
            voucherDao.fecharConexao();
        }
    }

    // validar - mesmas regras dos CHECK de TB_VOUCHER, com mensagens claras
    private void validar(Voucher voucher) {
        if (voucher == null) {
            throw new IllegalArgumentException("Dados do voucher nao informados.");
        }
        if (!voucher.tipoValido()) {
            throw new IllegalArgumentException("Tipo de transporte invalido! Use: ONIBUS, METRO, TREM, "
                    + "BICICLETA ou APLICATIVO.");
        }
        if (voucher.getCustoPontos() <= 0) {
            throw new IllegalArgumentException("Custo em pontos deve ser maior que zero.");
        }
        if (voucher.getValorMonetario() <= 0) {
            throw new IllegalArgumentException("Valor monetario deve ser maior que zero.");
        }
    }
}
