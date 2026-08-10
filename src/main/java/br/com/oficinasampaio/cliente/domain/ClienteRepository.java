package br.com.oficinasampaio.cliente.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository {

    Cliente salvar(Cliente cliente);

    boolean existePorCpfCnpj(String cpfCnpj);

    Optional<Cliente> buscarPorId(UUID id);

    List<Cliente> listarTodos();
}
