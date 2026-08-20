package br.com.oficinasampaio.relatorio.application;

import br.com.oficinasampaio.ordemservico.application.StatusOrdemServicoView;

import java.math.BigDecimal;
import java.util.List;

/**
 * A oficina em números, na ordem das perguntas que o dono faz: quanto trabalho
 * está no pátio, quanto falta receber e como está o caixa.
 */
public record PainelGerencialView(
        List<ContagemPorStatus> ordensPorStatus,
        long ordens,
        long contasEmAberto,
        BigDecimal totalAReceber,
        BigDecimal entradas,
        BigDecimal saidas,
        BigDecimal saldo
) {

    public record ContagemPorStatus(StatusOrdemServicoView status, long quantidade) {
    }
}
