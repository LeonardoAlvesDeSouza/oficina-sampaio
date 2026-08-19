package br.com.oficinasampaio.financeiro.application;

import br.com.oficinasampaio.financeiro.domain.PagamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class PagamentoQueryService implements PagamentoQueries {

    private final PagamentoRepository pagamentoRepository;

    public PagamentoQueryService(PagamentoRepository pagamentoRepository) {
        this.pagamentoRepository = pagamentoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PagamentoDaOrdem> buscarPorOrdemServico(UUID ordemServicoId) {
        return pagamentoRepository.buscarPorOrdemServico(ordemServicoId)
                .map(pagamento -> new PagamentoDaOrdem(
                        pagamento.getId(),
                        FormaPagamentoView.de(pagamento.getForma()),
                        pagamento.getValor(),
                        pagamento.getRegistradoEm()
                ));
    }
}