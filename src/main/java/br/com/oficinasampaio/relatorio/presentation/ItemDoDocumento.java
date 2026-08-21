package br.com.oficinasampaio.relatorio.presentation;

import br.com.oficinasampaio.ordemservico.application.ItemOrdemServicoView;
import br.com.oficinasampaio.shared.presentation.FormatoOficina;

import java.math.BigDecimal;
import java.util.List;

/**
 * Uma linha da via impressa da ordem. Os nomes dos componentes são os nomes dos
 * campos no JRXML — mudar um exige mudar o outro.
 */
public record ItemDoDocumento(
        String tipo,
        String descricao,
        String quantidade,
        BigDecimal valorUnitario,
        BigDecimal total
) {

    static List<ItemDoDocumento> de(List<ItemOrdemServicoView> itens) {
        return itens.stream()
                .map(item -> new ItemDoDocumento(
                        item.tipo().getRotulo(),
                        item.descricao(),
                        FormatoOficina.quantidade(item.quantidade()),
                        item.valorUnitario(),
                        item.total()
                ))
                .toList();
    }
}
