package br.com.oficinasampaio.financeiro.infrastructure.persistence;

import br.com.oficinasampaio.financeiro.domain.MovimentacaoFinanceira;
import br.com.oficinasampaio.financeiro.domain.TipoMovimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

interface SpringDataMovimentacaoFinanceiraRepository
        extends JpaRepository<MovimentacaoFinanceira, UUID> {

    List<MovimentacaoFinanceira> findAllByOrderByOcorridaEmDesc();

    List<MovimentacaoFinanceira> findByOcorridaEmBetweenOrderByOcorridaEmDesc(
            Instant inicio,
            Instant fim
    );

    /**
     * O coalesce faz o caixa vazio somar zero em vez de nulo — sem ele a primeira
     * abertura da tela, antes de qualquer lançamento, quebraria no cálculo.
     */
    @Query("""
            select coalesce(sum(movimentacao.valor), 0)
            from MovimentacaoFinanceira movimentacao
            where movimentacao.tipo = :tipo
            """)
    BigDecimal somarPorTipo(@Param("tipo") TipoMovimentacao tipo);

    @Query("""
            select coalesce(sum(movimentacao.valor), 0)
            from MovimentacaoFinanceira movimentacao
            where movimentacao.tipo = :tipo
              and movimentacao.ocorridaEm between :inicio and :fim
            """)
    BigDecimal somarPorTipoNoPeriodo(
            @Param("tipo") TipoMovimentacao tipo,
            @Param("inicio") Instant inicio,
            @Param("fim") Instant fim
    );
}
