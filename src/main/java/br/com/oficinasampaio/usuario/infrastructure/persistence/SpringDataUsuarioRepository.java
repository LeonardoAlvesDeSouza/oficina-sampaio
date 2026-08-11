package br.com.oficinasampaio.usuario.infrastructure.persistence;

import br.com.oficinasampaio.usuario.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SpringDataUsuarioRepository extends JpaRepository<Usuario, UUID> {

    @Modifying
    @Query(value = """
            insert into usuarios (id, nome, login, senha_hash, perfil, ativo, versao)
            values (gen_random_uuid(), :nome, :login, :senhaHash, :perfil, true, 0)
            on conflict (login) do nothing
            """, nativeQuery = true)
    int inserirSeLoginAusente(
            @Param("nome") String nome,
            @Param("login") String login,
            @Param("senhaHash") String senhaHash,
            @Param("perfil") String perfil
    );

    boolean existsByLoginIgnoreCase(String login);

    Optional<Usuario> findByLoginIgnoreCaseAndAtivoTrue(String login);

    List<Usuario> findAllByOrderByNomeAsc();
}
