package br.com.oficinasampaio.ordemservico.application;

import java.util.UUID;

public record AlterarStatusOrdemServicoCommand(
        UUID ordemServicoId,
        AcaoOrdemServicoView acao
) {
}
