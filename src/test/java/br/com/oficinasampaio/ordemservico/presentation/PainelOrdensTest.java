package br.com.oficinasampaio.ordemservico.presentation;

import br.com.oficinasampaio.ordemservico.application.StatusOrdemServicoView;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * O quadro é lido de cima para baixo por quem está no balcão, e a ordem dessa
 * leitura é a regra: primeiro o que está na mão, depois o que está travado, por
 * último o que já saiu do pátio. Também é regra o que conta como "na oficina" —
 * é esse número que diz se a oficina está cheia.
 */
class PainelOrdensTest {

    @Test
    void grupoVemNaOrdemDeLeituraDoQuadroEIgnoraEstadoSemOrdem() {
        var painel = PainelOrdens.montar(List.of(
                linha(StatusOrdemServicoView.ENTREGUE),
                linha(StatusOrdemServicoView.ABERTA),
                linha(StatusOrdemServicoView.EM_EXECUCAO)
        ));

        assertEquals(
                List.of(
                        StatusOrdemServicoView.EM_EXECUCAO,
                        StatusOrdemServicoView.ABERTA,
                        StatusOrdemServicoView.ENTREGUE
                ),
                painel.grupos().stream().map(PainelOrdens.GrupoOrdens::status).toList()
        );
    }

    @Test
    void contaComoNaOficinaSoOQueAindaEstaNoPatio() {
        var painel = PainelOrdens.montar(List.of(
                linha(StatusOrdemServicoView.EM_EXECUCAO),
                linha(StatusOrdemServicoView.AGUARDANDO_PECA),
                linha(StatusOrdemServicoView.ABERTA),
                linha(StatusOrdemServicoView.FINALIZADA),
                linha(StatusOrdemServicoView.ENTREGUE),
                linha(StatusOrdemServicoView.CANCELADA)
        ));

        assertAll(
                () -> assertEquals(4, painel.naOficina()),
                () -> assertEquals(2, painel.foraDoPatio())
        );
    }

    @Test
    void fitaDeCargaTrazAQuantidadePorEstadoNaOrdemDoPatio() {
        var painel = PainelOrdens.montar(List.of(
                linha(StatusOrdemServicoView.ABERTA),
                linha(StatusOrdemServicoView.EM_EXECUCAO),
                linha(StatusOrdemServicoView.EM_EXECUCAO),
                linha(StatusOrdemServicoView.ENTREGUE)
        ));

        assertEquals(
                List.of("EM_EXECUCAO:2", "ABERTA:1"),
                painel.carga().stream()
                        .map(faixa -> faixa.status().name() + ":" + faixa.quantidade())
                        .toList()
        );
    }

    @Test
    void ordemQueSaiuDoPatioNaoEntraNaFitaMasContinuaNoQuadro() {
        var painel = PainelOrdens.montar(List.of(linha(StatusOrdemServicoView.CANCELADA)));

        assertAll(
                () -> assertTrue(painel.carga().isEmpty()),
                () -> assertEquals(0, painel.naOficina()),
                () -> assertEquals(1, painel.grupos().size())
        );
    }

    @Test
    void oficinaSemOrdemNenhumaNaoTemFitaNemGrupo() {
        var painel = PainelOrdens.montar(List.of());

        assertAll(
                () -> assertTrue(painel.carga().isEmpty()),
                () -> assertTrue(painel.grupos().isEmpty()),
                () -> assertEquals(0, painel.naOficina()),
                () -> assertEquals(0, painel.foraDoPatio())
        );
    }

    private static OrdemServicoLinha linha(StatusOrdemServicoView status) {
        return new OrdemServicoLinha(
                UUID.randomUUID(),
                "4a9c0039",
                "Barulho ao frear",
                status,
                "ABC1D23",
                "Volkswagen Gol",
                "Marcos Ferreira",
                "12/08/26 às 14:07",
                new BigDecimal("649.80")
        );
    }
}
