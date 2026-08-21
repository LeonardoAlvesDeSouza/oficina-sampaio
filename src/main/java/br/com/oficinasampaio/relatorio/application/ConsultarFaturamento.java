package br.com.oficinasampaio.relatorio.application;

import br.com.oficinasampaio.cliente.application.BuscarCliente;
import br.com.oficinasampaio.financeiro.application.ListarPagamentos;
import br.com.oficinasampaio.shared.domain.Periodo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * O que a oficina recebeu numa janela de tempo. O total é somado sobre os mesmos
 * recebimentos que vão listados: o rodapé do relatório nunca discorda do corpo.
 */
@Service
public class ConsultarFaturamento {

    private static final BigDecimal ZERO_MONETARIO = new BigDecimal("0.00");

    private final ListarPagamentos listarPagamentos;
    private final BuscarCliente buscarCliente;

    public ConsultarFaturamento(ListarPagamentos listarPagamentos, BuscarCliente buscarCliente) {
        this.listarPagamentos = listarPagamentos;
        this.buscarCliente = buscarCliente;
    }

    @Transactional(readOnly = true)
    public FaturamentoView executar(Periodo periodo) {
        var recebimentos = listarPagamentos.executar(periodo).stream()
                .map(pagamento -> new RecebimentoDoPeriodo(
                        pagamento.ordemServicoId(),
                        buscarCliente.executar(pagamento.clienteId()).nome(),
                        pagamento.forma(),
                        pagamento.valor(),
                        pagamento.registradoEm()
                ))
                .toList();

        return new FaturamentoView(recebimentos, somar(recebimentos));
    }

    private static BigDecimal somar(List<RecebimentoDoPeriodo> recebimentos) {
        return recebimentos.stream()
                .map(RecebimentoDoPeriodo::valor)
                .reduce(ZERO_MONETARIO, BigDecimal::add);
    }
}
