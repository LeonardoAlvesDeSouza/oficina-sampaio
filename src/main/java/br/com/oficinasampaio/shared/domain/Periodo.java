package br.com.oficinasampaio.shared.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Uma janela de tempo fechada nas duas pontas, usada pelas consultas de período.
 * <p>
 * A oficina pensa em dias de calendário, o banco guarda instantes em UTC. A
 * conversão acontece aqui, de uma vez: o dia informado entra inteiro, do primeiro
 * instante ao último. Sem isso, um relatório "de 1 a 19" perderia tudo o que
 * aconteceu no dia 19 depois da meia-noite.
 */
public record Periodo(Instant inicio, Instant fim) {

    public Periodo {
        Objects.requireNonNull(inicio, "Início do período é obrigatório");
        Objects.requireNonNull(fim, "Fim do período é obrigatório");
        if (fim.isBefore(inicio)) {
            throw new RegraNegocioException("O fim do período não pode ser antes do início");
        }
    }

    public static Periodo deDias(LocalDate inicio, LocalDate fim, ZoneId fuso) {
        Objects.requireNonNull(inicio, "Data inicial é obrigatória");
        Objects.requireNonNull(fim, "Data final é obrigatória");
        if (fim.isBefore(inicio)) {
            throw new RegraNegocioException("A data final não pode ser antes da inicial");
        }
        return new Periodo(
                inicio.atStartOfDay(fuso).toInstant(),
                fim.plusDays(1).atStartOfDay(fuso).toInstant().minusMillis(1)
        );
    }
}
