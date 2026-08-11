package br.com.oficinasampaio.ordemservico.application;

import br.com.oficinasampaio.ordemservico.domain.OrdemServicoRepository;
import br.com.oficinasampaio.shared.domain.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlterarStatusOrdemServico {

    private final OrdemServicoRepository ordemServicoRepository;

    public AlterarStatusOrdemServico(OrdemServicoRepository ordemServicoRepository) {
        this.ordemServicoRepository = ordemServicoRepository;
    }

    @Transactional
    public OrdemServicoDetalheView executar(AlterarStatusOrdemServicoCommand command) {
        var ordem = ordemServicoRepository.buscarPorId(command.ordemServicoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de serviço não encontrada"));

        ordem.executar(command.acao().paraDominio());

        return OrdemServicoDetalheView.de(ordemServicoRepository.salvar(ordem));
    }
}
