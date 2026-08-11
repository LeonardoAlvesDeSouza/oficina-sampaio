package br.com.oficinasampaio.usuario.infrastructure.persistence;

import br.com.oficinasampaio.usuario.domain.Usuario;
import br.com.oficinasampaio.usuario.domain.UsuarioRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UsuarioRepositoryAdapter implements UsuarioRepository {

    private final SpringDataUsuarioRepository repository;

    public UsuarioRepositoryAdapter(SpringDataUsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        return repository.save(usuario);
    }

    @Override
    public boolean salvarSeLoginAusente(Usuario usuario) {
        return repository.inserirSeLoginAusente(
                usuario.getNome(),
                usuario.getLogin(),
                usuario.getSenhaHash(),
                usuario.getPerfil().name()
        ) == 1;
    }

    @Override
    public boolean existePorLogin(String login) {
        return repository.existsByLoginIgnoreCase(login);
    }

    @Override
    public Optional<Usuario> buscarAtivoPorLogin(String login) {
        return repository.findByLoginIgnoreCaseAndAtivoTrue(login);
    }

    @Override
    public Optional<Usuario> buscarPorId(UUID usuarioId) {
        return repository.findById(usuarioId);
    }

    @Override
    public List<Usuario> listar() {
        return repository.findAllByOrderByNomeAsc();
    }
}
