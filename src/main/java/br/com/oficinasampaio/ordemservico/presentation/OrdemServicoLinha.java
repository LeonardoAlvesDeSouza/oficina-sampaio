package br.com.oficinasampaio.ordemservico.presentation;

import br.com.oficinasampaio.cliente.application.ClienteView;
import br.com.oficinasampaio.ordemservico.application.OrdemServicoView;
import br.com.oficinasampaio.ordemservico.application.StatusOrdemServicoView;
import br.com.oficinasampaio.shared.presentation.FormatoOficina;
import br.com.oficinasampaio.veiculo.application.VeiculoParaOrdem;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Uma linha do quadro de ordens. Traz a placa junto porque na oficina o carro
 * é identificado pela placa, não pelo nome do cliente nem pelo número da ordem.
 */
public record OrdemServicoLinha(
        UUID id,
        String numero,
        String relatoProblema,
        StatusOrdemServicoView status,
        String placa,
        String veiculo,
        String cliente,
        String abertaEm,
        BigDecimal total
) {

    static OrdemServicoLinha de(
            OrdemServicoView ordem,
            VeiculoParaOrdem veiculo,
            ClienteView cliente
    ) {
        return new OrdemServicoLinha(
                ordem.id(),
                FormatoOficina.numeroOrdem(ordem.id()),
                ordem.relatoProblema(),
                ordem.status(),
                veiculo.placa(),
                veiculo.marca() + " " + veiculo.modelo(),
                cliente.nome(),
                FormatoOficina.dataHora(ordem.abertaEm()),
                ordem.total()
        );
    }
}
