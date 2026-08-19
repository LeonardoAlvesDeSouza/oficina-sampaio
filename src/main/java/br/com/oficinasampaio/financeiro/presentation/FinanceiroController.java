package br.com.oficinasampaio.financeiro.presentation;

import br.com.oficinasampaio.financeiro.application.ConsultarCaixa;
import br.com.oficinasampaio.financeiro.application.RegistrarSaida;
import br.com.oficinasampaio.financeiro.application.RegistrarSaidaCommand;
import br.com.oficinasampaio.shared.domain.RegraNegocioException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * O caixa da oficina: posição, extrato e lançamento de saída. O saldo mostrado
 * vem sempre do cálculo entradas menos saídas — não há número guardado.
 */
@Controller
@RequestMapping("/financeiro")
public class FinanceiroController {

    private final ConsultarCaixa consultarCaixa;
    private final RegistrarSaida registrarSaida;

    public FinanceiroController(ConsultarCaixa consultarCaixa, RegistrarSaida registrarSaida) {
        this.consultarCaixa = consultarCaixa;
        this.registrarSaida = registrarSaida;
    }

    @GetMapping
    public String caixa(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new RegistrarSaidaForm());
        }
        adicionarCaixa(model);
        return "financeiro/caixa";
    }

    @PostMapping("/saidas")
    public String registrarSaida(
            @Valid @ModelAttribute("form") RegistrarSaidaForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            adicionarCaixa(model);
            return "financeiro/caixa";
        }

        try {
            registrarSaida.executar(new RegistrarSaidaCommand(form.getDescricao(), form.getValor()));
            redirectAttributes.addFlashAttribute("sucesso", "Saída lançada no caixa");
        } catch (RegraNegocioException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
        }
        return "redirect:/financeiro";
    }

    private void adicionarCaixa(Model model) {
        var caixa = consultarCaixa.executar();
        model.addAttribute("caixa", caixa);
        model.addAttribute("movimentacoes", MovimentacaoLinha.de(caixa.movimentacoes()));
    }
}
