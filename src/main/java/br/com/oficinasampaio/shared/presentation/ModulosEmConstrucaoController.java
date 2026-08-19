package br.com.oficinasampaio.shared.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ModulosEmConstrucaoController {

    @GetMapping("/relatorios")
    public String relatorios(Model model) {
        return exibir(
                model,
                "relatorios",
                "Relatórios",
                "Relatórios gerenciais e documentos gerados com JasperReports."
        );
    }

    private String exibir(Model model, String secao, String titulo, String descricao) {
        model.addAttribute("secao", secao);
        model.addAttribute("tituloModulo", titulo);
        model.addAttribute("descricaoModulo", descricao);
        return "standby/modulo";
    }
}
