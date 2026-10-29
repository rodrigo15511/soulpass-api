package br.com.fiap.main;

import br.com.fiap.entities.AcaoRealizada;
import br.com.fiap.entities.Conquista;
import br.com.fiap.entities.DesafioColetivo;
import br.com.fiap.entities.InteracaoChatbot;
import br.com.fiap.entities.Missao;
import br.com.fiap.entities.Nivel;
import br.com.fiap.entities.Resgate;
import br.com.fiap.entities.Usuario;
import br.com.fiap.entities.Voucher;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Teste do modelo da SoulPass 2.0 - roda em memoria, sem banco de dados.
 * Fluxo: realizar -> validar -> pontuar -> evoluir -> participar -> resgatar.
 */
public class TesteModelo {

    public static void main(String[] args) {

        System.out.println("===== SOULPASS 2.0 - TESTE DO MODELO =====");

        // ---------- Niveis (viriam de TB_NIVEL) ----------
        Nivel iniciante = new Nivel(1, "Iniciante", 1, 0, "Comecando na plataforma");
        Nivel explorador = new Nivel(2, "Explorador", 2, 500, "Ja concluiu varias missoes");
        Nivel mobilizador = new Nivel(3, "Mobilizador", 3, 1500, "Participa de desafios coletivos");

        // ---------- 1) Cadastro ----------
        System.out.println("\n--- 1) CADASTRO ---");

        Usuario bruna = new Usuario(1, "Bruna Terra Costa", "123.456.789-01",
                "bruna@soulpass.com.br", 0, 0, iniciante, LocalDate.now(), true);

        System.out.println("CPF valido? " + bruna.validarCpf());
        System.out.println("E-mail valido? " + bruna.validarEmail());
        System.out.println(bruna);

        // ---------- 2) Missao ----------
        System.out.println("\n--- 2) MISSAO ---");

        Missao pedalar = new Missao(10, "Pedalar ate o trabalho",
                Missao.TIPO_BICICLETA, "Trajeto de ida ou volta em bicicleta", 200, true);

        System.out.println(pedalar);

        // ---------- 3) Acao pendente ----------
        System.out.println("\n--- 3) ACAO REALIZADA (PENDENTE) ---");

        AcaoRealizada acao = new AcaoRealizada();
        acao.setIdAcao(100);
        acao.setIdUsuario(bruna.getIdUsuario());
        acao.setIdMissao(pedalar.getIdMissao());
        acao.setMetodoValidacao(AcaoRealizada.METODO_QRCODE);
        acao.setCodigoValidacao(AcaoRealizada.gerarCodigoValidacao(10, AcaoRealizada.METODO_QRCODE));

        System.out.println("Status inicial: " + acao.getStatusAprovacao());
        System.out.println("Codigo do parceiro: " + acao.getCodigoValidacao());

        // ---------- 4) Validacao ----------
        System.out.println("\n--- 4) VALIDACAO ---");

        AcaoRealizada tentativa = new AcaoRealizada();
        tentativa.setIdAcao(101);
        tentativa.setIdMissao(10);
        tentativa.setMetodoValidacao(AcaoRealizada.METODO_QRCODE);
        tentativa.setCodigoValidacao("QRCODE-10-111111");

        System.out.println(tentativa.aprovar("codigo-errado", pedalar));
        System.out.println("Status apos erro: " + tentativa.getStatusAprovacao());

        System.out.println(acao.aprovar(acao.getCodigoValidacao(), pedalar));
        System.out.println(acao.aprovar(acao.getCodigoValidacao(), pedalar));

        // ---------- 5) Credito e nivel ----------
        System.out.println("\n--- 5) CREDITO DE PONTOS ---");

        System.out.println(bruna.creditarPontos(acao.getPontosConquistados(),
                "Missao: " + pedalar.getTituloMissao()));
        System.out.println(bruna.creditarPontos(200, "Missao: Metro por 5 dias"));

        if (bruna.subiuDeNivel(explorador)) {
            System.out.println("\nSubiu para o nivel " + bruna.getNivel().getNomeNivel() + "!");
        }

        System.out.println(bruna.getProgressoFormatado(mobilizador));

        // ---------- 6) Desafio coletivo ----------
        System.out.println("\n--- 6) DESAFIO COLETIVO ---");

        Missao missaoColetiva = new Missao(20, "Semana sem carro",
                Missao.TIPO_DESAFIO_COLETIVO, "Desafio em grupo", 300, true);

        DesafioColetivo desafio = new DesafioColetivo(30, 20, "Semana sem carro",
                1000, LocalDate.now().minusDays(1), LocalDate.now().plusDays(6),
                DesafioColetivo.STATUS_EM_ANDAMENTO);

        Usuario rodrigo = new Usuario(2, "Rodrigo Terra Costa", "98765432100",
                "rodrigo@soulpass.com.br", 300, 800, explorador, LocalDate.now(), true);
        Usuario ana = new Usuario(3, "Ana Souza", "45678912300",
                "ana@soulpass.com.br", 500, 1600, mobilizador, LocalDate.now(), true);

        // lista de objetos Usuario - necessaria para creditar a recompensa
        ArrayList<Usuario> inscritos = new ArrayList<Usuario>();
        inscritos.add(bruna);
        inscritos.add(rodrigo);
        inscritos.add(ana);

        System.out.println(desafio.inscrever(bruna));
        System.out.println(desafio.inscrever(bruna));
        System.out.println(desafio.registrarContribuicao(1, 100));
        System.out.println(desafio.inscrever(rodrigo));
        System.out.println(desafio.inscrever(ana));
        System.out.println(desafio.registrarContribuicao(1, 400));
        System.out.println(desafio.registrarContribuicao(2, 300));
        System.out.println(desafio.distribuirRecompensa(missaoColetiva, inscritos));
        System.out.println(desafio.registrarContribuicao(3, 400));
        System.out.println(desafio.distribuirRecompensa(missaoColetiva, inscritos));
        System.out.println(desafio);

        // com a recompensa creditada, o XP muda e o nivel e reavaliado
        System.out.println("\nXP da Bruna apos a recompensa: " + bruna.getPontosXp());
        if (bruna.subiuDeNivel(mobilizador)) {
            System.out.println("Subiu para o nivel " + bruna.getNivel().getNomeNivel() + "!");
        }

        // ---------- 7) Catalogo de vouchers ----------
        System.out.println("\n--- 7) CATALOGO DE VOUCHERS ---");

        Voucher bilhete = new Voucher(1, Voucher.TIPO_ONIBUS, 500, 25.00, "Recarga de R$ 25 no Bilhete Unico");
        Voucher passe = new Voucher(2, Voucher.TIPO_METRO, 5000, 250.00, "Passe mensal integrado");

        System.out.println(bilhete);
        System.out.println("Acessivel para Bruna? " + bilhete.acessivelPara(bruna));
        System.out.println("Passe mensal acessivel? " + passe.acessivelPara(bruna));

        // ---------- 8) Resgate ----------
        System.out.println("\n--- 8) RESGATE ---");

        System.out.println("Saldo antes: " + bruna.getSaldoPontos());
        System.out.println(bruna.debitarPontos(passe.getCustoPontos(), "Passe mensal"));

        if (bruna.podeResgatar(bilhete)) {
            System.out.println(bruna.debitarPontos(bilhete.getCustoPontos(),
                    bilhete.getTipoTransporte()));

            Resgate resgate = new Resgate();
            resgate.setIdResgate(200);
            resgate.setIdUsuario(bruna.getIdUsuario());
            resgate.setIdVoucher(bilhete.getIdVoucher());
            resgate.setIdAcao(acao.getIdAcao());
            resgate.setCodigoGerado(Resgate.gerarCodigo(1, 1));

            System.out.println(resgate);
            System.out.println(resgate.utilizar());
            System.out.println(resgate.utilizar());
        }

        System.out.println("\nXP preservado apos o resgate: " + bruna.getPontosXp());

        // ---------- 9) Resgate vencido ----------
        System.out.println("\n--- 9) RESGATE VENCIDO ---");

        Resgate antigo = new Resgate(201, 1, 1, 100, "SP-ANTIGO",
                LocalDate.now().minusDays(120), LocalDate.now().minusDays(30),
                Resgate.STATUS_DISPONIVEL);

        System.out.println("Valido? " + antigo.estaValido());
        System.out.println(antigo.utilizar());

        // ---------- 10) Conquistas ----------
        System.out.println("\n--- 10) CONQUISTAS ---");

        Conquista primeiros1000 = new Conquista(1, "Mil Pontos",
                "Acumulou 1000 pontos XP", "XP:>=:1000", 100);
        Conquista nivel3 = new Conquista(2, "Mobilizador",
                "Alcancou o terceiro nivel", "NIVEL:>=:3", 200);

        System.out.println(primeiros1000.getNomeConquista() + " desbloqueada? "
                + primeiros1000.atendidaPor(bruna));
        System.out.println(nivel3.getNomeConquista() + " desbloqueada? "
                + nivel3.atendidaPor(bruna));

        // ---------- 11) Chatbot ----------
        System.out.println("\n--- 11) CHATBOT ---");

        InteracaoChatbot chat = new InteracaoChatbot();
        chat.setIdInteracao(1);
        chat.setIdUsuario(1);

        System.out.println(chat.registrar("Quantos pontos preciso para um voucher?",
                "O Bilhete Unico custa 500 pontos."));
        System.out.println("Categoria detectada: " + chat.getCategoria());

        System.out.println("\n===== FIM DO TESTE =====");
    }
}
