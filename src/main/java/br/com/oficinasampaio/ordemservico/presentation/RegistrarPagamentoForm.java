package br.com.oficinasampaio.ordemservico.presentation;

import br.com.oficinasampaio.financeiro.application.FormaPagamentoView;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * O valor vai no formulário mesmo vindo pronto do total: o domínio confere os
 * dois e recusa o pagamento se a tela estiver com um total velho — outra aba
 * pode ter lançado uma peça no meio do caminho.
 */
public class RegistrarPagamentoForm {

    @NotNull(message = "Forma de pagamento é obrigatória")
    private FormaPagamentoView forma;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser positivo")
    @Digits(integer = 10, fraction = 2, message = "Valor inválido")
    private BigDecimal valor;

    public FormaPagamentoView getForma() {
        return forma;
    }

    public void setForma(FormaPagamentoView forma) {
        this.forma = forma;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}
