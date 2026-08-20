package br.com.oficinasampaio.relatorio.infrastructure.jasper;

import br.com.oficinasampaio.relatorio.application.TemplateRelatorio;
import br.com.oficinasampaio.relatorio.presentation.CaixaLinha;
import br.com.oficinasampaio.relatorio.presentation.FaturamentoLinha;
import br.com.oficinasampaio.relatorio.presentation.ItemDoDocumento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Os JRXML são texto versionado e só falhariam ao serem compilados, ou seja, na
 * primeira vez que alguém pedisse o PDF. Este teste compila e emite os três
 * templates de verdade, com as mesmas classes de linha que a aplicação usa: um
 * campo renomeado de um lado e não do outro para o build.
 */
class MotorJasperReportsTest {

    private static final byte[] ASSINATURA_PDF = "%PDF-".getBytes(StandardCharsets.US_ASCII);

    private final MotorJasperReports motor = new MotorJasperReports();

    @Test
    void emiteAViaImpressaDaOrdemDeServico() {
        var pdf = motor.emitirPdf(
                TemplateRelatorio.ORDEM_SERVICO,
                parametrosDaOrdem(),
                List.of(
                        new ItemDoDocumento(
                                "Serviço", "Troca dos amortecedores dianteiros", "1",
                                new BigDecimal("320.00"), new BigDecimal("320.00")
                        ),
                        new ItemDoDocumento(
                                "Peça", "Par de amortecedores", "2",
                                new BigDecimal("289.90"), new BigDecimal("579.80")
                        )
                )
        );

        verificarPdf(pdf);
        var texto = TextoDoPdf.de(pdf);
        assertAll(
                () -> assertImpresso(texto, "OFICINA SAMPAIO"),
                () -> assertImpresso(texto, "OS 1a2b3c4d"),
                // A placa é como a oficina identifica o carro: sem ela o papel
                // não serve. Já saiu faltando uma vez por caber mal na altura.
                () -> assertImpresso(texto, "ABC1D23"),
                () -> assertImpresso(texto, "Maria da Silva"),
                () -> assertImpresso(texto, "(11) 98888-7777"),
                () -> assertImpresso(texto, "Troca dos amortecedores dianteiros"),
                () -> assertImpresso(texto, "289,90"),
                () -> assertImpresso(texto, "R$ 899,80"),
                () -> assertImpresso(texto, "Assinatura do cliente")
        );
    }

    @Test
    void emiteOFaturamentoDoPeriodo() {
        var parametros = new LinkedHashMap<String, Object>();
        parametros.put("periodo", "01/08/2026 a 19/08/2026");
        parametros.put("recebimentos", 2L);
        parametros.put("total", new BigDecimal("1049.80"));

        var pdf = motor.emitirPdf(
                TemplateRelatorio.FATURAMENTO,
                parametros,
                List.of(
                        new FaturamentoLinha(
                                "19/08/26 às 09:31", "1a2b3c4d", "Maria da Silva",
                                "PIX", new BigDecimal("899.80")
                        ),
                        new FaturamentoLinha(
                                "18/08/26 às 15:02", "5e6f7a8b", "José Sampaio",
                                "Cartão de débito", new BigDecimal("150.00")
                        )
                )
        );

        verificarPdf(pdf);
        var texto = TextoDoPdf.de(pdf);
        assertAll(
                () -> assertImpresso(texto, "FATURAMENTO"),
                () -> assertImpresso(texto, "01/08/2026 a 19/08/2026"),
                () -> assertImpresso(texto, "Maria da Silva"),
                () -> assertImpresso(texto, "899,80"),
                () -> assertImpresso(texto, "R$ 1.049,80")
        );
    }

    /**
     * A linha do extrato tem sempre uma coluna nula — entrada ou saída. Emitir com
     * ela prova que o campo ausente vira espaço em branco em vez de quebrar.
     */
    @Test
    void emiteOExtratoDoCaixaComColunaVazia() {
        var pdf = motor.emitirPdf(
                TemplateRelatorio.CAIXA,
                parametrosDoCaixa(),
                List.of(
                        new CaixaLinha(
                                "19/08/26 às 09:31", "Pagamento da OS 1a2b3c4d",
                                new BigDecimal("899.80"), null
                        ),
                        new CaixaLinha(
                                "19/08/26 às 10:04", "Par de amortecedores no fornecedor",
                                null, new BigDecimal("410.00")
                        )
                )
        );

        verificarPdf(pdf);
        var texto = TextoDoPdf.de(pdf);
        assertAll(
                () -> assertImpresso(texto, "Pagamento da OS 1a2b3c4d"),
                () -> assertImpresso(texto, "899,80"),
                () -> assertImpresso(texto, "410,00"),
                () -> assertImpresso(texto, "R$ 489,80")
        );
    }

    /** Período sem movimento nenhum ainda imprime cabeçalho e saldo. */
    @Test
    void emiteOExtratoDoCaixaSemNenhumaMovimentacao() {
        var pdf = motor.emitirPdf(TemplateRelatorio.CAIXA, parametrosDoCaixa(), List.of());

        verificarPdf(pdf);
        var texto = TextoDoPdf.de(pdf);
        assertAll(
                () -> assertImpresso(texto, "CAIXA"),
                () -> assertImpresso(texto, "SALDO"),
                () -> assertImpresso(texto, "R$ 489,80")
        );
    }

    /** O template compilado é reaproveitado: compilar é a parte cara. */
    @Test
    void emiteDuasVezesOMesmoTemplate() {
        var primeiro = motor.emitirPdf(TemplateRelatorio.CAIXA, parametrosDoCaixa(), List.of());
        var segundo = motor.emitirPdf(TemplateRelatorio.CAIXA, parametrosDoCaixa(), List.of());

        verificarPdf(primeiro);
        verificarPdf(segundo);
    }

    private static Map<String, Object> parametrosDaOrdem() {
        var parametros = new LinkedHashMap<String, Object>();
        parametros.put("numeroOrdem", "1a2b3c4d");
        parametros.put("abertaEm", "19/08/26 às 08:31");
        parametros.put("status", "Finalizada");
        parametros.put("cliente", "Maria da Silva");
        parametros.put("documentoCliente", "529.982.247-25");
        parametros.put("telefoneCliente", "(11) 98888-7777");
        parametros.put("placa", "ABC1D23");
        parametros.put("veiculo", "Volkswagen Gol 1.6 MSI");
        parametros.put("relatoProblema", "Barulho na suspensão dianteira ao passar em lombada.");
        parametros.put("totalServicos", new BigDecimal("320.00"));
        parametros.put("totalPecas", new BigDecimal("579.80"));
        parametros.put("total", new BigDecimal("899.80"));
        parametros.put("contaSituacao", "Paga");
        parametros.put("contaDetalhe", "PIX em 19/08/26 às 09:31");
        return parametros;
    }

    private static Map<String, Object> parametrosDoCaixa() {
        var parametros = new LinkedHashMap<String, Object>();
        parametros.put("periodo", "01/08/2026 a 19/08/2026");
        parametros.put("entradas", new BigDecimal("899.80"));
        parametros.put("saidas", new BigDecimal("410.00"));
        parametros.put("saldo", new BigDecimal("489.80"));
        return parametros;
    }

    private static void assertImpresso(String texto, String esperado) {
        assertTrue(texto.contains(esperado), "não foi impresso no PDF: " + esperado);
    }

    private static void verificarPdf(byte[] pdf) {
        assertAll(
                () -> assertTrue(pdf.length > 500, "PDF pequeno demais para ter conteúdo"),
                () -> assertTrue(
                        java.util.Arrays.equals(
                                pdf, 0, ASSINATURA_PDF.length,
                                ASSINATURA_PDF, 0, ASSINATURA_PDF.length
                        ),
                        "conteúdo não começa com a assinatura de um PDF"
                )
        );
    }
}
