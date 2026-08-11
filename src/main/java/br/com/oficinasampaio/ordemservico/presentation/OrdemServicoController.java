package br.com.oficinasampaio.ordemservico.presentation;

import br.com.oficinasampaio.cliente.application.BuscarCliente;
import br.com.oficinasampaio.ordemservico.application.AbrirOrdemServico;
import br.com.oficinasampaio.ordemservico.application.AbrirOrdemServicoCommand;
import br.com.oficinasampaio.ordemservico.application.AdicionarItemOrdemServico;
import br.com.oficinasampaio.ordemservico.application.AdicionarItemOrdemServicoCommand;
import br.com.oficinasampaio.ordemservico.application.BuscarOrdemServico;
import br.com.oficinasampaio.ordemservico.application.ListarOrdensServico;
import br.com.oficinasampaio.veiculo.application.VeiculoQueries;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
@RequestMapping("/ordens-servico")
public class OrdemServicoController {

    private final AbrirOrdemServico abrirOrdemServico;
    private final AdicionarItemOrdemServico adicionarItemOrdemServico;
    private final BuscarOrdemServico buscarOrdemServico;
    private final ListarOrdensServico listarOrdensServico;
    private final VeiculoQueries veiculoQueries;
    private final BuscarCliente buscarCliente;

    public OrdemServicoController(
            AbrirOrdemServico abrirOrdemServico,
            AdicionarItemOrdemServico adicionarItemOrdemServico,
            BuscarOrdemServico buscarOrdemServico,
            ListarOrdensServico listarOrdensServico,
            VeiculoQueries veiculoQueries,
            BuscarCliente buscarCliente
    ) {
        this.abrirOrdemServico = abrirOrdemServico;
        this.adicionarItemOrdemServico = adicionarItemOrdemServico;
        this.buscarOrdemServico = buscarOrdemServico;
        this.listarOrdensServico = listarOrdensServico;
        this.veiculoQueries = veiculoQueries;
        this.buscarCliente = buscarCliente;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("ordens", listarOrdensServico.executar());
        return "ordensservico/lista";
    }

    @GetMapping("/nova")
    public String nova(@RequestParam UUID veiculoId, Model model) {
        var form = new AbrirOrdemServicoForm();
        form.setVeiculoId(veiculoId);
        model.addAttribute("form", form);
        adicionarDadosDaAbertura(veiculoId, model);
        return "ordensservico/nova";
    }

    @PostMapping
    public String abrir(
            @Valid @ModelAttribute("form") AbrirOrdemServicoForm form,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            if (form.getVeiculoId() != null) {
                adicionarDadosDaAbertura(form.getVeiculoId(), model);
            }
            return "ordensservico/nova";
        }

        var ordem = abrirOrdemServico.executar(new AbrirOrdemServicoCommand(
                form.getVeiculoId(), form.getRelatoProblema()
        ));
        return "redirect:/ordens-servico/" + ordem.id();
    }

    @GetMapping("/{ordemServicoId}")
    public String detalhe(@PathVariable UUID ordemServicoId, Model model) {
        if (!model.containsAttribute("itemForm")) {
            model.addAttribute("itemForm", new AdicionarItemOrdemServicoForm());
        }
        adicionarDadosDoDetalhe(ordemServicoId, model);
        return "ordensservico/detalhe";
    }

    @PostMapping("/{ordemServicoId}/itens")
    public String adicionarItem(
            @PathVariable UUID ordemServicoId,
            @Valid @ModelAttribute("itemForm") AdicionarItemOrdemServicoForm form,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            adicionarDadosDoDetalhe(ordemServicoId, model);
            return "ordensservico/detalhe";
        }

        adicionarItemOrdemServico.executar(new AdicionarItemOrdemServicoCommand(
                ordemServicoId,
                form.getTipo(),
                form.getDescricao(),
                form.getQuantidade(),
                form.getValorUnitario()
        ));
        return "redirect:/ordens-servico/" + ordemServicoId;
    }

    private void adicionarDadosDaAbertura(UUID veiculoId, Model model) {
        var veiculo = veiculoQueries.obterAtivo(veiculoId);
        model.addAttribute("veiculo", veiculo);
        model.addAttribute("cliente", buscarCliente.executar(veiculo.clienteId()));
    }

    private void adicionarDadosDoDetalhe(UUID ordemServicoId, Model model) {
        var ordem = buscarOrdemServico.executar(ordemServicoId);
        var veiculo = veiculoQueries.obterPorId(ordem.veiculoId());
        model.addAttribute("ordem", ordem);
        model.addAttribute("veiculo", veiculo);
        model.addAttribute("cliente", buscarCliente.executar(ordem.clienteId()));
    }
}
