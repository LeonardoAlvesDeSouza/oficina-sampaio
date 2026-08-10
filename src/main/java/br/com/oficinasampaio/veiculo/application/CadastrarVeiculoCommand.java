package br.com.oficinasampaio.veiculo.application;

import java.util.UUID;

public record CadastrarVeiculoCommand(
        UUID clienteId,
        String placa,
        String marca,
        String modelo,
        Integer ano,
        String cor,
        Long quilometragem
) {
}
