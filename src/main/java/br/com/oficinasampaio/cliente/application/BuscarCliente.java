package br.com.oficinasampaio.cliente.application;

import br.com.oficinasampaio.cliente.domain.ClienteRepository;
import br.com.oficinasampaio.shared.domain.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BuscarCliente {

    private final ClienteRepository clienteRepository;

    public BuscarCliente(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public ClienteView executar(UUID clienteId) {
        return clienteRepository.buscarPorId(clienteId)
                .map(ClienteView::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));
    }
}
