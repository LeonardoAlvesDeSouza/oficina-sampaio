package br.com.oficinasampaio.shared.presentation;

import org.springframework.security.core.Authentication;

public final class PerfilAutenticado {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private PerfilAutenticado() {
    }

    public static boolean ehAdministrador(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> ROLE_ADMIN.equals(authority.getAuthority()));
    }
}
