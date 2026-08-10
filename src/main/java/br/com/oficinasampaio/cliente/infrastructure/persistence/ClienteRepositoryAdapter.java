package br.com.oficinasampaio.cliente.infrastructure.persistence;

import br.com.oficinasampaio.cliente.domain.Cliente;
import br.com.oficinasampaio.cliente.domain.ClienteRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class ClienteRepositoryAdapter implements ClienteRepository {

    private final SpringDataClienteRepository repository;

    ClienteRepositoryAdapter(SpringDataClienteRepository repository) {
        this.repository = repository;
    }

    @Override
    public Cliente salvar(Cliente cliente) {
        return repository.save(cliente);
    }

    @Override
    public boolean existePorCpfCnpj(String cpfCnpj) {
        return repository.existsByCpfCnpj(cpfCnpj);
    }

    @Override
    public Optional<Cliente> buscarPorId(UUID id) {
        return repository.findById(id);
    }

    @Override
    public List<Cliente> listarTodos() {
        return repository.findAllByOrderByNomeAsc();
    }
}
