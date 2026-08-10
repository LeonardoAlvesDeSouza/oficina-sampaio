package br.com.oficinasampaio.veiculo.application;

import br.com.oficinasampaio.cliente.application.ClienteQueries;
import br.com.oficinasampaio.shared.domain.RecursoNaoEncontradoException;
import br.com.oficinasampaio.shared.domain.RegraNegocioException;
import br.com.oficinasampaio.veiculo.domain.Veiculo;
import br.com.oficinasampaio.veiculo.domain.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CadastrarVeiculo {

    private final ClienteQueries clienteQueries;
    private final VeiculoRepository veiculoRepository;

    public CadastrarVeiculo(ClienteQueries clienteQueries, VeiculoRepository veiculoRepository) {
        this.clienteQueries = clienteQueries;
        this.veiculoRepository = veiculoRepository;
    }

    @Transactional
    public VeiculoView executar(CadastrarVeiculoCommand command) {
        if (!clienteQueries.existeClienteAtivo(command.clienteId())) {
            throw new RecursoNaoEncontradoException("Cliente ativo não encontrado");
        }

        var veiculo = Veiculo.cadastrar(
                command.clienteId(),
                command.placa(),
                command.marca(),
                command.modelo(),
                command.ano(),
                command.cor(),
                command.quilometragem()
        );
        if (veiculoRepository.existePorPlaca(veiculo.getPlaca())) {
            throw new RegraNegocioException("Placa já cadastrada");
        }
        return VeiculoView.de(veiculoRepository.salvar(veiculo));
    }
}
