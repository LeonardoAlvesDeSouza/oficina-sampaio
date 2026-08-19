package br.com.oficinasampaio.financeiro.domain;

import java.util.List;

public interface MovimentacaoFinanceiraRepository {

    MovimentacaoFinanceira salvar(MovimentacaoFinanceira movimentacao);

    /** Do mais recente para o mais antigo, como se lê um extrato. */
    List<MovimentacaoFinanceira> listar();

    /**
     * Totais de entrada e de saída somados no banco. Somar em memória obrigaria a
     * carregar o histórico inteiro só para mostrar um saldo no topo da tela.
     */
    PosicaoDeCaixa posicao();
}