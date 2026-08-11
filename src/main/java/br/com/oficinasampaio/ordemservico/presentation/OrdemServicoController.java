package br.com.oficinasampaio.ordemservico.presentation;

import br.com.oficinasampaio.cliente.application.BuscarCliente;
import br.com.oficinasampaio.ordemservico.application.AbrirOrdemServico;
import br.com.oficinasampaio.ordemservico.application.AbrirOrdemServicoCommand;
import br.com.oficinasampaio.ordemservico.application.AcaoOrdemServicoView;
import br.com.oficinasampaio.ordemservico.application.AdicionarItemOrdemServico;
import br.com.oficinasampaio.ordemservico.application.AdicionarItemOrdemServicoCommand;
import br.com.oficinasampaio.ordemservico.application.AlterarStatusOrdemServico;
import br.com.oficinasampaio.ordemservico.application.AlterarStatusOrdemServicoCommand;
import br.com.oficinasampaio.ordemservico.application.BuscarOrdemServico;
import br.com.oficinasampaio.ordemservico.application.ListarOrdensServico;
import br.com.oficinasampaio.ordemservico.application.OrdemServicoDetalheView;
import br.com.oficinasampaio.shared.domain.RegraNegocioException;
import br.com.oficinasampaio.shared.presentation.PerfilAutenticado;
import br.com.oficinasampaio.shared.presentation.WebExceptionHandler;
import br.com.oficinasampaio.veiculo.application.VeiculoQueries;
import jakarta.validation.Valid;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/ordens-servico")
public class OrdemServicoController {

    private final AbrirOrdemServico abrirOrdemServico;
    private final AdicionarItemOrdemServico adicionarItemOrdemServico;
    private final AlterarStatusOrdemServico alterarStatusOrdemServico;
    private final BuscarOrdemServico buscarOrdemServico;
    private final ListarOrdensServico listarOrdensServico;
    private final VeiculoQueries veiculoQueries;
    private final BuscarCliente buscarCliente;

    public OrdemServicoController(
            AbrirOrdemServico abrirOrdemServico,
            AdicionarItemOrdemServico adicionarItemOrdemServico,
            AlterarStatusOrdemServico alterarStatusOrdemServico,
            BuscarOrdemServico buscarOrdemServico,
            ListarOrdensServico listarOrdensServico,
            VeiculoQueries veiculoQueries,
            BuscarCliente buscarCliente
    ) {
        this.abrirOrdemServico = abrirOrdemServico;
        this.adicionarItemOrdemServico = adicionarItemOrdemServico;
        this.alterarStatusOrdemServico = alterarStatusOrdemServico;
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
    public String detalhe(
            @PathVariable UUID ordemServicoId,
            Authentication authentication,
            Model model
    ) {
        if (!model.containsAttribute("itemForm")) {
            model.addAttribute("itemForm", new AdicionarItemOrdemServicoForm());
        }
        adicionarDadosDoDetalhe(ordemServicoId, authentication, model);
        return "ordensservico/detalhe";
    }

    @PostMapping("/{ordemServicoId}/itens")
    public String adicionarItem(
            @PathVariable UUID ordemServicoId,
            @Valid @ModelAttribute("itemForm") AdicionarItemOrdemServicoForm form,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            adicionarDadosDoDetalhe(ordemServicoId, authentication, model);
            return "ordensservico/detalhe";
        }

        try {
            adicionarItemOrdemServico.executar(new AdicionarItemOrdemServicoCommand(
                    ordemServicoId,
                    form.getTipo(),
                    form.getDescricao(),
                    form.getQuantidade(),
                    form.getValorUnitario()
            ));
        } catch (RegraNegocioException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
        } catch (ObjectOptimisticLockingFailureException exception) {
            redirectAttributes.addFlashAttribute("erro", WebExceptionHandler.MENSAGEM_CONFLITO);
        }
        return "redirect:/ordens-servico/" + ordemServicoId;
    }

    @PostMapping("/{ordemServicoId}/status")
    public String alterarStatus(
            @PathVariable UUID ordemServicoId,
            @RequestParam AcaoOrdemServicoView acao,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (acao.isRestritaAoAdministrador() && !PerfilAutenticado.ehAdministrador(authentication)) {
            throw new AccessDeniedException("Ação restrita ao administrador");
        }

        try {
            alterarStatusOrdemServico.executar(new AlterarStatusOrdemServicoCommand(
                    ordemServicoId, acao
            ));
            redirectAttributes.addFlashAttribute("sucesso", "Status da ordem atualizado");
        } catch (RegraNegocioException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
        } catch (ObjectOptimisticLockingFailureException exception) {
            redirectAttributes.addFlashAttribute("erro", WebExceptionHandler.MENSAGEM_CONFLITO);
        }
        return "redirect:/ordens-servico/" + ordemServicoId;
    }

    private void adicionarDadosDaAbertura(UUID veiculoId, Model model) {
        var veiculo = veiculoQueries.obterAtivo(veiculoId);
        model.addAttribute("veiculo", veiculo);
        model.addAttribute("cliente", buscarCliente.executar(veiculo.clienteId()));
    }

    private void adicionarDadosDoDetalhe(
            UUID ordemServicoId,
            Authentication authentication,
            Model model
    ) {
        var ordem = buscarOrdemServico.executar(ordemServicoId);
        var veiculo = veiculoQueries.obterPorId(ordem.veiculoId());
        model.addAttribute("ordem", ordem);
        model.addAttribute("acoes", acoesPermitidas(ordem, authentication));
        model.addAttribute("veiculo", veiculo);
        model.addAttribute("cliente", buscarCliente.executar(ordem.clienteId()));
    }

    private static List<AcaoOrdemServicoView> acoesPermitidas(
            OrdemServicoDetalheView ordem,
            Authentication authentication
    ) {
        var administrador = PerfilAutenticado.ehAdministrador(authentication);
        return ordem.acoesDisponiveis().stream()
                .filter(acao -> administrador || !acao.isRestritaAoAdministrador())
                .toList();
    }
}
