package br.com.oficinasampaio.ordemservico.application;

import br.com.oficinasampaio.ordemservico.domain.OrdemServicoRepository;
import br.com.oficinasampaio.shared.domain.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BuscarOrdemServico {

    private final OrdemServicoRepository ordemServicoRepository;

    public BuscarOrdemServico(OrdemServicoRepository ordemServicoRepository) {
        this.ordemServicoRepository = ordemServicoRepository;
    }

    @Transactional(readOnly = true)
    public OrdemServicoDetalheView executar(UUID ordemServicoId) {
        return ordemServicoRepository.buscarPorId(ordemServicoId)
                .map(OrdemServicoDetalheView::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de serviço não encontrada"));
    }
}
