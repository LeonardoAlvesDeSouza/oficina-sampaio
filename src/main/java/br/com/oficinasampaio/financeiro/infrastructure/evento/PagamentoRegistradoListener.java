package br.com.oficinasampaio.financeiro.infrastructure.evento;

import br.com.oficinasampaio.financeiro.application.RegistrarPagamento;
import br.com.oficinasampaio.financeiro.application.RegistrarPagamentoCommand;
import br.com.oficinasampaio.shared.domain.PagamentoRegistrado;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * A porta de entrada do evento da ordem de serviço. É adaptador: traduz o
 * anúncio da ordem para o comando do caixa e não decide nada.
 * <p>
 * Ouvinte síncrono de propósito. Roda na thread e na transação de quem publicou,
 * então o pagamento na ordem, o pagamento no financeiro e a entrada no caixa
 * confirmam juntos — ou nenhum deles confirma.
 */
@Component
class PagamentoRegistradoListener {

    private final RegistrarPagamento registrarPagamento;

    PagamentoRegistradoListener(RegistrarPagamento registrarPagamento) {
        this.registrarPagamento = registrarPagamento;
    }

    @EventListener
    void aoRegistrarPagamento(PagamentoRegistrado evento) {
        registrarPagamento.executar(new RegistrarPagamentoCommand(
                evento.ordemServicoId(),
                evento.clienteId(),
                evento.forma(),
                evento.valor(),
                evento.registradoEm()
        ));
    }
}
