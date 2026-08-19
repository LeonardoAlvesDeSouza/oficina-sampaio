package br.com.oficinasampaio.financeiro.presentation;

import br.com.oficinasampaio.cliente.application.BuscarCliente;
import br.com.oficinasampaio.financeiro.application.ListarPagamentos;
import br.com.oficinasampaio.ordemservico.application.ListarOrdensAReceber;
import br.com.oficinasampaio.veiculo.application.VeiculoQueries;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * O balcão do caixa: o que falta receber e o que já foi recebido.
 * <p>
 * O registro do pagamento em si não fica aqui — ele acontece na tela da ordem,
 * porque é o agregado da ordem que fecha a conta. Esta tela leva até lá.
 */
@Controller
@RequestMapping("/pagamentos")
public class PagamentoController {

    private final ListarOrdensAReceber listarOrdensAReceber;
    private final ListarPagamentos listarPagamentos;
    private final VeiculoQueries veiculoQueries;
    private final BuscarCliente buscarCliente;

    public PagamentoController(
            ListarOrdensAReceber listarOrdensAReceber,
            ListarPagamentos listarPagamentos,
            VeiculoQueries veiculoQueries,
            BuscarCliente buscarCliente
    ) {
        this.listarOrdensAReceber = listarOrdensAReceber;
        this.listarPagamentos = listarPagamentos;
        this.veiculoQueries = veiculoQueries;
        this.buscarCliente = buscarCliente;
    }

    @GetMapping
    public String listar(Model model) {
        var aReceber = listarOrdensAReceber.executar().stream()
                .map(ordem -> AReceberLinha.de(
                        ordem,
                        veiculoQueries.obterPorId(ordem.veiculoId()),
                        buscarCliente.executar(ordem.clienteId())
                ))
                .toList();

        var recebidos = listarPagamentos.executar().stream()
                .map(pagamento -> PagamentoLinha.de(
                        pagamento,
                        buscarCliente.executar(pagamento.clienteId())
                ))
                .toList();

        model.addAttribute("painel", PainelPagamentos.montar(aReceber, recebidos));
        return "pagamentos/lista";
    }
}
