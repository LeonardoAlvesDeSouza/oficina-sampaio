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

import br.com.oficinasampaio.shared.domain.FormaPagamento;
import br.com.oficinasampaio.shared.domain.PagamentoRegistrado;
import br.com.oficinasampaio.shared.domain.RecursoNaoEncontradoException;
import br.com.oficinasampaio.shared.domain.RegraNegocioException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "ordens_servico")
public class OrdemServico {

    private static final BigDecimal ZERO_MONETARIO = new BigDecimal("0.00");

    static final String ITENS_BLOQUEADOS =
            "Itens só podem ser alterados enquanto a ordem não está finalizada";

    static final String PAGAMENTO_DE_ORDEM_CANCELADA =
            "Ordem cancelada não recebe pagamento";

    static final String PAGAMENTO_ANTES_DE_FINALIZAR =
            "O pagamento só pode ser registrado depois de finalizar a ordem";

    static final String PAGAMENTO_DUPLICADO =
            "Esta ordem já está paga";

    static final String VALOR_DIFERENTE_DO_TOTAL =
            "O valor do pagamento deve ser igual ao total da ordem";

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

    @Enumerated(EnumType.STRING)
    @Column(name = "status_pagamento", nullable = false, length = 20)
    private StatusPagamento statusPagamento;

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
        this.statusPagamento = StatusPagamento.PENDENTE;
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
            throw new RegraNegocioException(campo + " é obrigatório");
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

    public StatusPagamento getStatusPagamento() {
        return statusPagamento;
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

    /**
     * Remove um serviço ou peça lançado por engano. Só vale na mesma janela em
     * que o item pôde ser cadastrado — depois de finalizada, o valor da ordem
     * está fechado e o item vira histórico.
     */
    public void removerItem(UUID itemId) {
        Objects.requireNonNull(itemId, "Item é obrigatório");
        if (!status.permiteAlterarItens()) {
            throw new RegraNegocioException(ITENS_BLOQUEADOS);
        }

        var item = itens.stream()
                .filter(candidato -> itemId.equals(candidato.getId()))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Item não encontrado nesta ordem de serviço"
                ));

        // A execução só começa com item lançado; esvaziar a lista depois disso
        // deixaria a ordem num estado que a própria transição não permitiria.
        if (status != StatusOrdemServico.ABERTA && itens.size() == 1) {
            throw new RegraNegocioException(
                    "A ordem já em andamento precisa manter ao menos um item"
            );
        }

        itens.remove(item);
    }

    public void iniciarExecucao() {
        executar(AcaoOrdemServico.INICIAR_EXECUCAO);
    }

    public void aguardarPeca() {
        executar(AcaoOrdemServico.AGUARDAR_PECA);
    }

    public void retomarExecucao() {
        executar(AcaoOrdemServico.RETOMAR_EXECUCAO);
    }

    public void finalizar() {
        executar(AcaoOrdemServico.FINALIZAR);
    }

    public void entregar() {
        executar(AcaoOrdemServico.ENTREGAR);
    }

    public void cancelar() {
        executar(AcaoOrdemServico.CANCELAR);
    }

    public void executar(AcaoOrdemServico acao) {
        Objects.requireNonNull(acao, "Ação é obrigatória");
        if (!acao.disponivelPara(this)) {
            throw new RegraNegocioException(acao.mensagemIndisponivel(this));
        }
        status = acao.getDestino();
    }

    public List<AcaoOrdemServico> getAcoesDisponiveis() {
        return Arrays.stream(AcaoOrdemServico.values())
                .filter(acao -> acao.disponivelPara(this))
                .toList();
    }

    /**
     * Fecha a conta da ordem e anuncia o pagamento para quem cuida do caixa.
     * <p>
     * A janela do pagamento é o espelho da janela dos itens: só depois que a
     * ordem é finalizada o valor está fechado, e pagar antes disso deixaria a
     * conta paga e o total ainda podendo crescer com uma peça lançada depois.
     * <p>
     * O valor chega de fora só para ser conferido contra o total; o que vale no
     * evento é o total do agregado, não o número que a tela enviou — se a tela
     * estiver com um total velho, o pagamento é recusado em vez de gravar uma
     * entrada de caixa que não corresponde à ordem.
     */
    public PagamentoRegistrado registrarPagamento(
            FormaPagamento forma,
            BigDecimal valor,
            Instant registradoEm
    ) {
        Objects.requireNonNull(forma, "Forma de pagamento é obrigatória");
        Objects.requireNonNull(registradoEm, "Data do pagamento é obrigatória");

        if (status == StatusOrdemServico.CANCELADA) {
            throw new RegraNegocioException(PAGAMENTO_DE_ORDEM_CANCELADA);
        }
        if (status.permiteAlterarItens()) {
            throw new RegraNegocioException(PAGAMENTO_ANTES_DE_FINALIZAR);
        }
        if (statusPagamento == StatusPagamento.PAGA) {
            throw new RegraNegocioException(PAGAMENTO_DUPLICADO);
        }

        var total = getTotal();
        if (valor == null || valor.compareTo(total) != 0) {
            throw new RegraNegocioException(VALOR_DIFERENTE_DO_TOTAL);
        }

        statusPagamento = StatusPagamento.PAGA;
        return new PagamentoRegistrado(id, clienteId, forma, total, registradoEm);
    }

    public boolean isPaga() {
        return statusPagamento == StatusPagamento.PAGA;
    }

    /**
     * Se a tela deve oferecer o registro do pagamento. Mesma regra do método que
     * registra — a tela não decide nada, só pergunta.
     */
    public boolean permiteRegistrarPagamento() {
        return statusPagamento == StatusPagamento.PENDENTE
                && status != StatusOrdemServico.CANCELADA
                && !status.permiteAlterarItens();
    }

    public boolean permiteAlterarItens() {
        return status.permiteAlterarItens();
    }

    boolean possuiItens() {
        return !itens.isEmpty();
    }

    private void adicionarItem(
            TipoItemOrdemServico tipo,
            String descricao,
            BigDecimal quantidade,
            BigDecimal valorUnitario
    ) {
        if (!status.permiteAlterarItens()) {
            throw new RegraNegocioException(ITENS_BLOQUEADOS);
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
