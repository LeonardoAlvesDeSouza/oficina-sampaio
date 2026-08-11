package br.com.oficinasampaio.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.bootstrap-admin")
public record AdministradorInicialProperties(
        boolean enabled,
        String nome,
        String login,
        String senha
) {
}
