package br.com.oficinasampaio.shared.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ModulosEmConstrucaoController {

    @GetMapping("/pagamentos")
    public String pagamentos(Model model) {
        return exibir(
                model,
                "Pagamentos",
                "Registro e acompanhamento dos pagamentos das ordens de serviço."
        );
    }

    @GetMapping("/financeiro")
    public String financeiro(Model model) {
        return exibir(
                model,
                "Financeiro",
                "Entradas, saídas, movimentações e posição financeira da oficina."
        );
    }

    @GetMapping("/relatorios")
    public String relatorios(Model model) {
        return exibir(
                model,
                "Relatórios",
                "Relatórios gerenciais e documentos gerados com JasperReports."
        );
    }

    private String exibir(Model model, String titulo, String descricao) {
        model.addAttribute("tituloModulo", titulo);
        model.addAttribute("descricaoModulo", descricao);
        return "standby/modulo";
    }
}
