package br.com.oficinasampaio.financeiro.domain;

import br.com.oficinasampaio.shared.domain.FormaPagamento;
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
 * O pagamento de uma ordem de serviço, guardado fora do estado operacional dela:
 * a ordem sabe apenas que está paga, e o histórico de como e quando o dinheiro
 * entrou vive aqui.
 * <p>
 * Uma ordem tem no máximo um pagamento. A regra é conferida pelo caso de uso e
 * garantida por restrição única no banco, que é o que resiste a duas requisições
 * ao mesmo tempo.
 */
@Entity
@Table(name = "pagamentos")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "ordem_servico_id", nullable = false, unique = true)
    private UUID ordemServicoId;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FormaPagamento forma;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "registrado_em", nullable = false)
    private Instant registradoEm;

    @Version
    private long versao;

    protected Pagamento() {
    }

    private Pagamento(
            UUID ordemServicoId,
            UUID clienteId,
            FormaPagamento forma,
            BigDecimal valor,
            Instant registradoEm
    ) {
        this.ordemServicoId = Objects.requireNonNull(ordemServicoId, "Ordem de serviço é obrigatória");
        this.clienteId = Objects.requireNonNull(clienteId, "Cliente é obrigatório");
        this.forma = Objects.requireNonNull(forma, "Forma de pagamento é obrigatória");
        this.valor = valorMonetarioPositivo(valor);
        this.registradoEm = Objects.requireNonNull(registradoEm, "Data do pagamento é obrigatória");
    }

    public static Pagamento registrar(
            UUID ordemServicoId,
            UUID clienteId,
            FormaPagamento forma,
            BigDecimal valor,
            Instant registradoEm
    ) {
        return new Pagamento(ordemServicoId, clienteId, forma, valor, registradoEm);
    }

    /**
     * Confere antes e depois de arredondar: um valor como 0,001 passa por
     * positivo e viraria 0,00 em dinheiro, que não é pagamento nenhum.
     */
    private static BigDecimal valorMonetarioPositivo(BigDecimal valor) {
        var arredondado = positivo(valor).setScale(2, RoundingMode.HALF_UP);
        return positivo(arredondado);
    }

    private static BigDecimal positivo(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new RegraNegocioException("Valor do pagamento deve ser positivo");
        }
        return valor;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrdemServicoId() {
        return ordemServicoId;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public FormaPagamento getForma() {
        return forma;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public Instant getRegistradoEm() {
        return registradoEm;
    }
}