package br.com.oficinasampaio.usuario.presentation;

import br.com.oficinasampaio.shared.domain.RegraNegocioException;
import br.com.oficinasampaio.usuario.application.CadastrarUsuario;
import br.com.oficinasampaio.usuario.application.CadastrarUsuarioCommand;
import br.com.oficinasampaio.usuario.application.ListarUsuarios;
import br.com.oficinasampaio.usuario.application.PerfilUsuarioView;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final CadastrarUsuario cadastrarUsuario;
    private final ListarUsuarios listarUsuarios;

    public UsuarioController(CadastrarUsuario cadastrarUsuario, ListarUsuarios listarUsuarios) {
        this.cadastrarUsuario = cadastrarUsuario;
        this.listarUsuarios = listarUsuarios;
    }

    @GetMapping
    public String listar(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new UsuarioForm());
        }
        adicionarDadosDaPagina(model);
        return "usuarios/lista";
    }

    @PostMapping
    public String cadastrar(
            @Valid @ModelAttribute("form") UsuarioForm form,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            adicionarDadosDaPagina(model);
            return "usuarios/lista";
        }

        try {
            cadastrarUsuario.executar(new CadastrarUsuarioCommand(
                    form.getNome(), form.getLogin(), form.getSenha(), form.getPerfil()
            ));
        } catch (RegraNegocioException exception) {
            bindingResult.rejectValue("login", "login.duplicado", exception.getMessage());
            form.setSenha(null);
            adicionarDadosDaPagina(model);
            return "usuarios/lista";
        }

        return "redirect:/usuarios";
    }

    private void adicionarDadosDaPagina(Model model) {
        model.addAttribute("usuarios", listarUsuarios.executar());
        model.addAttribute("perfis", PerfilUsuarioView.values());
    }
}
