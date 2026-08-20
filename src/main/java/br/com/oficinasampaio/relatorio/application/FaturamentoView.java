package br.com.oficinasampaio.relatorio.application;

import java.math.BigDecimal;
import java.util.List;

public record FaturamentoView(List<RecebimentoDoPeriodo> recebimentos, BigDecimal total) {
}
