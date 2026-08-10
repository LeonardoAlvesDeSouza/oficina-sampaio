package br.com.oficinasampaio.cliente.application;

import br.com.oficinasampaio.cliente.domain.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ClienteQueryService implements ClienteQueries {

    private final ClienteRepository clienteRepository;

    public ClienteQueryService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeClienteAtivo(UUID clienteId) {
        return clienteRepository.buscarPorId(clienteId)
                .filter(cliente -> cliente.isAtivo())
                .isPresent();
    }
}
