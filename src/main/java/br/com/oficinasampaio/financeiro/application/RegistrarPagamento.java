package br.com.oficinasampaio.financeiro.application;

import br.com.oficinasampaio.financeiro.domain.MovimentacaoFinanceira;
import br.com.oficinasampaio.financeiro.domain.MovimentacaoFinanceiraRepository;
import br.com.oficinasampaio.financeiro.domain.Pagamento;
import br.com.oficinasampaio.financeiro.domain.PagamentoRepository;
import br.com.oficinasampaio.shared.domain.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lança o pagamento e a entrada de caixa correspondente.
 * <p>
 * Roda dentro da transação de quem chamou — o registro do pagamento na ordem de
 * serviço. Recusar aqui derruba a operação inteira, que é exatamente o desejado:
 * ordem paga sem entrada no caixa seria dinheiro que ninguém consegue explicar.
 */
@Service
public class RegistrarPagamento {

    static final String PAGAMENTO_JA_REGISTRADO =
            "Pagamento já registrado para esta ordem de serviço";

    private final PagamentoRepository pagamentoRepository;
    private final MovimentacaoFinanceiraRepository movimentacaoRepository;

    public RegistrarPagamento(
            PagamentoRepository pagamentoRepository,
            MovimentacaoFinanceiraRepository movimentacaoRepository
    ) {
        this.pagamentoRepository = pagamentoRepository;
        this.movimentacaoRepository = movimentacaoRepository;
    }

    @Transactional
    public PagamentoView executar(RegistrarPagamentoCommand command) {
        if (pagamentoRepository.existePorOrdemServico(command.ordemServicoId())) {
            throw new RegraNegocioException(PAGAMENTO_JA_REGISTRADO);
        }

        var pagamento = pagamentoRepository.salvar(Pagamento.registrar(
                command.ordemServicoId(),
                command.clienteId(),
                command.forma(),
                command.valor(),
                command.registradoEm()
        ));

        movimentacaoRepository.salvar(MovimentacaoFinanceira.entradaDePagamento(
                pagamento.getId(),
                pagamento.getOrdemServicoId(),
                pagamento.getValor(),
                pagamento.getRegistradoEm()
        ));

        return PagamentoView.de(pagamento);
    }
}