package br.com.oficinasampaio.usuario.application;

public record CadastrarUsuarioCommand(
        String nome,
        String login,
        String senha,
        PerfilUsuarioView perfil
) {
}
