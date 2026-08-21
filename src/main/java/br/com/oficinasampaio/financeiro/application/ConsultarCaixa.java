package br.com.oficinasampaio.financeiro.application;

import br.com.oficinasampaio.financeiro.domain.MovimentacaoFinanceira;
import br.com.oficinasampaio.financeiro.domain.MovimentacaoFinanceiraRepository;
import br.com.oficinasampaio.financeiro.domain.PosicaoDeCaixa;
import br.com.oficinasampaio.shared.domain.Periodo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConsultarCaixa {

    private final MovimentacaoFinanceiraRepository movimentacaoRepository;

    public ConsultarCaixa(MovimentacaoFinanceiraRepository movimentacaoRepository) {
        this.movimentacaoRepository = movimentacaoRepository;
    }

    /** O caixa desde sempre, que é o que a tela do financeiro mostra. */
    @Transactional(readOnly = true)
    public CaixaView executar() {
        return montar(movimentacaoRepository.posicao(), movimentacaoRepository.listar());
    }

    /** O caixa de uma janela de tempo, que é o que o relatório fecha. */
    @Transactional(readOnly = true)
    public CaixaView executar(Periodo periodo) {
        return montar(
                movimentacaoRepository.posicao(periodo),
                movimentacaoRepository.listar(periodo)
        );
    }

    private static CaixaView montar(
            PosicaoDeCaixa posicao,
            List<MovimentacaoFinanceira> movimentacoes
    ) {
        return new CaixaView(
                posicao.entradas(),
                posicao.saidas(),
                posicao.saldo(),
                movimentacoes.stream().map(MovimentacaoView::de).toList()
        );
    }
}