package br.com.oficinasampaio.financeiro.domain;

/**
 * Direção do dinheiro no caixa. O valor de uma movimentação é sempre positivo —
 * quem diz se ele soma ou subtrai é o tipo, não o sinal do número.
 */
public enum TipoMovimentacao {
    ENTRADA,
    SAIDA
}