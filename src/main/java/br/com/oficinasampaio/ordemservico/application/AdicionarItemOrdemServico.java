package br.com.oficinasampaio.ordemservico.application;

import br.com.oficinasampaio.ordemservico.domain.OrdemServicoRepository;
import br.com.oficinasampaio.shared.domain.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdicionarItemOrdemServico {

    private final OrdemServicoRepository ordemServicoRepository;

    public AdicionarItemOrdemServico(OrdemServicoRepository ordemServicoRepository) {
        this.ordemServicoRepository = ordemServicoRepository;
    }

    @Transactional
    public OrdemServicoDetalheView executar(AdicionarItemOrdemServicoCommand command) {
        var ordem = ordemServicoRepository.buscarPorId(command.ordemServicoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de serviço não encontrada"));

        switch (command.tipo()) {
            case SERVICO -> ordem.adicionarServico(
                    command.descricao(), command.quantidade(), command.valorUnitario()
            );
            case PECA -> ordem.adicionarPeca(
                    command.descricao(), command.quantidade(), command.valorUnitario()
            );
        }

        return OrdemServicoDetalheView.de(ordemServicoRepository.salvar(ordem));
    }
}
