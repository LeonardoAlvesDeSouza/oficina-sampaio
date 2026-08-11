package br.com.oficinasampaio.ordemservico.infrastructure.persistence;

import br.com.oficinasampaio.ordemservico.domain.OrdemServico;
import br.com.oficinasampaio.ordemservico.domain.OrdemServicoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class OrdemServicoRepositoryAdapter implements OrdemServicoRepository {

    private final SpringDataOrdemServicoRepository repository;

    OrdemServicoRepositoryAdapter(SpringDataOrdemServicoRepository repository) {
        this.repository = repository;
    }

    @Override
    public OrdemServico salvar(OrdemServico ordemServico) {
        return repository.save(ordemServico);
    }

    @Override
    public Optional<OrdemServico> buscarPorId(UUID ordemServicoId) {
        return repository.findById(ordemServicoId);
    }

    @Override
    public List<OrdemServico> listar() {
        return repository.findAllByOrderByAbertaEmDesc();
    }
}
