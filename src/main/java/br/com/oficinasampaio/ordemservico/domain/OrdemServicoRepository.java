package br.com.oficinasampaio.ordemservico.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrdemServicoRepository {

    OrdemServico salvar(OrdemServico ordemServico);

    Optional<OrdemServico> buscarPorId(UUID ordemServicoId);

    List<OrdemServico> listar();
}
