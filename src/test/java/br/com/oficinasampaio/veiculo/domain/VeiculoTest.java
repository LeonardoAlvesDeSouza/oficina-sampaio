package br.com.oficinasampaio.veiculo.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VeiculoTest {

    @Test
    void cadastraVeiculoAtivoComPlacaNormalizada() {
        var clienteId = UUID.randomUUID();

        var veiculo = Veiculo.cadastrar(
                clienteId,
                "abc-1d23",
                "  Volkswagen ",
                " Gol ",
                2022,
                "Prata",
                35_000L
        );

        assertAll(
                () -> assertEquals(clienteId, veiculo.getClienteId()),
                () -> assertEquals("ABC1D23", veiculo.getPlaca()),
                () -> assertEquals("Volkswagen", veiculo.getMarca()),
                () -> assertEquals("Gol", veiculo.getModelo()),
                () -> assertEquals(2022, veiculo.getAno()),
                () -> assertEquals(35_000L, veiculo.getQuilometragem()),
                () -> assertTrue(veiculo.isAtivo())
        );
    }

    @Test
    void impedeCadastroComQuilometragemNegativa() {
        var erro = assertThrows(IllegalArgumentException.class, () ->
                Veiculo.cadastrar(
                        UUID.randomUUID(), "ABC1D23", "Volkswagen", "Gol",
                        2022, "Prata", -1L
                )
        );

        assertEquals("Quilometragem não pode ser negativa", erro.getMessage());
    }
}
