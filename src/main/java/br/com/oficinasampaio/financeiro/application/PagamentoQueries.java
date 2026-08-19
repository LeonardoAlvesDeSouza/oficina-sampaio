package br.com.oficinasampaio.financeiro.application;

import java.util.Optional;
import java.util.UUID;

/**
 * Contrato público de leitura do financeiro. É por aqui que outro módulo pergunta
 * do pagamento de uma ordem, sem tocar no repositório interno deste módulo.
 */
@FunctionalInterface
public interface PagamentoQueries {

    Optional<PagamentoDaOrdem> buscarPorOrdemServico(UUID ordemServicoId);
}