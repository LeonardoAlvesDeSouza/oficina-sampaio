package br.com.oficinasampaio.ordemservico.domain;

/**
 * Estado financeiro da ordem, separado do estado operacional: um carro pode
 * estar entregue e a conta em aberto, e o contrário também acontece.
 */
public enum StatusPagamento {
    PENDENTE,
    PAGA
}