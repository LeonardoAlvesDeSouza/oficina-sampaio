package br.com.oficinasampaio.shared.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * O que a ordem de serviço anuncia quando recebe pagamento. É o contrato entre
 * os dois módulos: a ordem não conhece o caixa, e o caixa não mexe na ordem.
 * <p>
 * O valor vem fechado pelo agregado — é o total da ordem, não o número que a
 * tela enviou.
 */
public record PagamentoRegistrado(
        UUID ordemServicoId,
        UUID clienteId,
        FormaPagamento forma,
        BigDecimal valor,
        Instant registradoEm
) {
}