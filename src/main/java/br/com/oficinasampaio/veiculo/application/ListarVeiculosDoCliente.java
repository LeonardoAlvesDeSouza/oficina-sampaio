package br.com.oficinasampaio.veiculo.application;

import br.com.oficinasampaio.veiculo.domain.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListarVeiculosDoCliente {

    private final VeiculoRepository veiculoRepository;

    public ListarVeiculosDoCliente(VeiculoRepository veiculoRepository) {
        this.veiculoRepository = veiculoRepository;
    }

    @Transactional(readOnly = true)
    public List<VeiculoView> executar(UUID clienteId) {
        return veiculoRepository.listarPorCliente(clienteId).stream()
                .map(VeiculoView::de)
                .toList();
    }
}
