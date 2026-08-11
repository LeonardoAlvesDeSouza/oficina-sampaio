package br.com.oficinasampaio.ordemservico.application;

import br.com.oficinasampaio.ordemservico.domain.OrdemServico;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrdemServicoView(
        UUID id,
        UUID clienteId,
        UUID veiculoId,
        String relatoProblema,
        Instant abertaEm,
        StatusOrdemServicoView status,
        BigDecimal totalServicos,
        BigDecimal totalPecas,
        BigDecimal total
) {

    static OrdemServicoView de(OrdemServico ordem) {
        return new OrdemServicoView(
                ordem.getId(),
                ordem.getClienteId(),
                ordem.getVeiculoId(),
                ordem.getRelatoProblema(),
                ordem.getAbertaEm(),
                StatusOrdemServicoView.valueOf(ordem.getStatus().name()),
                ordem.getTotalServicos(),
                ordem.getTotalPecas(),
                ordem.getTotal()
        );
    }
}
