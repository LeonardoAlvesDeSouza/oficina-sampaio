package br.com.oficinasampaio.veiculo.presentation;

import br.com.oficinasampaio.cliente.application.BuscarCliente;
import br.com.oficinasampaio.shared.domain.RegraNegocioException;
import br.com.oficinasampaio.veiculo.application.CadastrarVeiculo;
import br.com.oficinasampaio.veiculo.application.CadastrarVeiculoCommand;
import br.com.oficinasampaio.veiculo.application.ListarVeiculosDoCliente;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
@RequestMapping("/clientes/{clienteId}/veiculos")
public class VeiculoController {

    private final BuscarCliente buscarCliente;
    private final CadastrarVeiculo cadastrarVeiculo;
    private final ListarVeiculosDoCliente listarVeiculosDoCliente;

    public VeiculoController(
            BuscarCliente buscarCliente,
            CadastrarVeiculo cadastrarVeiculo,
            ListarVeiculosDoCliente listarVeiculosDoCliente
    ) {
        this.buscarCliente = buscarCliente;
        this.cadastrarVeiculo = cadastrarVeiculo;
        this.listarVeiculosDoCliente = listarVeiculosDoCliente;
    }

    @GetMapping
    public String listar(@PathVariable UUID clienteId, Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new VeiculoForm());
        }
        adicionarDadosDaPagina(clienteId, model);
        return "veiculos/lista";
    }

    @PostMapping
    public String cadastrar(
            @PathVariable UUID clienteId,
            @Valid @ModelAttribute("form") VeiculoForm form,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            adicionarDadosDaPagina(clienteId, model);
            return "veiculos/lista";
        }

        try {
            cadastrarVeiculo.executar(new CadastrarVeiculoCommand(
                    clienteId,
                    form.getPlaca(),
                    form.getMarca(),
                    form.getModelo(),
                    form.getAno(),
                    form.getCor(),
                    form.getQuilometragem()
            ));
        } catch (RegraNegocioException exception) {
            bindingResult.rejectValue("placa", "placa.invalida", exception.getMessage());
            adicionarDadosDaPagina(clienteId, model);
            return "veiculos/lista";
        }

        return "redirect:/clientes/" + clienteId + "/veiculos";
    }

    private void adicionarDadosDaPagina(UUID clienteId, Model model) {
        model.addAttribute("cliente", buscarCliente.executar(clienteId));
        model.addAttribute("veiculos", listarVeiculosDoCliente.executar(clienteId));
    }
}
