package br.com.oficinasampaio.ordemservico.application;

import br.com.oficinasampaio.ordemservico.domain.AcaoOrdemServico;
import br.com.oficinasampaio.ordemservico.domain.StatusOrdemServico;
import br.com.oficinasampaio.ordemservico.domain.StatusPagamento;
import br.com.oficinasampaio.ordemservico.domain.TipoItemOrdemServico;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A tradução entre domínio e apresentação é feita por {@code valueOf(name())}. Se
 * um dos lados ganhar ou perder uma constante, a falha só apareceria em runtime,
 * ao renderizar a tela. Estes testes transformam esse descompasso em erro de build.
 */
class ParidadeDeEnumsTest {

    @Test
    void acoesDoDominioEDaApresentacaoTemOsMesmosNomesNaMesmaOrdem() {
        assertEquals(nomes(AcaoOrdemServico.values()), nomes(AcaoOrdemServicoView.values()));
    }

    @Test
    void statusDoDominioEDaApresentacaoTemOsMesmosNomesNaMesmaOrdem() {
        assertEquals(nomes(StatusOrdemServico.values()), nomes(StatusOrdemServicoView.values()));
    }

    @Test
    void statusDePagamentoDoDominioEDaApresentacaoTemOsMesmosNomesNaMesmaOrdem() {
        assertEquals(nomes(StatusPagamento.values()), nomes(StatusPagamentoView.values()));
    }

    @Test
    void tiposDeItemDoDominioEDaApresentacaoTemOsMesmosNomesNaMesmaOrdem() {
        assertEquals(nomes(TipoItemOrdemServico.values()), nomes(TipoItemOrdemServicoView.values()));
    }

    private static List<String> nomes(Enum<?>[] valores) {
        return Arrays.stream(valores).map(Enum::name).toList();
    }
}
