package br.com.oficinasampaio.usuario.application;

public record CredenciaisUsuario(
        String login,
        String senhaHash,
        PerfilUsuarioView perfil
) {
}
