package br.com.oficinasampaio.usuario.application;

import br.com.oficinasampaio.usuario.domain.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BuscarCredenciaisUsuario {

    private final UsuarioRepository usuarioRepository;

    public BuscarCredenciaisUsuario(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public Optional<CredenciaisUsuario> executar(String login) {
        return usuarioRepository.buscarAtivoPorLogin(login.trim())
                .map(usuario -> new CredenciaisUsuario(
                        usuario.getLogin(),
                        usuario.getSenhaHash(),
                        PerfilUsuarioView.de(usuario.getPerfil())
                ));
    }
}
