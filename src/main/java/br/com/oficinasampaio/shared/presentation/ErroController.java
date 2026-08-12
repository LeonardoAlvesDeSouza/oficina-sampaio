package br.com.oficinasampaio.shared.presentation;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Assume o /error do servidor. Sem isto, um endereço digitado errado ou uma
 * falha inesperada cai na página branca padrão, que não parece ser deste
 * sistema e não diz ao usuário o que fazer em seguida.
 * <p>
 * O código de status já vem definido na resposta pelo despacho de erro do
 * contêiner; aqui só se escolhe o que a tela conta.
 */
@Controller
public class ErroController implements ErrorController {

    @RequestMapping("/error")
    public String aviso(HttpServletRequest requisicao, Model model) {
        model.addAttribute("aviso", AvisoDeErro.de(codigo(requisicao), null));
        return "erro/aviso";
    }

    private static int codigo(HttpServletRequest requisicao) {
        var atributo = requisicao.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        return atributo instanceof Integer codigo ? codigo : AvisoDeErro.ERRO_INTERNO;
    }
}