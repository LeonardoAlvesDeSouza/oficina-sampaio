package br.com.oficinasampaio.relatorio.presentation;

import br.com.oficinasampaio.financeiro.application.ConsultarCaixa;
import br.com.oficinasampaio.relatorio.application.ConsultarFaturamento;
import br.com.oficinasampaio.relatorio.application.ConsultarPainelGerencial;
import br.com.oficinasampaio.relatorio.application.MotorDeRelatorio;
import br.com.oficinasampaio.relatorio.application.TemplateRelatorio;
import br.com.oficinasampaio.shared.presentation.FormatoOficina;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashMap;

/**
 * Os relatórios gerenciais da oficina: o painel na tela e dois fechamentos em PDF.
 * <p>
 * Toda a área é do administrador, porque aqui aparecem faturamento e caixa — o
 * balcão vê o que precisa nas telas de Pagamentos e da própria ordem.
 */
@Controller
@RequestMapping("/relatorios")
public class RelatorioController {

    private final ConsultarPainelGerencial consultarPainelGerencial;
    private final ConsultarFaturamento consultarFaturamento;
    private final ConsultarCaixa consultarCaixa;
    private final MotorDeRelatorio motor;
    private final Clock clock;

    public RelatorioController(
            ConsultarPainelGerencial consultarPainelGerencial,
            ConsultarFaturamento consultarFaturamento,
            ConsultarCaixa consultarCaixa,
            MotorDeRelatorio motor,
            Clock clock
    ) {
        this.consultarPainelGerencial = consultarPainelGerencial;
        this.consultarFaturamento = consultarFaturamento;
        this.consultarCaixa = consultarCaixa;
        this.motor = motor;
        this.clock = clock;
    }

    @GetMapping
    public String painel(Model model) {
        model.addAttribute("painel", consultarPainelGerencial.executar());
        model.addAttribute("form", mesCorrente());
        return "relatorios/painel";
    }

    @GetMapping("/faturamento")
    public ResponseEntity<byte[]> faturamento(@ModelAttribute PeriodoForm form) {
        var faturamento = consultarFaturamento.executar(form.paraDominio());

        var parametros = new LinkedHashMap<String, Object>();
        parametros.put("periodo", form.descricao());
        parametros.put("recebimentos", (long) faturamento.recebimentos().size());
        parametros.put("total", faturamento.total());

        return RespostaPdf.de(
                "faturamento.pdf",
                motor.emitirPdf(
                        TemplateRelatorio.FATURAMENTO,
                        parametros,
                        FaturamentoLinha.de(faturamento.recebimentos())
                )
        );
    }

    @GetMapping("/caixa")
    public ResponseEntity<byte[]> caixa(@ModelAttribute PeriodoForm form) {
        var caixa = consultarCaixa.executar(form.paraDominio());

        var parametros = new LinkedHashMap<String, Object>();
        parametros.put("periodo", form.descricao());
        parametros.put("entradas", caixa.entradas());
        parametros.put("saidas", caixa.saidas());
        parametros.put("saldo", caixa.saldo());

        return RespostaPdf.de(
                "caixa.pdf",
                motor.emitirPdf(
                        TemplateRelatorio.CAIXA,
                        parametros,
                        CaixaLinha.de(caixa.movimentacoes())
                )
        );
    }

    /**
     * A tela abre com o mês corrente preenchido, que é o fechamento pedido em nove
     * de cada dez vezes.
     */
    private PeriodoForm mesCorrente() {
        // O relógio da aplicação é UTC; "hoje" para a oficina é o dia de Brasília,
        // senão de madrugada o mês corrente abriria errado.
        var hoje = LocalDate.now(clock.withZone(FormatoOficina.FUSO_DA_OFICINA));
        var form = new PeriodoForm();
        form.setInicio(hoje.withDayOfMonth(1));
        form.setFim(hoje);
        return form;
    }
}
