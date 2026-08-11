package br.com.oficinasampaio.ordemservico.application;

import br.com.oficinasampaio.ordemservico.domain.ItemOrdemServico;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemOrdemServicoView(
        UUID id,
        TipoItemOrdemServicoView tipo,
        String descricao,
        BigDecimal quantidade,
        BigDecimal valorUnitario,
        BigDecimal total
) {

    static ItemOrdemServicoView de(ItemOrdemServico item) {
        return new ItemOrdemServicoView(
                item.getId(),
                TipoItemOrdemServicoView.valueOf(item.getTipo().name()),
                item.getDescricao(),
                item.getQuantidade(),
                item.getValorUnitario(),
                item.getTotal()
        );
    }
}
