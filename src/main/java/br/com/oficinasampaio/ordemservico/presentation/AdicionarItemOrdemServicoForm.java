package br.com.oficinasampaio.ordemservico.presentation;

import br.com.oficinasampaio.ordemservico.application.TipoItemOrdemServicoView;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class AdicionarItemOrdemServicoForm {

    @NotNull(message = "Tipo é obrigatório")
    private TipoItemOrdemServicoView tipo;

    @NotBlank(message = "Descrição é obrigatória")
    @Size(max = 200, message = "Descrição deve possuir no máximo 200 caracteres")
    private String descricao;

    @NotNull(message = "Quantidade é obrigatória")
    @DecimalMin(value = "0.001", message = "Quantidade deve ser positiva")
    @Digits(integer = 7, fraction = 3, message = "Quantidade inválida")
    private BigDecimal quantidade;

    @NotNull(message = "Valor unitário é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor unitário deve ser positivo")
    @Digits(integer = 10, fraction = 2, message = "Valor unitário inválido")
    private BigDecimal valorUnitario;

    public TipoItemOrdemServicoView getTipo() {
        return tipo;
    }

    public void setTipo(TipoItemOrdemServicoView tipo) {
        this.tipo = tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }
}
