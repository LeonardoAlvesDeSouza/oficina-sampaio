package br.com.oficinasampaio.cliente.infrastructure.persistence;

import br.com.oficinasampaio.cliente.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SpringDataClienteRepository extends JpaRepository<Cliente, UUID> {

    boolean existsByCpfCnpj(String cpfCnpj);

    List<Cliente> findAllByOrderByNomeAsc();
}
