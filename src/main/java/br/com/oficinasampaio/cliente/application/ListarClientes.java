package br.com.oficinasampaio.cliente.application;

import br.com.oficinasampaio.cliente.domain.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListarClientes {

    private final ClienteRepository clienteRepository;

    public ListarClientes(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public List<ClienteView> executar() {
        return clienteRepository.listarTodos().stream()
                .map(ClienteView::de)
                .toList();
    }
}
