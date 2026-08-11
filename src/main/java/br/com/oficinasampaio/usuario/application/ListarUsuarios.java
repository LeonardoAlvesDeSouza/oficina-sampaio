package br.com.oficinasampaio.usuario.application;

import br.com.oficinasampaio.usuario.domain.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListarUsuarios {

    private final UsuarioRepository usuarioRepository;

    public ListarUsuarios(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<UsuarioView> executar() {
        return usuarioRepository.listar().stream().map(UsuarioView::de).toList();
    }
}
