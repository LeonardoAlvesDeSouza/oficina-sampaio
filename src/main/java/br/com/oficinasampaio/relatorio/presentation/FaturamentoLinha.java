package br.com.oficinasampaio.relatorio.presentation;

import br.com.oficinasampaio.relatorio.application.RecebimentoDoPeriodo;
import br.com.oficinasampaio.shared.presentation.FormatoOficina;

import java.math.BigDecimal;
import java.util.List;

/** Uma linha do relatório de faturamento. Nomes espelham os campos do JRXML. */
public record FaturamentoLinha(
        String recebidoEm,
        String numeroOrdem,
        String cliente,
        String forma,
        BigDecimal valor
) {

    static List<FaturamentoLinha> de(List<RecebimentoDoPeriodo> recebimentos) {
        return recebimentos.stream()
                .map(recebimento -> new FaturamentoLinha(
                        FormatoOficina.dataHora(recebimento.recebidoEm()),
                        FormatoOficina.numeroOrdem(recebimento.ordemServicoId()),
                        recebimento.cliente(),
                        recebimento.forma().getRotulo(),
                        recebimento.valor()
                ))
                .toList();
    }
}
