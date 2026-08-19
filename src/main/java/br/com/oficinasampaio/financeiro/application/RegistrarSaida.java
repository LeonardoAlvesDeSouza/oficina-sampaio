package br.com.oficinasampaio.financeiro.application;

import br.com.oficinasampaio.financeiro.domain.MovimentacaoFinanceira;
import br.com.oficinasampaio.financeiro.domain.MovimentacaoFinanceiraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

/**
 * Saída de caixa lançada à mão: compra de peça no fornecedor, conta de luz,
 * retirada do dia. Não tem pagamento de ordem atrás dela, e é o contrapeso das
 * entradas no cálculo do saldo.
 */
@Service
public class RegistrarSaida {

    private final MovimentacaoFinanceiraRepository movimentacaoRepository;
    private final Clock clock;

    public RegistrarSaida(MovimentacaoFinanceiraRepository movimentacaoRepository, Clock clock) {
        this.movimentacaoRepository = movimentacaoRepository;
        this.clock = clock;
    }

    @Transactional
    public MovimentacaoView executar(RegistrarSaidaCommand command) {
        return MovimentacaoView.de(movimentacaoRepository.salvar(MovimentacaoFinanceira.saida(
                command.descricao(), command.valor(), clock.instant()
        )));
    }
}