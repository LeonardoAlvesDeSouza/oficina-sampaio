package br.com.oficinasampaio.financeiro.infrastructure.persistence;

import br.com.oficinasampaio.financeiro.domain.Pagamento;
import br.com.oficinasampaio.financeiro.domain.PagamentoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class PagamentoRepositoryAdapter implements PagamentoRepository {

    private final SpringDataPagamentoRepository repository;

    PagamentoRepositoryAdapter(SpringDataPagamentoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Pagamento salvar(Pagamento pagamento) {
        return repository.save(pagamento);
    }

    @Override
    public boolean existePorOrdemServico(UUID ordemServicoId) {
        return repository.existsByOrdemServicoId(ordemServicoId);
    }

    @Override
    public Optional<Pagamento> buscarPorOrdemServico(UUID ordemServicoId) {
        return repository.findByOrdemServicoId(ordemServicoId);
    }

    @Override
    public List<Pagamento> listar() {
        return repository.findAllByOrderByRegistradoEmDesc();
    }
}
