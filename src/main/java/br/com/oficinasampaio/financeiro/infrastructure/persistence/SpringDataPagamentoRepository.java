package br.com.oficinasampaio.financeiro.infrastructure.persistence;

import br.com.oficinasampaio.financeiro.domain.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SpringDataPagamentoRepository extends JpaRepository<Pagamento, UUID> {

    boolean existsByOrdemServicoId(UUID ordemServicoId);

    Optional<Pagamento> findByOrdemServicoId(UUID ordemServicoId);

    List<Pagamento> findAllByOrderByRegistradoEmDesc();

    List<Pagamento> findByRegistradoEmBetweenOrderByRegistradoEmDesc(Instant inicio, Instant fim);
}