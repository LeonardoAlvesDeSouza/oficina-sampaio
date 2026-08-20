package br.com.oficinasampaio.financeiro.application;

import br.com.oficinasampaio.financeiro.domain.Pagamento;
import br.com.oficinasampaio.financeiro.domain.PagamentoRepository;
import br.com.oficinasampaio.shared.domain.Periodo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListarPagamentos {

    private final PagamentoRepository pagamentoRepository;

    public ListarPagamentos(PagamentoRepository pagamentoRepository) {
        this.pagamentoRepository = pagamentoRepository;
    }

    @Transactional(readOnly = true)
    public List<PagamentoView> executar() {
        return traduzir(pagamentoRepository.listar());
    }

    /** Os recebimentos de uma janela de tempo, para fechar o faturamento. */
    @Transactional(readOnly = true)
    public List<PagamentoView> executar(Periodo periodo) {
        return traduzir(pagamentoRepository.listar(periodo));
    }

    private static List<PagamentoView> traduzir(List<Pagamento> pagamentos) {
        return pagamentos.stream().map(PagamentoView::de).toList();
    }
}