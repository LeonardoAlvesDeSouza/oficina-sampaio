package br.com.oficinasampaio.financeiro.domain;

import br.com.oficinasampaio.shared.domain.Periodo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PagamentoRepository {

    Pagamento salvar(Pagamento pagamento);

    boolean existePorOrdemServico(UUID ordemServicoId);

    Optional<Pagamento> buscarPorOrdemServico(UUID ordemServicoId);

    /** Do mais recente para o mais antigo: o caixa do dia é o que se confere. */
    List<Pagamento> listar();

    List<Pagamento> listar(Periodo periodo);
}