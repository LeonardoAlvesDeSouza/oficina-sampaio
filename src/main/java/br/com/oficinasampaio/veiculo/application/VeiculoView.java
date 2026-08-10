package br.com.oficinasampaio.veiculo.application;

import br.com.oficinasampaio.veiculo.domain.Veiculo;

import java.util.UUID;

public record VeiculoView(
        UUID id,
        UUID clienteId,
        String placa,
        String marca,
        String modelo,
        Integer ano,
        String cor,
        Long quilometragem,
        boolean ativo
) {

    static VeiculoView de(Veiculo veiculo) {
        return new VeiculoView(
                veiculo.getId(),
                veiculo.getClienteId(),
                veiculo.getPlaca(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getAno(),
                veiculo.getCor(),
                veiculo.getQuilometragem(),
                veiculo.isAtivo()
        );
    }
}
