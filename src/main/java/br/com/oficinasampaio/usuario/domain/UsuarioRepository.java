package br.com.oficinasampaio.usuario.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository {

    Usuario salvar(Usuario usuario);

    boolean salvarSeLoginAusente(Usuario usuario);

    boolean existePorLogin(String login);

    Optional<Usuario> buscarAtivoPorLogin(String login);

    Optional<Usuario> buscarPorId(UUID usuarioId);

    List<Usuario> listar();
}
