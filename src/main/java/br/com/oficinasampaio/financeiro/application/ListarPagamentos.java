package br.com.oficinasampaio.financeiro.application;

import br.com.oficinasampaio.financeiro.domain.PagamentoRepository;
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
        return pagamentoRepository.listar().stream().map(PagamentoView::de).toList();
    }
}