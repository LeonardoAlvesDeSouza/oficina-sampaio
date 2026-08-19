package br.com.oficinasampaio.financeiro.application;

import br.com.oficinasampaio.financeiro.domain.TipoMovimentacao;
import br.com.oficinasampaio.shared.domain.FormaPagamento;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Mesma proteção do módulo de ordens: a tradução é por {@code valueOf(name())} e
 * um descompasso entre domínio e apresentação só apareceria ao abrir a tela. Aqui
 * ele vira erro de build — e a forma de pagamento tem um agravante, o nome cru
 * também está escrito na restrição CHECK da migration.
 */
class ParidadeDeEnumsFinanceirosTest {

    @Test
    void formasDePagamentoDoDominioEDaApresentacaoTemOsMesmosNomesNaMesmaOrdem() {
        assertEquals(nomes(FormaPagamento.values()), nomes(FormaPagamentoView.values()));
    }

    @Test
    void tiposDeMovimentacaoDoDominioEDaApresentacaoTemOsMesmosNomesNaMesmaOrdem() {
        assertEquals(nomes(TipoMovimentacao.values()), nomes(TipoMovimentacaoView.values()));
    }

    @Test
    void traducaoDeIdaEVoltaDaFormaDePagamento() {
        for (var forma : FormaPagamento.values()) {
            assertEquals(forma, FormaPagamentoView.de(forma).paraDominio());
        }
    }

    private static List<String> nomes(Enum<?>[] valores) {
        return Arrays.stream(valores).map(Enum::name).toList();
    }
}
