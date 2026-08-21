package br.com.oficinasampaio.relatorio.application;

import br.com.oficinasampaio.cliente.application.BuscarCliente;
import br.com.oficinasampaio.financeiro.application.PagamentoQueries;
import br.com.oficinasampaio.ordemservico.application.BuscarOrdemServico;
import br.com.oficinasampaio.veiculo.application.VeiculoQueries;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Reúne os dados da via impressa da ordem usando só consultas públicas dos outros
 * módulos — o relatório lê, nunca escreve, e não conhece repositório de ninguém.
 */
@Service
public class ConsultarDocumentoDaOrdem {

    private final BuscarOrdemServico buscarOrdemServico;
    private final BuscarCliente buscarCliente;
    private final VeiculoQueries veiculoQueries;
    private final PagamentoQueries pagamentoQueries;

    public ConsultarDocumentoDaOrdem(
            BuscarOrdemServico buscarOrdemServico,
            BuscarCliente buscarCliente,
            VeiculoQueries veiculoQueries,
            PagamentoQueries pagamentoQueries
    ) {
        this.buscarOrdemServico = buscarOrdemServico;
        this.buscarCliente = buscarCliente;
        this.veiculoQueries = veiculoQueries;
        this.pagamentoQueries = pagamentoQueries;
    }

    @Transactional(readOnly = true)
    public DocumentoDaOrdemView executar(UUID ordemServicoId) {
        var ordem = buscarOrdemServico.executar(ordemServicoId);
        return new DocumentoDaOrdemView(
                ordem,
                buscarCliente.executar(ordem.clienteId()),
                veiculoQueries.obterPorId(ordem.veiculoId()),
                pagamentoQueries.buscarPorOrdemServico(ordemServicoId).orElse(null)
        );
    }
}
