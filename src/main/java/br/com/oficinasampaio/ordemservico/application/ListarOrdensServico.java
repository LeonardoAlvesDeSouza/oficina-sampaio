package br.com.oficinasampaio.ordemservico.application;

import br.com.oficinasampaio.ordemservico.domain.OrdemServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListarOrdensServico {

    private final OrdemServicoRepository ordemServicoRepository;

    public ListarOrdensServico(OrdemServicoRepository ordemServicoRepository) {
        this.ordemServicoRepository = ordemServicoRepository;
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoView> executar() {
        return ordemServicoRepository.listar().stream()
                .map(OrdemServicoView::de)
                .toList();
    }
}
