package br.com.oficinasampaio.ordemservico.presentation;

import br.com.oficinasampaio.ordemservico.application.StatusOrdemServicoView;

import java.util.List;

/**
 * A trilha que a ordem percorre, para a tela mostrar onde ela está.
 * <p>
 * São quatro paradas, não seis. "Aguardando peça" não é uma parada própria: é a
 * execução travada, e aparece como a parada de execução em estado
 * {@code TRAVADA}. Cancelamento não é parada nenhuma — é saída da linha, então a
 * trilha vem vazia e a tela mostra o aviso de cancelamento no lugar dela.
 */
public final class TrilhaOrdemServico {

    private static final int PARADA_ABERTURA = 0;
    private static final int PARADA_EXECUCAO = 1;
    private static final int PARADA_FINALIZACAO = 2;
    private static final int PARADA_ENTREGA = 3;

    private TrilhaOrdemServico() {
    }

    public static List<ParadaOrdem> de(StatusOrdemServicoView status) {
        if (status == StatusOrdemServicoView.CANCELADA) {
            return List.of();
        }

        var atual = paradaAtual(status);
        var travada = status == StatusOrdemServicoView.AGUARDANDO_PECA;

        return List.of(
                parada("Aberta", PARADA_ABERTURA, atual, false),
                parada("Em execução", PARADA_EXECUCAO, atual, travada),
                parada("Finalizada", PARADA_FINALIZACAO, atual, false),
                parada("Entregue", PARADA_ENTREGA, atual, false)
        );
    }

    private static ParadaOrdem parada(String rotulo, int parada, int atual, boolean travada) {
        if (parada < atual) {
            return new ParadaOrdem(rotulo, "CONCLUIDA", null);
        }
        if (parada > atual) {
            return new ParadaOrdem(rotulo, "PENDENTE", null);
        }
        return travada
                ? new ParadaOrdem(rotulo, "TRAVADA", "aguardando peça")
                : new ParadaOrdem(rotulo, "ATUAL", null);
    }

    private static int paradaAtual(StatusOrdemServicoView status) {
        return switch (status) {
            case ABERTA -> PARADA_ABERTURA;
            case EM_EXECUCAO, AGUARDANDO_PECA -> PARADA_EXECUCAO;
            case FINALIZADA -> PARADA_FINALIZACAO;
            case ENTREGUE -> PARADA_ENTREGA;
            case CANCELADA -> throw new IllegalStateException("Ordem cancelada não percorre a trilha");
        };
    }

    /**
     * @param etapa CONCLUIDA, ATUAL, TRAVADA ou PENDENTE — é o que o CSS pinta
     * @param nota  motivo da parada, quando há um
     */
    public record ParadaOrdem(String rotulo, String etapa, String nota) {
    }
}
