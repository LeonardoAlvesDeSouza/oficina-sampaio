package br.com.oficinasampaio.ordemservico.application;

import java.math.BigDecimal;
import java.util.UUID;

public record AdicionarItemOrdemServicoCommand(
        UUID ordemServicoId,
        TipoItemOrdemServicoView tipo,
        String descricao,
        BigDecimal quantidade,
        BigDecimal valorUnitario
) {
}
