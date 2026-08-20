package br.com.oficinasampaio.relatorio.application;

import br.com.oficinasampaio.cliente.application.ClienteView;
import br.com.oficinasampaio.financeiro.application.PagamentoDaOrdem;
import br.com.oficinasampaio.ordemservico.application.OrdemServicoDetalheView;
import br.com.oficinasampaio.veiculo.application.VeiculoParaOrdem;

/**
 * Tudo o que a via impressa da ordem precisa, reunido de quatro módulos numa só
 * leitura. O pagamento é opcional: ordem com a conta em aberto também imprime.
 */
public record DocumentoDaOrdemView(
        OrdemServicoDetalheView ordem,
        ClienteView cliente,
        VeiculoParaOrdem veiculo,
        PagamentoDaOrdem pagamento
) {

    public boolean paga() {
        return pagamento != null;
    }
}
