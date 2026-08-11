package br.com.oficinasampaio.security;

import br.com.oficinasampaio.usuario.application.BuscarCredenciaisUsuario;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioUserDetailsService implements UserDetailsService {

    private final BuscarCredenciaisUsuario buscarCredenciaisUsuario;

    public UsuarioUserDetailsService(BuscarCredenciaisUsuario buscarCredenciaisUsuario) {
        this.buscarCredenciaisUsuario = buscarCredenciaisUsuario;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        var credenciais = buscarCredenciaisUsuario.executar(login)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        return User.withUsername(credenciais.login())
                .password(credenciais.senhaHash())
                .roles(credenciais.perfil().name())
                .build();
    }
}
