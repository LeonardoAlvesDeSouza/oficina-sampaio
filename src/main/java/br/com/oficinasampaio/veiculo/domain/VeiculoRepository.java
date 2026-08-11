package br.com.oficinasampaio.veiculo.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VeiculoRepository {

    Veiculo salvar(Veiculo veiculo);

    boolean existePorPlaca(String placa);

    Optional<Veiculo> buscarPorId(UUID veiculoId);

    List<Veiculo> listarPorCliente(UUID clienteId);
}
