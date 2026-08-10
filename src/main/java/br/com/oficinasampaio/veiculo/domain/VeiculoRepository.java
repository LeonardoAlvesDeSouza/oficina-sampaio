package br.com.oficinasampaio.veiculo.domain;

import java.util.List;
import java.util.UUID;

public interface VeiculoRepository {

    Veiculo salvar(Veiculo veiculo);

    boolean existePorPlaca(String placa);

    List<Veiculo> listarPorCliente(UUID clienteId);
}
