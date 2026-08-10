package br.com.oficinasampaio.cliente.presentation;

import br.com.oficinasampaio.cliente.application.CadastrarCliente;
import br.com.oficinasampaio.cliente.application.CadastrarClienteCommand;
import br.com.oficinasampaio.cliente.application.ListarClientes;
import br.com.oficinasampaio.shared.domain.RegraNegocioException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final CadastrarCliente cadastrarCliente;
    private final ListarClientes listarClientes;

    public ClienteController(CadastrarCliente cadastrarCliente, ListarClientes listarClientes) {
        this.cadastrarCliente = cadastrarCliente;
        this.listarClientes = listarClientes;
    }

    @GetMapping
    public String listar(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new ClienteForm());
        }
        adicionarListagem(model);
        return "clientes/lista";
    }

    @PostMapping
    public String cadastrar(
            @Valid @ModelAttribute("form") ClienteForm form,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            adicionarListagem(model);
            return "clientes/lista";
        }

        try {
            cadastrarCliente.executar(new CadastrarClienteCommand(
                    form.getNome(),
                    form.getCpfCnpj(),
                    form.getTelefone(),
                    form.getEmail()
            ));
        } catch (RegraNegocioException exception) {
            bindingResult.rejectValue("cpfCnpj", "cpfCnpj.duplicado", exception.getMessage());
            adicionarListagem(model);
            return "clientes/lista";
        }

        return "redirect:/clientes";
    }

    private void adicionarListagem(Model model) {
        model.addAttribute("clientes", listarClientes.executar());
    }
}
