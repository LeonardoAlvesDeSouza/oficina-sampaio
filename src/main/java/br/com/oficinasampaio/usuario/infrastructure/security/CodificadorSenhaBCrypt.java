package br.com.oficinasampaio.usuario.infrastructure.security;

import br.com.oficinasampaio.usuario.application.CodificadorSenha;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CodificadorSenhaBCrypt implements CodificadorSenha {

    private final PasswordEncoder passwordEncoder;

    public CodificadorSenhaBCrypt(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String codificar(String senha) {
        return passwordEncoder.encode(senha);
    }
}
