package br.com.oficinasampaio.cliente.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClienteTest {

    @Test
    void cadastraClienteAtivoComDadosNormalizados() {
        var cliente = Cliente.cadastrar(
                "  Maria da Silva  ",
                "529.982.247-25",
                "(11) 99999-8888",
                "  MARIA@EXAMPLE.COM  "
        );

        assertAll(
                () -> assertEquals("Maria da Silva", cliente.getNome()),
                () -> assertEquals("52998224725", cliente.getCpfCnpj()),
                () -> assertEquals("11999998888", cliente.getTelefone()),
                () -> assertEquals("maria@example.com", cliente.getEmail()),
                () -> assertTrue(cliente.isAtivo())
        );
    }

    @Test
    void inativaClientePreservandoSeuCadastro() {
        var cliente = Cliente.cadastrar("Maria da Silva", null, null, null);

        cliente.inativar();

        assertEquals("Maria da Silva", cliente.getNome());
        assertTrue(cliente.isInativo());
    }
}
