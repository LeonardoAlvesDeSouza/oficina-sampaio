package br.com.oficinasampaio.financeiro.presentation;

import br.com.oficinasampaio.cliente.application.ClienteView;
import br.com.oficinasampaio.ordemservico.application.OrdemServicoView;
import br.com.oficinasampaio.ordemservico.application.StatusOrdemServicoView;
import br.com.oficinasampaio.shared.presentation.FormatoOficina;
import br.com.oficinasampaio.veiculo.application.VeiculoParaOrdem;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Uma ordem com o serviço pronto e a conta em aberto. Traz a placa porque é
 * assim que o balcão acha o carro quando o cliente chega para pagar.
 */
public record AReceberLinha(
        UUID ordemServicoId,
        String numeroOrdem,
        String placa,
        String veiculo,
        String cliente,
        StatusOrdemServicoView status,
        String abertaEm,
        BigDecimal total
) {

    static AReceberLinha de(
            OrdemServicoView ordem,
            VeiculoParaOrdem veiculo,
            ClienteView cliente
    ) {
        return new AReceberLinha(
                ordem.id(),
                FormatoOficina.numeroOrdem(ordem.id()),
                veiculo.placa(),
                veiculo.marca() + " " + veiculo.modelo(),
                cliente.nome(),
                ordem.status(),
                FormatoOficina.dataHora(ordem.abertaEm()),
                ordem.total()
        );
    }
}
