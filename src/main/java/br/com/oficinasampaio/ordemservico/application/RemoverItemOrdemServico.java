package br.com.oficinasampaio.ordemservico.application;

import br.com.oficinasampaio.ordemservico.domain.OrdemServicoRepository;
import br.com.oficinasampaio.shared.domain.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemoverItemOrdemServico {

    private final OrdemServicoRepository ordemServicoRepository;

    public RemoverItemOrdemServico(OrdemServicoRepository ordemServicoRepository) {
        this.ordemServicoRepository = ordemServicoRepository;
    }

    @Transactional
    public OrdemServicoDetalheView executar(RemoverItemOrdemServicoCommand command) {
        var ordem = ordemServicoRepository.buscarPorId(command.ordemServicoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de serviço não encontrada"));

        ordem.removerItem(command.itemId());

        return OrdemServicoDetalheView.de(ordemServicoRepository.salvar(ordem));
    }
}
