package br.com.oficinasampaio.usuario.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 80, unique = true)
    private String login;

    @Column(name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PerfilUsuario perfil;

    @Column(nullable = false)
    private boolean ativo;

    @Version
    private long versao;

    protected Usuario() {
    }

    private Usuario(String nome, String login, String senhaHash, PerfilUsuario perfil) {
        this.nome = textoObrigatorio(nome, "Nome");
        this.login = textoObrigatorio(login, "Login").toLowerCase(Locale.ROOT);
        this.senhaHash = textoObrigatorio(senhaHash, "Senha codificada");
        this.perfil = Objects.requireNonNull(perfil, "Perfil é obrigatório");
        this.ativo = true;
    }

    public static Usuario cadastrar(String nome, String login, String senhaHash, PerfilUsuario perfil) {
        return new Usuario(nome, login, senhaHash, perfil);
    }

    public void inativar() {
        this.ativo = false;
    }

    private static String textoObrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " é obrigatório");
        }
        return valor.trim();
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getLogin() {
        return login;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
