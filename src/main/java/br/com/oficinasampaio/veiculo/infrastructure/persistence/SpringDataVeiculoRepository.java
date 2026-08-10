package br.com.oficinasampaio.veiculo.infrastructure.persistence;

import br.com.oficinasampaio.veiculo.domain.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SpringDataVeiculoRepository extends JpaRepository<Veiculo, UUID> {

    boolean existsByPlaca(String placa);

    List<Veiculo> findAllByClienteIdOrderByPlacaAsc(UUID clienteId);
}
