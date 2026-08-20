package br.com.oficinasampaio.relatorio.presentation;

import br.com.oficinasampaio.relatorio.application.ConsultarDocumentoDaOrdem;
import br.com.oficinasampaio.relatorio.application.DocumentoDaOrdemView;
import br.com.oficinasampaio.relatorio.application.MotorDeRelatorio;
import br.com.oficinasampaio.relatorio.application.TemplateRelatorio;
import br.com.oficinasampaio.shared.presentation.FormatoOficina;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A via impressa da ordem de serviço — o papel que fica com o cliente e o que vai
 * para a pasta da oficina.
 * <p>
 * Documento não é relatório gerencial: quem atende no balcão imprime a OS, então
 * esta rota é aberta a qualquer usuário autenticado, enquanto {@code /relatorios}
 * fica com o administrador.
 * <p>
 * Escolher o template aqui é o mesmo gesto de devolver o nome de uma página HTML:
 * a apresentação decide como o dado aparece, e o motor só renderiza.
 */
@Controller
@RequestMapping("/documentos")
public class DocumentoController {

    private static final String SEM_DADO = "—";

    private final ConsultarDocumentoDaOrdem consultarDocumentoDaOrdem;
    private final MotorDeRelatorio motor;

    public DocumentoController(
            ConsultarDocumentoDaOrdem consultarDocumentoDaOrdem,
            MotorDeRelatorio motor
    ) {
        this.consultarDocumentoDaOrdem = consultarDocumentoDaOrdem;
        this.motor = motor;
    }

    @GetMapping("/ordens-servico/{ordemServicoId}")
    public ResponseEntity<byte[]> ordemServico(@PathVariable UUID ordemServicoId) {
        var documento = consultarDocumentoDaOrdem.executar(ordemServicoId);
        var numero = FormatoOficina.numeroOrdem(documento.ordem().id());

        return RespostaPdf.de(
                "os-" + numero + ".pdf",
                motor.emitirPdf(
                        TemplateRelatorio.ORDEM_SERVICO,
                        parametros(documento, numero),
                        ItemDoDocumento.de(documento.ordem().itens())
                )
        );
    }

    private static Map<String, Object> parametros(DocumentoDaOrdemView documento, String numero) {
        var ordem = documento.ordem();
        var cliente = documento.cliente();
        var veiculo = documento.veiculo();

        var parametros = new LinkedHashMap<String, Object>();
        parametros.put("numeroOrdem", numero);
        parametros.put("abertaEm", FormatoOficina.dataHora(ordem.abertaEm()));
        parametros.put("status", ordem.status().getRotulo());
        parametros.put("cliente", cliente.nome());
        parametros.put("documentoCliente", ou(FormatoOficina.documento(cliente.cpfCnpj())));
        parametros.put("telefoneCliente", ou(FormatoOficina.telefone(cliente.telefone())));
        parametros.put("placa", veiculo.placa());
        parametros.put("veiculo", veiculo.marca() + " " + veiculo.modelo());
        parametros.put("relatoProblema", ordem.relatoProblema());
        parametros.put("totalServicos", ordem.totalServicos());
        parametros.put("totalPecas", ordem.totalPecas());
        parametros.put("total", ordem.total());
        parametros.put("contaSituacao", ordem.statusPagamento().getRotulo());
        parametros.put("contaDetalhe", detalheDaConta(documento));
        return parametros;
    }

    /**
     * Ordem paga imprime como e quando; conta em aberto imprime o que falta, que é
     * a informação útil para quem vai receber.
     */
    private static String detalheDaConta(DocumentoDaOrdemView documento) {
        if (!documento.paga()) {
            return "Valor a receber: " + FormatoOficina.dinheiro(documento.ordem().total());
        }
        var pagamento = documento.pagamento();
        return pagamento.forma().getRotulo()
                + " em " + FormatoOficina.dataHora(pagamento.registradoEm());
    }

    private static String ou(String valor) {
        return valor == null ? SEM_DADO : valor;
    }
}
