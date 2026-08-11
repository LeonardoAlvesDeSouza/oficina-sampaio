package br.com.oficinasampaio.ordemservico.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrdemServicoTest {

    @Test
    void abreOrdemSemItensComStatusAberta() {
        var clienteId = UUID.randomUUID();
        var veiculoId = UUID.randomUUID();
        var abertaEm = Instant.parse("2026-08-11T12:00:00Z");

        var ordem = OrdemServico.abrir(
                clienteId, veiculoId, "  Ruído na suspensão dianteira ", abertaEm
        );

        assertAll(
                () -> assertEquals(clienteId, ordem.getClienteId()),
                () -> assertEquals(veiculoId, ordem.getVeiculoId()),
                () -> assertEquals("Ruído na suspensão dianteira", ordem.getRelatoProblema()),
                () -> assertEquals(abertaEm, ordem.getAbertaEm()),
                () -> assertEquals(StatusOrdemServico.ABERTA, ordem.getStatus()),
                () -> assertEquals(new BigDecimal("0.00"), ordem.getTotalServicos()),
                () -> assertEquals(new BigDecimal("0.00"), ordem.getTotalPecas()),
                () -> assertEquals(new BigDecimal("0.00"), ordem.getTotal())
        );
    }

    @Test
    void adicionaServicosEPecasECalculaTotais() {
        var ordem = OrdemServico.abrir(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Revisão da suspensão",
                Instant.parse("2026-08-11T12:00:00Z")
        );

        ordem.adicionarServico("Alinhamento", new BigDecimal("1.5"), new BigDecimal("120.00"));
        ordem.adicionarPeca("Amortecedor dianteiro", new BigDecimal("2"), new BigDecimal("350.50"));

        assertAll(
                () -> assertEquals(2, ordem.getItens().size()),
                () -> assertEquals(TipoItemOrdemServico.SERVICO, ordem.getItens().get(0).getTipo()),
                () -> assertEquals(TipoItemOrdemServico.PECA, ordem.getItens().get(1).getTipo()),
                () -> assertEquals(new BigDecimal("180.00"), ordem.getTotalServicos()),
                () -> assertEquals(new BigDecimal("701.00"), ordem.getTotalPecas()),
                () -> assertEquals(new BigDecimal("881.00"), ordem.getTotal())
        );
    }

    @Test
    void impedeItemComQuantidadeNaoPositiva() {
        var ordem = OrdemServico.abrir(
                UUID.randomUUID(), UUID.randomUUID(), "Revisão",
                Instant.parse("2026-08-11T12:00:00Z")
        );

        var erro = assertThrows(IllegalArgumentException.class, () ->
                ordem.adicionarServico("Alinhamento", BigDecimal.ZERO, new BigDecimal("120.00"))
        );

        assertEquals("Quantidade deve ser positiva", erro.getMessage());
        assertEquals(0, ordem.getItens().size());
    }

    @Test
    void impedeValorUnitarioQueArredondariaParaZero() {
        var ordem = OrdemServico.abrir(
                UUID.randomUUID(), UUID.randomUUID(), "Revisão",
                Instant.parse("2026-08-11T12:00:00Z")
        );

        var erro = assertThrows(IllegalArgumentException.class, () ->
                ordem.adicionarPeca("Arruela", BigDecimal.ONE, new BigDecimal("0.001"))
        );

        assertEquals("Valor unitário deve ser positivo", erro.getMessage());
        assertEquals(0, ordem.getItens().size());
    }
}
