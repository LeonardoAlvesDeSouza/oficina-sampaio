package br.com.oficinasampaio.ordemservico.application;

import br.com.oficinasampaio.ordemservico.domain.OrdemServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Ordens com serviço entregue ou finalizado e conta em aberto — o dinheiro que a
 * oficina ainda tem para receber. Consulta pública: é o que a tela de pagamentos
 * mostra ao lado do que já entrou no caixa.
 */
@Service
public class ListarOrdensAReceber {

    private final OrdemServicoRepository ordemServicoRepository;

    public ListarOrdensAReceber(OrdemServicoRepository ordemServicoRepository) {
        this.ordemServicoRepository = ordemServicoRepository;
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoView> executar() {
        return ordemServicoRepository.listarAReceber().stream()
                .map(OrdemServicoView::de)
                .toList();
    }
}