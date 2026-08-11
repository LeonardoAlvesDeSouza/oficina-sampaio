package br.com.oficinasampaio.usuario.application;

import br.com.oficinasampaio.usuario.domain.PerfilUsuario;

public enum PerfilUsuarioView {
    ADMIN(PerfilUsuario.ADMIN),
    FUNCIONARIO(PerfilUsuario.FUNCIONARIO);

    private final PerfilUsuario perfilDomain;

    PerfilUsuarioView(PerfilUsuario perfilDomain) {
        this.perfilDomain = perfilDomain;
    }

    PerfilUsuario paraDominio() {
        return perfilDomain;
    }

    static PerfilUsuarioView de(PerfilUsuario perfil) {
        return valueOf(perfil.name());
    }
}
