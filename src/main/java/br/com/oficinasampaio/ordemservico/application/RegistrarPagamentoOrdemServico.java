package br.com.oficinasampaio.ordemservico.application;

import br.com.oficinasampaio.ordemservico.domain.OrdemServicoRepository;
import br.com.oficinasampaio.shared.domain.RecursoNaoEncontradoException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

/**
 * Registra o pagamento da ordem e publica o evento na mesma transação.
 * <p>
 * O ouvinte do financeiro roda dentro desta transação: se o caixa recusar o
 * lançamento — pagamento já registrado, por exemplo — a ordem também não fica
 * paga. Pagamento, estado da ordem e entrada de caixa entram ou não entram
 * juntos, e é por isso que a publicação não é assíncrona.
 */
@Service
public class RegistrarPagamentoOrdemServico {

    private final OrdemServicoRepository ordemServicoRepository;
    private final ApplicationEventPublisher eventos;
    private final Clock clock;

    public RegistrarPagamentoOrdemServico(
            OrdemServicoRepository ordemServicoRepository,
            ApplicationEventPublisher eventos,
            Clock clock
    ) {
        this.ordemServicoRepository = ordemServicoRepository;
        this.eventos = eventos;
        this.clock = clock;
    }

    @Transactional
    public OrdemServicoDetalheView executar(RegistrarPagamentoOrdemServicoCommand command) {
        var ordem = ordemServicoRepository.buscarPorId(command.ordemServicoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de serviço não encontrada"));

        var evento = ordem.registrarPagamento(command.forma(), command.valor(), clock.instant());
        var salva = ordemServicoRepository.salvar(ordem);
        eventos.publishEvent(evento);

        return OrdemServicoDetalheView.de(salva);
    }
}