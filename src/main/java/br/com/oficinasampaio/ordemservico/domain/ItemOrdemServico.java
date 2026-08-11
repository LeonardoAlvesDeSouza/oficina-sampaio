package br.com.oficinasampaio.ordemservico.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Entity
@Table(name = "itens_ordem_servico")
public class ItemOrdemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServico ordemServico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoItemOrdemServico tipo;

    @Column(nullable = false, length = 200)
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantidade;

    @Column(name = "valor_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorUnitario;

    protected ItemOrdemServico() {
    }

    ItemOrdemServico(
            OrdemServico ordemServico,
            TipoItemOrdemServico tipo,
            String descricao,
            BigDecimal quantidade,
            BigDecimal valorUnitario
    ) {
        this.ordemServico = ordemServico;
        this.tipo = tipo;
        this.descricao = textoObrigatorio(descricao, "Descrição");
        this.quantidade = numeroPositivo(quantidade, "Quantidade deve ser positiva");
        this.valorUnitario = valorMonetarioPositivo(valorUnitario);
    }

    private static String textoObrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " é obrigatória");
        }
        return valor.trim();
    }

    private static BigDecimal numeroPositivo(BigDecimal valor, String mensagem) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException(mensagem);
        }
        return valor;
    }

    private static BigDecimal valorMonetarioPositivo(BigDecimal valor) {
        var normalizado = numeroPositivo(valor, "Valor unitário deve ser positivo")
                .setScale(2, RoundingMode.HALF_UP);
        return numeroPositivo(normalizado, "Valor unitário deve ser positivo");
    }

    public UUID getId() {
        return id;
    }

    public TipoItemOrdemServico getTipo() {
        return tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public BigDecimal getTotal() {
        return quantidade.multiply(valorUnitario).setScale(2, RoundingMode.HALF_UP);
    }
}
