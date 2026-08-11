package br.com.oficinasampaio.ordemservico.application;

import java.util.UUID;

public record AbrirOrdemServicoCommand(
        UUID veiculoId,
        String relatoProblema
) {
}
