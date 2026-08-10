package br.com.oficinasampaio.veiculo.infrastructure.persistence;

import br.com.oficinasampaio.veiculo.domain.Veiculo;
import br.com.oficinasampaio.veiculo.domain.VeiculoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
class VeiculoRepositoryAdapter implements VeiculoRepository {

    private final SpringDataVeiculoRepository repository;

    VeiculoRepositoryAdapter(SpringDataVeiculoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Veiculo salvar(Veiculo veiculo) {
        return repository.save(veiculo);
    }

    @Override
    public boolean existePorPlaca(String placa) {
        return repository.existsByPlaca(placa);
    }

    @Override
    public List<Veiculo> listarPorCliente(UUID clienteId) {
        return repository.findAllByClienteIdOrderByPlacaAsc(clienteId);
    }
}
