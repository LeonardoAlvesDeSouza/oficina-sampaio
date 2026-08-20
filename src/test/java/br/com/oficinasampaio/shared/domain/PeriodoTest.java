package br.com.oficinasampaio.shared.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PeriodoTest {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    /**
     * O último dia entra inteiro. Se o fim fosse a meia-noite do dia informado, um
     * relatório "de 1 a 19" perderia tudo o que aconteceu no dia 19 — que é
     * justamente o dia que a pessoa quer ver quando pede o mês corrente.
     */
    @Test
    void transformaDiasDeCalendarioEmInstantesCobrindoOsDiasInteiros() {
        var periodo = Periodo.deDias(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 19), FUSO
        );

        assertAll(
                () -> assertEquals(Instant.parse("2026-08-01T03:00:00Z"), periodo.inicio()),
                () -> assertEquals(Instant.parse("2026-08-20T02:59:59.999Z"), periodo.fim())
        );
    }

    @Test
    void umDiaSoEUmPeriodoValido() {
        var dia = LocalDate.of(2026, 8, 19);

        var periodo = Periodo.deDias(dia, dia, FUSO);

        assertAll(
                () -> assertEquals(Instant.parse("2026-08-19T03:00:00Z"), periodo.inicio()),
                () -> assertEquals(Instant.parse("2026-08-20T02:59:59.999Z"), periodo.fim())
        );
    }

    @Test
    void recusaDataFinalAntesDaInicial() {
        var erro = assertThrows(RegraNegocioException.class, () -> Periodo.deDias(
                LocalDate.of(2026, 8, 19), LocalDate.of(2026, 8, 1), FUSO
        ));

        assertEquals("A data final não pode ser antes da inicial", erro.getMessage());
    }

    @Test
    void recusaInstantesInvertidos() {
        var erro = assertThrows(RegraNegocioException.class, () -> new Periodo(
                Instant.parse("2026-08-19T03:00:00Z"), Instant.parse("2026-08-01T03:00:00Z")
        ));

        assertEquals("O fim do período não pode ser antes do início", erro.getMessage());
    }
}
