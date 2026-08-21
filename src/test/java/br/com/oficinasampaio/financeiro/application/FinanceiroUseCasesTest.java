package br.com.oficinasampaio.financeiro.application;

import br.com.oficinasampaio.financeiro.domain.MovimentacaoFinanceira;
import br.com.oficinasampaio.financeiro.domain.MovimentacaoFinanceiraRepository;
import br.com.oficinasampaio.financeiro.domain.Pagamento;
import br.com.oficinasampaio.financeiro.domain.PagamentoRepository;
import br.com.oficinasampaio.financeiro.domain.PosicaoDeCaixa;
import br.com.oficinasampaio.financeiro.domain.TipoMovimentacao;
import br.com.oficinasampaio.shared.domain.FormaPagamento;
import br.com.oficinasampaio.shared.domain.Periodo;
import br.com.oficinasampaio.shared.domain.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceiroUseCasesTest {

    private static final Instant INSTANTE = Instant.parse("2026-08-12T18:30:00Z");
    private static final Clock RELOGIO = Clock.fixed(INSTANTE, ZoneOffset.UTC);

    @Test
    void pagamentoLancadoViraEntradaNoCaixa() {
        var pagamentos = new PagamentoRepositoryEmMemoria();
        var movimentacoes = new MovimentacaoFinanceiraRepositoryEmMemoria();
        var ordemServicoId = UUID.fromString("1a2b3c4d-0000-0000-0000-000000000000");

        var pagamento = new RegistrarPagamento(pagamentos, movimentacoes).executar(
                new RegistrarPagamentoCommand(
                        ordemServicoId, UUID.randomUUID(), FormaPagamento.PIX,
                        new BigDecimal("300.00"), INSTANTE
                )
        );

        var caixa = new ConsultarCaixa(movimentacoes).executar();
        assertAll(
                () -> assertEquals(FormaPagamentoView.PIX, pagamento.forma()),
                () -> assertEquals(new BigDecimal("300.00"), pagamento.valor()),
                () -> assertEquals(1, caixa.movimentacoes().size()),
                () -> assertEquals(TipoMovimentacaoView.ENTRADA, caixa.movimentacoes().getFirst().tipo()),
                () -> assertEquals(
                        "Pagamento da OS 1a2b3c4d",
                        caixa.movimentacoes().getFirst().descricao()
                ),
                () -> assertEquals(new BigDecimal("300.00"), caixa.entradas()),
                () -> assertEquals(new BigDecimal("0.00"), caixa.saidas()),
                () -> assertEquals(new BigDecimal("300.00"), caixa.saldo())
        );
    }

    /**
     * A ordem já barra o segundo pagamento, mas o caixa não confia nisso: ele é
     * chamado por evento e precisa recusar por conta própria.
     */
    @Test
    void recusaSegundoPagamentoDaMesmaOrdemESemMexerNoCaixa() {
        var pagamentos = new PagamentoRepositoryEmMemoria();
        var movimentacoes = new MovimentacaoFinanceiraRepositoryEmMemoria();
        var registrar = new RegistrarPagamento(pagamentos, movimentacoes);
        var ordemServicoId = UUID.randomUUID();
        var comando = new RegistrarPagamentoCommand(
                ordemServicoId, UUID.randomUUID(), FormaPagamento.DINHEIRO,
                new BigDecimal("150.00"), INSTANTE
        );
        registrar.executar(comando);

        var erro = assertThrows(RegraNegocioException.class, () -> registrar.executar(comando));

        var caixa = new ConsultarCaixa(movimentacoes).executar();
        assertAll(
                () -> assertEquals(
                        "Pagamento já registrado para esta ordem de serviço",
                        erro.getMessage()
                ),
                () -> assertEquals(1, new ListarPagamentos(pagamentos).executar().size()),
                () -> assertEquals(1, caixa.movimentacoes().size()),
                () -> assertEquals(new BigDecimal("150.00"), caixa.entradas())
        );
    }

    @Test
    void saidaLancadaAMaoEntraNoExtratoEDerrubaOSaldo() {
        var movimentacoes = new MovimentacaoFinanceiraRepositoryEmMemoria();
        new RegistrarPagamento(new PagamentoRepositoryEmMemoria(), movimentacoes).executar(
                new RegistrarPagamentoCommand(
                        UUID.randomUUID(), UUID.randomUUID(), FormaPagamento.PIX,
                        new BigDecimal("300.00"), INSTANTE
                )
        );

        var saida = new RegistrarSaida(movimentacoes, RELOGIO).executar(
                new RegistrarSaidaCommand("Jogo de pastilhas no fornecedor", new BigDecimal("180.50"))
        );

        var caixa = new ConsultarCaixa(movimentacoes).executar();
        assertAll(
                () -> assertEquals(TipoMovimentacaoView.SAIDA, saida.tipo()),
                () -> assertEquals(INSTANTE, saida.ocorridaEm()),
                () -> assertEquals(2, caixa.movimentacoes().size()),
                () -> assertEquals(new BigDecimal("300.00"), caixa.entradas()),
                () -> assertEquals(new BigDecimal("180.50"), caixa.saidas()),
                () -> assertEquals(new BigDecimal("119.50"), caixa.saldo())
        );
    }

    @Test
    void caixaSemMovimentacaoAbreEmZero() {
        var caixa = new ConsultarCaixa(new MovimentacaoFinanceiraRepositoryEmMemoria()).executar();

        assertAll(
                () -> assertEquals(new BigDecimal("0.00"), caixa.entradas()),
                () -> assertEquals(new BigDecimal("0.00"), caixa.saidas()),
                () -> assertEquals(new BigDecimal("0.00"), caixa.saldo()),
                () -> assertTrue(caixa.movimentacoes().isEmpty())
        );
    }

    /**
     * O fechamento do mês não pode arrastar o mês anterior. Aqui as duas saídas são
     * lançadas com relógios diferentes e só uma está na janela pedida.
     */
    @Test
    void caixaDoPeriodoDeixaDeForaOQueAconteceuFora() {
        var movimentacoes = new MovimentacaoFinanceiraRepositoryEmMemoria();
        var mesPassado = Instant.parse("2026-07-28T13:00:00Z");
        new RegistrarSaida(movimentacoes, Clock.fixed(mesPassado, ZoneOffset.UTC)).executar(
                new RegistrarSaidaCommand("Conta de luz de julho", new BigDecimal("240.00"))
        );
        new RegistrarSaida(movimentacoes, RELOGIO).executar(
                new RegistrarSaidaCommand("Jogo de pastilhas", new BigDecimal("180.50"))
        );
        var agosto = new Periodo(
                Instant.parse("2026-08-01T03:00:00Z"), Instant.parse("2026-09-01T02:59:59.999Z")
        );

        var caixa = new ConsultarCaixa(movimentacoes).executar(agosto);
        var completo = new ConsultarCaixa(movimentacoes).executar();

        assertAll(
                () -> assertEquals(1, caixa.movimentacoes().size()),
                () -> assertEquals("Jogo de pastilhas", caixa.movimentacoes().getFirst().descricao()),
                () -> assertEquals(new BigDecimal("180.50"), caixa.saidas()),
                () -> assertEquals(new BigDecimal("-180.50"), caixa.saldo()),
                () -> assertEquals(2, completo.movimentacoes().size()),
                () -> assertEquals(new BigDecimal("420.50"), completo.saidas())
        );
    }

    @Test
    void faturamentoDoPeriodoSoTrazOsRecebimentosDaJanela() {
        var pagamentos = new PagamentoRepositoryEmMemoria();
        var registrar = new RegistrarPagamento(
                pagamentos, new MovimentacaoFinanceiraRepositoryEmMemoria()
        );
        registrar.executar(new RegistrarPagamentoCommand(
                UUID.randomUUID(), UUID.randomUUID(), FormaPagamento.PIX,
                new BigDecimal("300.00"), Instant.parse("2026-07-15T12:00:00Z")
        ));
        registrar.executar(new RegistrarPagamentoCommand(
                UUID.randomUUID(), UUID.randomUUID(), FormaPagamento.DINHEIRO,
                new BigDecimal("150.00"), INSTANTE
        ));
        var agosto = new Periodo(
                Instant.parse("2026-08-01T03:00:00Z"), Instant.parse("2026-09-01T02:59:59.999Z")
        );

        var doPeriodo = new ListarPagamentos(pagamentos).executar(agosto);

        assertAll(
                () -> assertEquals(1, doPeriodo.size()),
                () -> assertEquals(new BigDecimal("150.00"), doPeriodo.getFirst().valor()),
                () -> assertEquals(2, new ListarPagamentos(pagamentos).executar().size())
        );
    }

    @Test
    void consultaPublicaDevolveOPagamentoDaOrdemParaOutroModulo() {
        var pagamentos = new PagamentoRepositoryEmMemoria();
        var ordemServicoId = UUID.randomUUID();
        new RegistrarPagamento(pagamentos, new MovimentacaoFinanceiraRepositoryEmMemoria()).executar(
                new RegistrarPagamentoCommand(
                        ordemServicoId, UUID.randomUUID(), FormaPagamento.CARTAO_CREDITO,
                        new BigDecimal("420.00"), INSTANTE
                )
        );
        var queries = new PagamentoQueryService(pagamentos);

        var encontrado = queries.buscarPorOrdemServico(ordemServicoId);
        var inexistente = queries.buscarPorOrdemServico(UUID.randomUUID());

        assertAll(
                () -> assertTrue(encontrado.isPresent()),
                () -> assertEquals(FormaPagamentoView.CARTAO_CREDITO, encontrado.orElseThrow().forma()),
                () -> assertEquals(new BigDecimal("420.00"), encontrado.orElseThrow().valor()),
                () -> assertEquals(INSTANTE, encontrado.orElseThrow().registradoEm()),
                () -> assertTrue(inexistente.isEmpty())
        );
    }

    /**
     * Repositórios em memória com a geração de id que o banco faria: a entrada do
     * caixa aponta para o pagamento gravado, e sem id isso não seria exercitado.
     */
    private static final class PagamentoRepositoryEmMemoria implements PagamentoRepository {

        private final Map<UUID, Pagamento> pagamentos = new LinkedHashMap<>();

        @Override
        public Pagamento salvar(Pagamento pagamento) {
            var id = pagamento.getId() != null
                    ? pagamento.getId()
                    : IdentificadorEmMemoria.atribuir(Pagamento.class, pagamento);
            pagamentos.put(id, pagamento);
            return pagamento;
        }

        @Override
        public boolean existePorOrdemServico(UUID ordemServicoId) {
            return buscarPorOrdemServico(ordemServicoId).isPresent();
        }

        @Override
        public Optional<Pagamento> buscarPorOrdemServico(UUID ordemServicoId) {
            return pagamentos.values().stream()
                    .filter(pagamento -> ordemServicoId.equals(pagamento.getOrdemServicoId()))
                    .findFirst();
        }

        @Override
        public List<Pagamento> listar() {
            return pagamentos.values().stream()
                    .sorted(Comparator.comparing(Pagamento::getRegistradoEm).reversed())
                    .toList();
        }

        @Override
        public List<Pagamento> listar(Periodo periodo) {
            return listar().stream()
                    .filter(pagamento -> dentro(periodo, pagamento.getRegistradoEm()))
                    .toList();
        }
    }

    private static final class MovimentacaoFinanceiraRepositoryEmMemoria
            implements MovimentacaoFinanceiraRepository {

        private static final BigDecimal ZERO_MONETARIO = new BigDecimal("0.00");

        private final List<MovimentacaoFinanceira> movimentacoes = new java.util.ArrayList<>();

        @Override
        public MovimentacaoFinanceira salvar(MovimentacaoFinanceira movimentacao) {
            if (movimentacao.getId() == null) {
                IdentificadorEmMemoria.atribuir(MovimentacaoFinanceira.class, movimentacao);
            }
            movimentacoes.add(movimentacao);
            return movimentacao;
        }

        @Override
        public List<MovimentacaoFinanceira> listar() {
            return movimentacoes.stream()
                    .sorted(Comparator.comparing(MovimentacaoFinanceira::getOcorridaEm).reversed())
                    .toList();
        }

        @Override
        public List<MovimentacaoFinanceira> listar(Periodo periodo) {
            return listar().stream()
                    .filter(movimentacao -> dentro(periodo, movimentacao.getOcorridaEm()))
                    .toList();
        }

        @Override
        public PosicaoDeCaixa posicao() {
            return posicaoDe(movimentacoes);
        }

        @Override
        public PosicaoDeCaixa posicao(Periodo periodo) {
            return posicaoDe(listar(periodo));
        }

        private static PosicaoDeCaixa posicaoDe(List<MovimentacaoFinanceira> movimentacoes) {
            return new PosicaoDeCaixa(
                    somar(movimentacoes, TipoMovimentacao.ENTRADA),
                    somar(movimentacoes, TipoMovimentacao.SAIDA)
            );
        }

        private static BigDecimal somar(
                List<MovimentacaoFinanceira> movimentacoes,
                TipoMovimentacao tipo
        ) {
            return movimentacoes.stream()
                    .filter(movimentacao -> movimentacao.getTipo() == tipo)
                    .map(MovimentacaoFinanceira::getValor)
                    .reduce(ZERO_MONETARIO, BigDecimal::add);
        }
    }

    private static boolean dentro(Periodo periodo, Instant instante) {
        return !instante.isBefore(periodo.inicio()) && !instante.isAfter(periodo.fim());
    }

    private static final class IdentificadorEmMemoria {

        private IdentificadorEmMemoria() {
        }

        static UUID atribuir(Class<?> tipo, Object entidade) {
            var id = UUID.randomUUID();
            try {
                campo(tipo).set(entidade, id);
            } catch (IllegalAccessException erro) {
                throw new IllegalStateException("Não foi possível atribuir o id", erro);
            }
            return id;
        }

        private static Field campo(Class<?> tipo) {
            try {
                var campo = tipo.getDeclaredField("id");
                campo.setAccessible(true);
                return campo;
            } catch (NoSuchFieldException erro) {
                throw new IllegalStateException("Campo id não encontrado em " + tipo, erro);
            }
        }
    }
}
