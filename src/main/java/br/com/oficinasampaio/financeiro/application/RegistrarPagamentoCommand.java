package br.com.oficinasampaio.financeiro.application;

import br.com.oficinasampaio.shared.domain.FormaPagamento;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * O que o caixa precisa saber para lançar um pagamento. Espelha o evento da
 * ordem de serviço, mas é contrato próprio do módulo: o financeiro continua
 * chamável sem evento nenhum.
 */
public record RegistrarPagamentoCommand(
        UUID ordemServicoId,
        UUID clienteId,
        FormaPagamento forma,
        BigDecimal valor,
        Instant registradoEm
) {
}