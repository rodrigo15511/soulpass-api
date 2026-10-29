package br.com.fiap.main;

import br.com.fiap.dao.AcaoRealizadaDao;
import br.com.fiap.dao.MissaoDao;
import br.com.fiap.dao.UsuarioDao;
import br.com.fiap.entities.AcaoRealizada;
import br.com.fiap.entities.Missao;
import br.com.fiap.entities.Nivel;
import br.com.fiap.entities.Usuario;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Classe de teste do CRUD - grava, le, atualiza e apaga no banco de verdade.
 * Rode o TesteConexao antes deste. Precisa das tabelas ja criadas.
 */
public class TesteCrud {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {

        System.out.println("===== SOULPASS 2.0 - TESTE DO CRUD =====");

        UsuarioDao usuarioDao = new UsuarioDao();
        MissaoDao missaoDao = new MissaoDao();
        AcaoRealizadaDao acaoDao = new AcaoRealizadaDao();

        // ---------- CREATE: missao ----------
        System.out.println("\n--- 1) CREATE: MISSAO ---");

        int idMissao = missaoDao.proximoId();

        Missao missao = new Missao(idMissao, "Pedalar ate o trabalho",
                Missao.TIPO_BICICLETA, "Trajeto de ida ou volta em bicicleta", 200, true);

        System.out.println(missaoDao.inserir(missao));
        System.out.println("ID gerado: " + idMissao);

        // ---------- CREATE: usuario ----------
        System.out.println("\n--- 2) CREATE: USUARIO ---");

        // o nivel 1 (Iniciante) ja vem carregado pelo script do banco
        Nivel iniciante = new Nivel();
        iniciante.setIdNivel(1);

        int idUsuario = usuarioDao.proximoId();

        Usuario usuario = new Usuario(idUsuario, "Bruna Terra Costa",
                "123.456.789-0" + (idUsuario % 10),
                "bruna" + idUsuario + "@soulpass.com.br",
                0, 0, iniciante, LocalDate.now(), true);

        if (!usuario.validarCpf()) {
            System.out.println("CPF invalido! Cadastro abortado.");
            return;
        }

        System.out.println(usuarioDao.inserir(usuario));
        System.out.println("ID gerado: " + idUsuario);

        // ---------- CREATE: acao pendente ----------
        System.out.println("\n--- 3) CREATE: ACAO PENDENTE ---");

        int idAcao = acaoDao.proximoId();

        AcaoRealizada acao = new AcaoRealizada();
        acao.setIdAcao(idAcao);
        acao.setIdUsuario(idUsuario);
        acao.setIdMissao(idMissao);
        acao.setMetodoValidacao(AcaoRealizada.METODO_QRCODE);
        acao.setCodigoValidacao(AcaoRealizada.gerarCodigoValidacao(idMissao,
                AcaoRealizada.METODO_QRCODE));

        System.out.println(acaoDao.inserir(acao));
        System.out.println("Codigo do parceiro: " + acao.getCodigoValidacao());

        // ---------- UPDATE: aprovar a acao e creditar ----------
        System.out.println("\n--- 4) UPDATE: APROVAR E CREDITAR ---");

        Missao missaoBanco = missaoDao.buscarPorId(idMissao);

        System.out.println(acao.aprovar(acao.getCodigoValidacao(), missaoBanco));
        System.out.println(acaoDao.atualizarStatus(idAcao, acao.getStatusAprovacao(),
                acao.getPontosConquistados()));

        Usuario usuarioBanco = usuarioDao.buscarPorId(idUsuario);
        System.out.println(usuarioBanco.creditarPontos(acao.getPontosConquistados(),
                missaoBanco.getTituloMissao()));
        System.out.println(usuarioDao.atualizarPontos(idUsuario,
                usuarioBanco.getSaldoPontos(), usuarioBanco.getPontosXp()));

        // ---------- READ: conferir no banco ----------
        System.out.println("\n--- 5) READ: CONFERIR NO BANCO ---");

        Usuario conferencia = usuarioDao.buscarPorId(idUsuario);
        System.out.println(conferencia);
        System.out.println("\nMissoes aprovadas: " + acaoDao.contarAprovadasPorUsuario(idUsuario));

        // ---------- READ: listar tudo ----------
        System.out.println("\n--- 6) READ: LISTAGEM ---");

        ArrayList<Usuario> usuarios = usuarioDao.selecionar();
        System.out.println("Total de usuarios: " + usuarios.size());

        ArrayList<Missao> missoes = missaoDao.selecionarAtivas();
        System.out.println("Missoes ativas: " + missoes.size());

        ArrayList<AcaoRealizada> acoes = acaoDao.buscarPorUsuario(idUsuario);
        for (AcaoRealizada a : acoes) {
            System.out.println(a);
        }

        // ---------- UPDATE: alterar cadastro ----------
        System.out.println("\n--- 7) UPDATE: ALTERAR CADASTRO ---");

        conferencia.setEmail("bruna.terra" + idUsuario + "@soulpass.com.br");
        System.out.println(usuarioDao.atualizar(conferencia));
        System.out.println("E-mail no banco: " + usuarioDao.buscarPorId(idUsuario).getEmail());

        // ---------- DELETE ----------
        System.out.println("\n--- 8) DELETE ---");

        // a ordem importa: a acao depende do usuario e da missao (FK)
        System.out.println(acaoDao.deletar(idAcao));
        System.out.println(usuarioDao.deletar(idUsuario));
        System.out.println(missaoDao.deletar(idMissao));

        System.out.println("Usuario ainda existe? " + (usuarioDao.buscarPorId(idUsuario) != null));

        System.out.println("\n===== FIM DO TESTE DO CRUD =====");
    }
}
