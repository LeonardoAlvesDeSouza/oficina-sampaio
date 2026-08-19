package br.com.oficinasampaio.ordemservico.infrastructure.persistence;

import br.com.oficinasampaio.ordemservico.domain.OrdemServico;
import br.com.oficinasampaio.ordemservico.domain.OrdemServicoRepository;
import br.com.oficinasampaio.ordemservico.domain.StatusOrdemServico;
import br.com.oficinasampaio.ordemservico.domain.StatusPagamento;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
class OrdemServicoRepositoryAdapter implements OrdemServicoRepository {

    /** Estados em que o valor da ordem já está fechado e a conta pode ser cobrada. */
    private static final Set<StatusOrdemServico> COM_VALOR_FECHADO = Set.of(
            StatusOrdemServico.FINALIZADA,
            StatusOrdemServico.ENTREGUE
    );

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

    @Override
    public List<OrdemServico> listarAReceber() {
        return repository.findByStatusPagamentoAndStatusInOrderByAbertaEmAsc(
                StatusPagamento.PENDENTE, COM_VALOR_FECHADO
        );
    }
}
