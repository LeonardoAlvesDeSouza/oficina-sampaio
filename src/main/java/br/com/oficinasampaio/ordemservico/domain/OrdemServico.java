package br.com.oficinasampaio.ordemservico.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "ordens_servico")
public class OrdemServico {

    private static final BigDecimal ZERO_MONETARIO = new BigDecimal("0.00");

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Column(name = "veiculo_id", nullable = false)
    private UUID veiculoId;

    @Column(name = "relato_problema", nullable = false, length = 1000)
    private String relatoProblema;

    @Column(name = "aberta_em", nullable = false)
    private Instant abertaEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusOrdemServico status;

    @OneToMany(mappedBy = "ordemServico", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private final List<ItemOrdemServico> itens = new ArrayList<>();

    @Version
    private long versao;

    protected OrdemServico() {
    }

    private OrdemServico(UUID clienteId, UUID veiculoId, String relatoProblema, Instant abertaEm) {
        this.clienteId = Objects.requireNonNull(clienteId, "Cliente é obrigatório");
        this.veiculoId = Objects.requireNonNull(veiculoId, "Veículo é obrigatório");
        this.relatoProblema = textoObrigatorio(relatoProblema, "Relato do problema");
        this.abertaEm = Objects.requireNonNull(abertaEm, "Data de abertura é obrigatória");
        this.status = StatusOrdemServico.ABERTA;
    }

    public static OrdemServico abrir(
            UUID clienteId,
            UUID veiculoId,
            String relatoProblema,
            Instant abertaEm
    ) {
        return new OrdemServico(clienteId, veiculoId, relatoProblema, abertaEm);
    }

    private static String textoObrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " é obrigatório");
        }
        return valor.trim();
    }

    public UUID getId() {
        return id;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public UUID getVeiculoId() {
        return veiculoId;
    }

    public String getRelatoProblema() {
        return relatoProblema;
    }

    public Instant getAbertaEm() {
        return abertaEm;
    }

    public StatusOrdemServico getStatus() {
        return status;
    }

    public BigDecimal getTotalServicos() {
        return totalPorTipo(TipoItemOrdemServico.SERVICO);
    }

    public BigDecimal getTotalPecas() {
        return totalPorTipo(TipoItemOrdemServico.PECA);
    }

    public BigDecimal getTotal() {
        return getTotalServicos().add(getTotalPecas());
    }

    public void adicionarServico(String descricao, BigDecimal quantidade, BigDecimal valorUnitario) {
        adicionarItem(TipoItemOrdemServico.SERVICO, descricao, quantidade, valorUnitario);
    }

    public void adicionarPeca(String descricao, BigDecimal quantidade, BigDecimal valorUnitario) {
        adicionarItem(TipoItemOrdemServico.PECA, descricao, quantidade, valorUnitario);
    }

    private void adicionarItem(
            TipoItemOrdemServico tipo,
            String descricao,
            BigDecimal quantidade,
            BigDecimal valorUnitario
    ) {
        if (status != StatusOrdemServico.ABERTA) {
            throw new IllegalStateException("Itens só podem ser alterados enquanto a ordem está aberta");
        }
        itens.add(new ItemOrdemServico(this, tipo, descricao, quantidade, valorUnitario));
    }

    private BigDecimal totalPorTipo(TipoItemOrdemServico tipo) {
        return itens.stream()
                .filter(item -> item.getTipo() == tipo)
                .map(ItemOrdemServico::getTotal)
                .reduce(ZERO_MONETARIO, BigDecimal::add);
    }

    public List<ItemOrdemServico> getItens() {
        return List.copyOf(itens);
    }
}
