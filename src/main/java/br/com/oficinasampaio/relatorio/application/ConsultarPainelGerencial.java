package br.com.oficinasampaio.relatorio.application;

import br.com.oficinasampaio.financeiro.application.ConsultarCaixa;
import br.com.oficinasampaio.ordemservico.application.ListarOrdensAReceber;
import br.com.oficinasampaio.ordemservico.application.ListarOrdensServico;
import br.com.oficinasampaio.ordemservico.application.OrdemServicoView;
import br.com.oficinasampaio.ordemservico.application.StatusOrdemServicoView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * O painel da tela de relatórios, montado só com consultas públicas dos outros
 * módulos. Nada aqui é somado no banco por conta própria: se o número aparece na
 * tela do módulo dono, é dele que ele vem.
 */
@Service
public class ConsultarPainelGerencial {

    private static final BigDecimal ZERO_MONETARIO = new BigDecimal("0.00");

    private final ListarOrdensServico listarOrdensServico;
    private final ListarOrdensAReceber listarOrdensAReceber;
    private final ConsultarCaixa consultarCaixa;

    public ConsultarPainelGerencial(
            ListarOrdensServico listarOrdensServico,
            ListarOrdensAReceber listarOrdensAReceber,
            ConsultarCaixa consultarCaixa
    ) {
        this.listarOrdensServico = listarOrdensServico;
        this.listarOrdensAReceber = listarOrdensAReceber;
        this.consultarCaixa = consultarCaixa;
    }

    @Transactional(readOnly = true)
    public PainelGerencialView executar() {
        var ordens = listarOrdensServico.executar();
        var aReceber = listarOrdensAReceber.executar();
        var caixa = consultarCaixa.executar();

        return new PainelGerencialView(
                contarPorStatus(ordens),
                ordens.size(),
                aReceber.size(),
                somar(aReceber),
                caixa.entradas(),
                caixa.saidas(),
                caixa.saldo()
        );
    }

    /** Estados sem nenhuma ordem ficam de fora: linha zerada não informa nada. */
    private static List<PainelGerencialView.ContagemPorStatus> contarPorStatus(
            List<OrdemServicoView> ordens
    ) {
        return Arrays.stream(StatusOrdemServicoView.values())
                .map(status -> new PainelGerencialView.ContagemPorStatus(
                        status,
                        ordens.stream().filter(ordem -> ordem.status() == status).count()
                ))
                .filter(contagem -> contagem.quantidade() > 0)
                .toList();
    }

    private static BigDecimal somar(List<OrdemServicoView> ordens) {
        return ordens.stream()
                .map(OrdemServicoView::total)
                .reduce(ZERO_MONETARIO, BigDecimal::add);
    }
}
