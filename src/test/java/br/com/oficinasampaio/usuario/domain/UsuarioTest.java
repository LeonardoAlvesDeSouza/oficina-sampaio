package br.com.oficinasampaio.usuario.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioTest {

    @Test
    void cadastraUsuarioAtivoComLoginNormalizado() {
        var usuario = Usuario.cadastrar(
                "  Maria Administradora ",
                "  MARIA.Admin ",
                "$2a$10$senhaCodificada",
                PerfilUsuario.ADMIN
        );

        assertAll(
                () -> assertEquals("Maria Administradora", usuario.getNome()),
                () -> assertEquals("maria.admin", usuario.getLogin()),
                () -> assertEquals("$2a$10$senhaCodificada", usuario.getSenhaHash()),
                () -> assertEquals(PerfilUsuario.ADMIN, usuario.getPerfil()),
                () -> assertTrue(usuario.isAtivo())
        );
    }

    @Test
    void usuarioPodeSerInativado() {
        var usuario = Usuario.cadastrar(
                "João Funcionário", "joao", "senha-codificada", PerfilUsuario.FUNCIONARIO
        );

        usuario.inativar();

        assertFalse(usuario.isAtivo());
    }
}
