package br.com.oficinasampaio.usuario.presentation;

import br.com.oficinasampaio.usuario.application.PerfilUsuarioView;
import br.com.oficinasampaio.usuario.application.PoliticaSenhaUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UsuarioForm {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 120, message = "Nome deve possuir no máximo 120 caracteres")
    private String nome;

    @NotBlank(message = "Login é obrigatório")
    @Pattern(
            regexp = "^[a-zA-Z0-9._-]{3,80}$",
            message = "Login deve possuir de 3 a 80 letras, números, pontos, hífens ou sublinhados"
    )
    private String login;

    @NotBlank(message = "Senha é obrigatória")
    @Size(
            min = PoliticaSenhaUsuario.TAMANHO_MINIMO,
            max = PoliticaSenhaUsuario.TAMANHO_MAXIMO,
            message = PoliticaSenhaUsuario.MENSAGEM_TAMANHO
    )
    private String senha;

    @NotNull(message = "Perfil é obrigatório")
    private PerfilUsuarioView perfil;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public PerfilUsuarioView getPerfil() {
        return perfil;
    }

    public void setPerfil(PerfilUsuarioView perfil) {
        this.perfil = perfil;
    }
}
