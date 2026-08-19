package br.com.oficinasampaio.shared.domain;

/**
 * Como o cliente pagou. Vocabulário compartilhado entre a ordem de serviço, que
 * registra o pagamento, e o financeiro, que classifica a entrada no caixa — por
 * isso mora no domínio comum e não dentro de um dos dois módulos.
 * <p>
 * Sem rótulo de tela, como todo enum de domínio: quem exibe traduz.
 */
public enum FormaPagamento {
    DINHEIRO,
    PIX,
    CARTAO_DEBITO,
    CARTAO_CREDITO,
    TRANSFERENCIA
}