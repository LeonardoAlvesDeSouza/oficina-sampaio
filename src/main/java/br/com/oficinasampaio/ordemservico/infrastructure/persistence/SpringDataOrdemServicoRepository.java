package br.com.oficinasampaio.ordemservico.infrastructure.persistence;

import br.com.oficinasampaio.ordemservico.domain.OrdemServico;
import br.com.oficinasampaio.ordemservico.domain.StatusOrdemServico;
import br.com.oficinasampaio.ordemservico.domain.StatusPagamento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SpringDataOrdemServicoRepository extends JpaRepository<OrdemServico, UUID> {

    @Override
    @EntityGraph(attributePaths = "itens")
    Optional<OrdemServico> findById(UUID id);

    @EntityGraph(attributePaths = "itens")
    List<OrdemServico> findAllByOrderByAbertaEmDesc();

    @EntityGraph(attributePaths = "itens")
    List<OrdemServico> findByStatusPagamentoAndStatusInOrderByAbertaEmAsc(
            StatusPagamento statusPagamento,
            Collection<StatusOrdemServico> status
    );
}
