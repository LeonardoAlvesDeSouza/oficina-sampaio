package br.com.oficinasampaio.ordemservico.application;

import br.com.oficinasampaio.shared.domain.FormaPagamento;

import java.math.BigDecimal;
import java.util.UUID;

public record RegistrarPagamentoOrdemServicoCommand(
        UUID ordemServicoId,
        FormaPagamento forma,
        BigDecimal valor
) {
}