package br.com.oficinasampaio.ordemservico.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrdemServicoRepository {

    OrdemServico salvar(OrdemServico ordemServico);

    Optional<OrdemServico> buscarPorId(UUID ordemServicoId);

    List<OrdemServico> listar();

    /**
     * Ordens com o valor fechado e a conta em aberto, das mais antigas para as
     * mais novas: o que está esperando pagamento há mais tempo aparece primeiro.
     */
    List<OrdemServico> listarAReceber();
}
