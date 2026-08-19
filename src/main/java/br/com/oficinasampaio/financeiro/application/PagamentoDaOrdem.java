package br.com.oficinasampaio.financeiro.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * O pagamento visto de fora do módulo, para a tela da ordem de serviço mostrar
 * como e quando a conta foi paga. Só leitura: quem altera pagamento é o caixa.
 */
public record PagamentoDaOrdem(
        UUID id,
        FormaPagamentoView forma,
        BigDecimal valor,
        Instant registradoEm
) {
}