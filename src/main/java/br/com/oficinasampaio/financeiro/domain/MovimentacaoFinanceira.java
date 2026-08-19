package br.com.oficinasampaio.financeiro.domain;

import br.com.oficinasampaio.shared.domain.NumeroCurto;
import br.com.oficinasampaio.shared.domain.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Uma linha do histórico do caixa. As movimentações não são alteradas nem
 * apagadas: é delas que o saldo é derivado, e um lançamento errado se corrige
 * com outro lançamento, como em qualquer livro caixa.
 * <p>
 * Entrada nasce de um pagamento e guarda qual foi — é esse vínculo que impede o
 * mesmo pagamento entrar duas vezes no caixa. Saída é lançada à mão pela oficina
 * e não tem pagamento nenhum atrás dela.
 */
@Entity
@Table(name = "movimentacoes_financeiras")
public class MovimentacaoFinanceira {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimentacao tipo;

    @Column(nullable = false, length = 200)
    private String descricao;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "ocorrida_em", nullable = false)
    private Instant ocorridaEm;

    @Column(name = "pagamento_id", unique = true)
    private UUID pagamentoId;

    @Version
    private long versao;

    protected MovimentacaoFinanceira() {
    }

    private MovimentacaoFinanceira(
            TipoMovimentacao tipo,
            String descricao,
            BigDecimal valor,
            Instant ocorridaEm,
            UUID pagamentoId
    ) {
        this.tipo = tipo;
        this.descricao = textoObrigatorio(descricao);
        this.valor = valorMonetarioPositivo(valor);
        this.ocorridaEm = Objects.requireNonNull(ocorridaEm, "Data da movimentação é obrigatória");
        this.pagamentoId = pagamentoId;
    }

    /**
     * Entrada de caixa correspondente ao pagamento de uma ordem. A descrição já
     * sai citando o número curto da ordem: quem lê o caixa precisa saber de onde
     * veio o dinheiro sem abrir outra tela.
     */
    public static MovimentacaoFinanceira entradaDePagamento(
            UUID pagamentoId,
            UUID ordemServicoId,
            BigDecimal valor,
            Instant ocorridaEm
    ) {
        Objects.requireNonNull(pagamentoId, "Pagamento é obrigatório");
        Objects.requireNonNull(ordemServicoId, "Ordem de serviço é obrigatória");
        return new MovimentacaoFinanceira(
                TipoMovimentacao.ENTRADA,
                "Pagamento da OS " + NumeroCurto.de(ordemServicoId),
                valor,
                ocorridaEm,
                pagamentoId
        );
    }

    public static MovimentacaoFinanceira saida(
            String descricao,
            BigDecimal valor,
            Instant ocorridaEm
    ) {
        return new MovimentacaoFinanceira(TipoMovimentacao.SAIDA, descricao, valor, ocorridaEm, null);
    }

    private static String textoObrigatorio(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new RegraNegocioException("Descrição da movimentação é obrigatória");
        }
        return valor.trim();
    }

    /**
     * Confere antes e depois de arredondar: 0,001 é positivo e viraria 0,00 em
     * dinheiro, e o caixa não aceita lançamento de valor nenhum.
     */
    private static BigDecimal valorMonetarioPositivo(BigDecimal valor) {
        var arredondado = positivo(valor).setScale(2, RoundingMode.HALF_UP);
        return positivo(arredondado);
    }

    private static BigDecimal positivo(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new RegraNegocioException("Valor da movimentação deve ser positivo");
        }
        return valor;
    }

    public UUID getId() {
        return id;
    }

    public TipoMovimentacao getTipo() {
        return tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public Instant getOcorridaEm() {
        return ocorridaEm;
    }

    public UUID getPagamentoId() {
        return pagamentoId;
    }
}