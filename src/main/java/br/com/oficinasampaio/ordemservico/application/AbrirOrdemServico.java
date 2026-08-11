package br.com.oficinasampaio.ordemservico.application;

import br.com.oficinasampaio.cliente.application.ClienteQueries;
import br.com.oficinasampaio.ordemservico.domain.OrdemServico;
import br.com.oficinasampaio.ordemservico.domain.OrdemServicoRepository;
import br.com.oficinasampaio.shared.domain.RecursoNaoEncontradoException;
import br.com.oficinasampaio.veiculo.application.VeiculoQueries;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class AbrirOrdemServico {

    private final ClienteQueries clienteQueries;
    private final VeiculoQueries veiculoQueries;
    private final OrdemServicoRepository ordemServicoRepository;
    private final Clock clock;

    public AbrirOrdemServico(
            ClienteQueries clienteQueries,
            VeiculoQueries veiculoQueries,
            OrdemServicoRepository ordemServicoRepository,
            Clock clock
    ) {
        this.clienteQueries = clienteQueries;
        this.veiculoQueries = veiculoQueries;
        this.ordemServicoRepository = ordemServicoRepository;
        this.clock = clock;
    }

    @Transactional
    public OrdemServicoView executar(AbrirOrdemServicoCommand command) {
        var veiculo = veiculoQueries.obterAtivo(command.veiculoId());
        if (!clienteQueries.existeClienteAtivo(veiculo.clienteId())) {
            throw new RecursoNaoEncontradoException("Cliente ativo não encontrado");
        }

        var ordem = OrdemServico.abrir(
                veiculo.clienteId(),
                veiculo.id(),
                command.relatoProblema(),
                clock.instant()
        );
        return OrdemServicoView.de(ordemServicoRepository.salvar(ordem));
    }
}
