package br.com.oficinasampaio.ordemservico.presentation;

import br.com.oficinasampaio.ordemservico.application.StatusOrdemServicoView;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A trilha é a leitura visual da máquina de estados, e as duas decisões que ela
 * toma são regra, não enfeite: "aguardando peça" não é uma parada própria (é a
 * execução travada) e "cancelada" não é parada nenhuma (é saída da linha).
 * Se alguém acrescentar um estado sem pensar nisso, o build avisa.
 */
class TrilhaOrdemServicoTest {

    private static final List<String> PARADAS =
            List.of("Aberta", "Em execução", "Finalizada", "Entregue");

    @Test
    void trilhaTemSempreAsQuatroParadasDoCiclo() {
        var trilha = TrilhaOrdemServico.de(StatusOrdemServicoView.ABERTA);

        assertEquals(PARADAS, trilha.stream().map(TrilhaOrdemServico.ParadaOrdem::rotulo).toList());
    }

    @Test
    void ordemAbertaTemAPrimeiraParadaComoAtualEORestoPendente() {
        var trilha = TrilhaOrdemServico.de(StatusOrdemServicoView.ABERTA);

        assertAll(
                () -> assertEquals("ATUAL", etapa(trilha, 0)),
                () -> assertEquals("PENDENTE", etapa(trilha, 1)),
                () -> assertEquals("PENDENTE", etapa(trilha, 2)),
                () -> assertEquals("PENDENTE", etapa(trilha, 3))
        );
    }

    @Test
    void ordemEmExecucaoDeixaAAberturaConcluidaAtrasDeSi() {
        var trilha = TrilhaOrdemServico.de(StatusOrdemServicoView.EM_EXECUCAO);

        assertAll(
                () -> assertEquals("CONCLUIDA", etapa(trilha, 0)),
                () -> assertEquals("ATUAL", etapa(trilha, 1)),
                () -> assertNull(trilha.get(1).nota())
        );
    }

    @Test
    void aguardandoPecaTravaAParadaDaExecucaoEmVezDeCriarUmaParadaPropria() {
        var trilha = TrilhaOrdemServico.de(StatusOrdemServicoView.AGUARDANDO_PECA);

        assertAll(
                () -> assertEquals(PARADAS.size(), trilha.size()),
                () -> assertEquals("Em execução", trilha.get(1).rotulo()),
                () -> assertEquals("TRAVADA", etapa(trilha, 1)),
                () -> assertEquals("aguardando peça", trilha.get(1).nota())
        );
    }

    @Test
    void ordemEntregueTemTodasAsParadasAnterioresConcluidas() {
        var trilha = TrilhaOrdemServico.de(StatusOrdemServicoView.ENTREGUE);

        assertAll(
                () -> assertEquals("CONCLUIDA", etapa(trilha, 0)),
                () -> assertEquals("CONCLUIDA", etapa(trilha, 1)),
                () -> assertEquals("CONCLUIDA", etapa(trilha, 2)),
                () -> assertEquals("ATUAL", etapa(trilha, 3))
        );
    }

    @Test
    void ordemCanceladaNaoPercorreTrilhaNenhuma() {
        assertTrue(TrilhaOrdemServico.de(StatusOrdemServicoView.CANCELADA).isEmpty());
    }

    private static String etapa(List<TrilhaOrdemServico.ParadaOrdem> trilha, int parada) {
        return trilha.get(parada).etapa();
    }
}
