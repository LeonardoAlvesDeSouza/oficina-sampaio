package br.com.oficinasampaio.financeiro.presentation;

import java.math.BigDecimal;
import java.util.List;

/**
 * A tela de pagamentos tem duas metades e elas respondem perguntas diferentes:
 * quanto a oficina ainda tem para receber, e o que já entrou. Os dois totais
 * ficam no topo porque é o que se olha primeiro.
 */
public record PainelPagamentos(
        List<AReceberLinha> aReceber,
        List<PagamentoLinha> recebidos,
        BigDecimal totalAReceber,
        BigDecimal totalRecebido
) {

    private static final BigDecimal ZERO_MONETARIO = new BigDecimal("0.00");

    public static PainelPagamentos montar(
            List<AReceberLinha> aReceber,
            List<PagamentoLinha> recebidos
    ) {
        return new PainelPagamentos(
                aReceber,
                recebidos,
                somar(aReceber.stream().map(AReceberLinha::total).toList()),
                somar(recebidos.stream().map(PagamentoLinha::valor).toList())
        );
    }

    private static BigDecimal somar(List<BigDecimal> valores) {
        return valores.stream().reduce(ZERO_MONETARIO, BigDecimal::add);
    }
}
