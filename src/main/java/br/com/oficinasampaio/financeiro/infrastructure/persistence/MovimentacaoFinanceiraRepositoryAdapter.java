package br.com.oficinasampaio.financeiro.infrastructure.persistence;

import br.com.oficinasampaio.financeiro.domain.MovimentacaoFinanceira;
import br.com.oficinasampaio.financeiro.domain.MovimentacaoFinanceiraRepository;
import br.com.oficinasampaio.financeiro.domain.PosicaoDeCaixa;
import br.com.oficinasampaio.financeiro.domain.TipoMovimentacao;
import br.com.oficinasampaio.shared.domain.Periodo;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Repository
class MovimentacaoFinanceiraRepositoryAdapter implements MovimentacaoFinanceiraRepository {

    private final SpringDataMovimentacaoFinanceiraRepository repository;

    MovimentacaoFinanceiraRepositoryAdapter(SpringDataMovimentacaoFinanceiraRepository repository) {
        this.repository = repository;
    }

    @Override
    public MovimentacaoFinanceira salvar(MovimentacaoFinanceira movimentacao) {
        return repository.save(movimentacao);
    }

    @Override
    public List<MovimentacaoFinanceira> listar() {
        return repository.findAllByOrderByOcorridaEmDesc();
    }

    @Override
    public List<MovimentacaoFinanceira> listar(Periodo periodo) {
        return repository.findByOcorridaEmBetweenOrderByOcorridaEmDesc(
                periodo.inicio(), periodo.fim()
        );
    }

    @Override
    public PosicaoDeCaixa posicao() {
        return new PosicaoDeCaixa(
                somar(TipoMovimentacao.ENTRADA),
                somar(TipoMovimentacao.SAIDA)
        );
    }

    @Override
    public PosicaoDeCaixa posicao(Periodo periodo) {
        return new PosicaoDeCaixa(
                somar(TipoMovimentacao.ENTRADA, periodo),
                somar(TipoMovimentacao.SAIDA, periodo)
        );
    }

    /**
     * A soma volta do banco com a escala que ele quiser; a posição do caixa é
     * dinheiro e sai daqui sempre com dois dígitos.
     */
    private BigDecimal somar(TipoMovimentacao tipo) {
        return monetario(repository.somarPorTipo(tipo));
    }

    private BigDecimal somar(TipoMovimentacao tipo, Periodo periodo) {
        return monetario(repository.somarPorTipoNoPeriodo(tipo, periodo.inicio(), periodo.fim()));
    }

    private static BigDecimal monetario(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
