package br.com.oficinasampaio.financeiro.application;

import java.math.BigDecimal;

public record RegistrarSaidaCommand(String descricao, BigDecimal valor) {
}