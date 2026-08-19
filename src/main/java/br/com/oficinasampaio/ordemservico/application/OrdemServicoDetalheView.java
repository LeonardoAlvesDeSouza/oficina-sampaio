package br.com.oficinasampaio.ordemservico.application;

import br.com.oficinasampaio.ordemservico.domain.OrdemServico;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrdemServicoDetalheView(
        UUID id,
        UUID clienteId,
        UUID veiculoId,
        String relatoProblema,
        Instant abertaEm,
        StatusOrdemServicoView status,
        StatusPagamentoView statusPagamento,
        List<AcaoOrdemServicoView> acoesDisponiveis,
        boolean permiteAlterarItens,
        boolean permiteRegistrarPagamento,
        List<ItemOrdemServicoView> itens,
        BigDecimal totalServicos,
        BigDecimal totalPecas,
        BigDecimal total
) {

    static OrdemServicoDetalheView de(OrdemServico ordem) {
        return new OrdemServicoDetalheView(
                ordem.getId(),
                ordem.getClienteId(),
                ordem.getVeiculoId(),
                ordem.getRelatoProblema(),
                ordem.getAbertaEm(),
                StatusOrdemServicoView.valueOf(ordem.getStatus().name()),
                StatusPagamentoView.valueOf(ordem.getStatusPagamento().name()),
                AcaoOrdemServicoView.disponiveisPara(ordem),
                ordem.permiteAlterarItens(),
                ordem.permiteRegistrarPagamento(),
                ordem.getItens().stream().map(ItemOrdemServicoView::de).toList(),
                ordem.getTotalServicos(),
                ordem.getTotalPecas(),
                ordem.getTotal()
        );
    }
}
