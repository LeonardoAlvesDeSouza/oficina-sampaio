package br.com.oficinasampaio.ordemservico.domain;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

public enum AcaoOrdemServico {
    INICIAR_EXECUCAO(
            StatusOrdemServico.EM_EXECUCAO,
            "A ordem só pode iniciar quando está aberta",
            StatusOrdemServico.ABERTA
    ) {
        @Override
        boolean atendePrecondicoes(OrdemServico ordem) {
            return ordem.possuiItens();
        }

        @Override
        String mensagemIndisponivel(OrdemServico ordem) {
            if (ordem.getStatus() == StatusOrdemServico.ABERTA && !ordem.possuiItens()) {
                return "Inclua ao menos um item antes de iniciar a execução";
            }
            return super.mensagemIndisponivel(ordem);
        }
    },
    AGUARDAR_PECA(
            StatusOrdemServico.AGUARDANDO_PECA,
            "Apenas uma ordem em execução pode aguardar peça",
            StatusOrdemServico.EM_EXECUCAO
    ),
    RETOMAR_EXECUCAO(
            StatusOrdemServico.EM_EXECUCAO,
            "Apenas uma ordem aguardando peça pode retomar a execução",
            StatusOrdemServico.AGUARDANDO_PECA
    ),
    FINALIZAR(
            StatusOrdemServico.FINALIZADA,
            "Apenas uma ordem em execução pode ser finalizada",
            StatusOrdemServico.EM_EXECUCAO
    ),
    ENTREGAR(
            StatusOrdemServico.ENTREGUE,
            "Apenas uma ordem finalizada pode ser entregue",
            StatusOrdemServico.FINALIZADA
    ),
    CANCELAR(
            StatusOrdemServico.CANCELADA,
            "A ordem entregue ou cancelada não pode ser cancelada",
            StatusOrdemServico.ABERTA,
            StatusOrdemServico.EM_EXECUCAO,
            StatusOrdemServico.AGUARDANDO_PECA,
            StatusOrdemServico.FINALIZADA
    );

    private final StatusOrdemServico destino;
    private final String mensagemIndisponivel;
    private final Set<StatusOrdemServico> origens;

    AcaoOrdemServico(
            StatusOrdemServico destino,
            String mensagemIndisponivel,
            StatusOrdemServico... origens
    ) {
        this.destino = destino;
        this.mensagemIndisponivel = mensagemIndisponivel;
        this.origens = EnumSet.copyOf(Arrays.asList(origens));
    }

    boolean disponivelPara(OrdemServico ordem) {
        return origens.contains(ordem.getStatus()) && atendePrecondicoes(ordem);
    }

    boolean atendePrecondicoes(OrdemServico ordem) {
        return true;
    }

    String mensagemIndisponivel(OrdemServico ordem) {
        return mensagemIndisponivel;
    }

    StatusOrdemServico getDestino() {
        return destino;
    }
}
