package br.com.oficinasampaio.cliente.application;

import br.com.oficinasampaio.cliente.domain.Cliente;
import br.com.oficinasampaio.cliente.domain.ClienteRepository;
import br.com.oficinasampaio.shared.domain.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CadastrarCliente {

    private final ClienteRepository clienteRepository;

    public CadastrarCliente(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public ClienteView executar(CadastrarClienteCommand command) {
        var cliente = Cliente.cadastrar(
                command.nome(),
                command.cpfCnpj(),
                command.telefone(),
                command.email()
        );
        if (cliente.getCpfCnpj() != null
                && clienteRepository.existePorCpfCnpj(cliente.getCpfCnpj())) {
            throw new RegraNegocioException("CPF/CNPJ já cadastrado");
        }
        return ClienteView.de(clienteRepository.salvar(cliente));
    }
}
