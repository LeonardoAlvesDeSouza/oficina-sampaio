package br.com.oficinasampaio.financeiro.application;

import br.com.oficinasampaio.financeiro.domain.MovimentacaoFinanceiraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultarCaixa {

    private final MovimentacaoFinanceiraRepository movimentacaoRepository;

    public ConsultarCaixa(MovimentacaoFinanceiraRepository movimentacaoRepository) {
        this.movimentacaoRepository = movimentacaoRepository;
    }

    @Transactional(readOnly = true)
    public CaixaView executar() {
        var posicao = movimentacaoRepository.posicao();
        return new CaixaView(
                posicao.entradas(),
                posicao.saidas(),
                posicao.saldo(),
                movimentacaoRepository.listar().stream().map(MovimentacaoView::de).toList()
        );
    }
}