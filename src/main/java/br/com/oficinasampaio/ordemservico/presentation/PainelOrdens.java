package br.com.oficinasampaio.ordemservico.presentation;

import br.com.oficinasampaio.ordemservico.application.StatusOrdemServicoView;

import java.util.List;

/**
 * O quadro de ordens do dia, organizado como a oficina funciona: primeiro o que
 * está na mão, depois o que está travado, por último o que já saiu do pátio.
 */
public record PainelOrdens(
        List<FaixaCarga> carga,
        long naOficina,
        long foraDoPatio,
        List<GrupoOrdens> grupos
) {

    /** Carros que ainda estão na oficina — entram na fita de carga. */
    private static final List<StatusOrdemServicoView> NO_PATIO = List.of(
            StatusOrdemServicoView.EM_EXECUCAO,
            StatusOrdemServicoView.AGUARDANDO_PECA,
            StatusOrdemServicoView.ABERTA,
            StatusOrdemServicoView.FINALIZADA
    );

    /** Ordem de leitura do quadro: o que exige ação primeiro. */
    private static final List<StatusOrdemServicoView> ORDEM_DE_LEITURA = List.of(
            StatusOrdemServicoView.EM_EXECUCAO,
            StatusOrdemServicoView.AGUARDANDO_PECA,
            StatusOrdemServicoView.ABERTA,
            StatusOrdemServicoView.FINALIZADA,
            StatusOrdemServicoView.ENTREGUE,
            StatusOrdemServicoView.CANCELADA
    );

    public static PainelOrdens montar(List<OrdemServicoLinha> linhas) {
        var carga = NO_PATIO.stream()
                .map(status -> new FaixaCarga(status, contar(linhas, status)))
                .filter(faixa -> faixa.quantidade() > 0)
                .toList();

        var grupos = ORDEM_DE_LEITURA.stream()
                .map(status -> new GrupoOrdens(status, filtrar(linhas, status)))
                .filter(grupo -> !grupo.ordens().isEmpty())
                .toList();

        var naOficina = carga.stream().mapToLong(FaixaCarga::quantidade).sum();

        return new PainelOrdens(carga, naOficina, linhas.size() - naOficina, grupos);
    }

    private static long contar(List<OrdemServicoLinha> linhas, StatusOrdemServicoView status) {
        return linhas.stream().filter(linha -> linha.status() == status).count();
    }

    private static List<OrdemServicoLinha> filtrar(
            List<OrdemServicoLinha> linhas,
            StatusOrdemServicoView status
    ) {
        return linhas.stream().filter(linha -> linha.status() == status).toList();
    }

    /** Um pedaço da fita de carga: quanto do pátio está neste estado. */
    public record FaixaCarga(StatusOrdemServicoView status, long quantidade) {
    }

    /** Um bloco do quadro: todas as ordens em um mesmo estado. */
    public record GrupoOrdens(StatusOrdemServicoView status, List<OrdemServicoLinha> ordens) {
    }
}
