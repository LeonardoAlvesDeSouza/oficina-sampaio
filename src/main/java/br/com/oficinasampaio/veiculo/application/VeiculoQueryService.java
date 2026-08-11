package br.com.oficinasampaio.veiculo.application;

import br.com.oficinasampaio.veiculo.domain.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class VeiculoQueryService implements VeiculoQueries {

    private final VeiculoRepository veiculoRepository;

    public VeiculoQueryService(VeiculoRepository veiculoRepository) {
        this.veiculoRepository = veiculoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VeiculoParaOrdem> buscarPorId(UUID veiculoId) {
        return veiculoRepository.buscarPorId(veiculoId)
                .map(veiculo -> new VeiculoParaOrdem(
                        veiculo.getId(),
                        veiculo.getClienteId(),
                        veiculo.getPlaca(),
                        veiculo.getMarca(),
                        veiculo.getModelo(),
                        veiculo.isAtivo()
                ));
    }
}
