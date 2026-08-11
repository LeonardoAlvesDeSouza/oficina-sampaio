package br.com.oficinasampaio.usuario.application;

import br.com.oficinasampaio.usuario.domain.Usuario;

import java.util.UUID;

public record UsuarioView(
        UUID id,
        String nome,
        String login,
        PerfilUsuarioView perfil,
        boolean ativo
) {
    public static UsuarioView de(Usuario usuario) {
        return new UsuarioView(
                usuario.getId(),
                usuario.getNome(),
                usuario.getLogin(),
                PerfilUsuarioView.de(usuario.getPerfil()),
                usuario.isAtivo()
        );
    }
}
