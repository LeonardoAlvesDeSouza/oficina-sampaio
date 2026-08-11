package br.com.oficinasampaio.veiculo.application;

import java.util.UUID;

public record VeiculoParaOrdem(
        UUID id,
        UUID clienteId,
        String placa,
        String marca,
        String modelo,
        boolean ativo
) {
}
