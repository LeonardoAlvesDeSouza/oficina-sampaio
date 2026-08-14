package br.com.oficinasampaio.ordemservico.application;

import java.util.UUID;

public record RemoverItemOrdemServicoCommand(
        UUID ordemServicoId,
        UUID itemId
) {
}
