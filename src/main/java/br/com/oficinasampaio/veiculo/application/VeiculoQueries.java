package br.com.oficinasampaio.veiculo.application;

import br.com.oficinasampaio.shared.domain.RecursoNaoEncontradoException;

import java.util.Optional;
import java.util.UUID;

public interface VeiculoQueries {

    Optional<VeiculoParaOrdem> buscarPorId(UUID veiculoId);

    default Optional<VeiculoParaOrdem> buscarAtivo(UUID veiculoId) {
        return buscarPorId(veiculoId).filter(VeiculoParaOrdem::ativo);
    }

    default VeiculoParaOrdem obterPorId(UUID veiculoId) {
        return buscarPorId(veiculoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo não encontrado"));
    }

    default VeiculoParaOrdem obterAtivo(UUID veiculoId) {
        return buscarAtivo(veiculoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo ativo não encontrado"));
    }
}
