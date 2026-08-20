package br.com.oficinasampaio.relatorio.infrastructure.jasper;

import br.com.oficinasampaio.relatorio.application.MotorDeRelatorio;
import br.com.oficinasampaio.relatorio.application.TemplateRelatorio;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.RecordComponent;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * O motor de relatórios, e o único lugar do sistema que menciona JasperReports.
 * <p>
 * Os JRXML são versionados como texto e compilados pela aplicação. Compilar custa
 * caro, então cada template é compilado uma vez e fica guardado: o primeiro PDF de
 * cada tipo paga o preço, os seguintes não.
 * <p>
 * As linhas chegam como {@code record} e são traduzidas para mapa aqui. É
 * proposital: o JasperReports leria propriedades de JavaBean — {@code getValor()} —
 * e um record expõe {@code valor()}. Em vez de manter classes de linha só para
 * agradar a ferramenta, a acomodação fica na infraestrutura, que é quem conhece a
 * exigência. O nome do componente do record é o nome do campo no JRXML.
 */
@Component
class MotorJasperReports implements MotorDeRelatorio {

    private static final String PASTA = "relatorios/";
    private static final String EXTENSAO = ".jrxml";

    /**
     * Locale fixo, não o da máquina: os padrões numéricos do JRXML são resolvidos
     * pelo locale do relatório, e sem fixá-lo o mesmo total sairia "1.849,50" aqui
     * e "1,849.50" em outro computador.
     */
    private static final Locale LOCALE_DA_OFICINA = Locale.of("pt", "BR");

    private final Map<TemplateRelatorio, JasperReport> compilados = new ConcurrentHashMap<>();

    @Override
    public byte[] emitirPdf(
            TemplateRelatorio template,
            Map<String, Object> parametros,
            List<? extends Record> linhas
    ) {
        var parametrosDoRelatorio = new HashMap<String, Object>(parametros);
        parametrosDoRelatorio.put(JRParameter.REPORT_LOCALE, LOCALE_DA_OFICINA);

        try {
            var impressao = JasperFillManager.fillReport(
                    compilados.computeIfAbsent(template, MotorJasperReports::compilar),
                    parametrosDoRelatorio,
                    new JRMapCollectionDataSource(linhas.stream()
                            .map(MotorJasperReports::comoMapa)
                            .toList())
            );
            return JasperExportManager.exportReportToPdf(impressao);
        } catch (JRException erro) {
            throw new IllegalStateException(
                    "Não foi possível emitir o relatório " + template.getArquivo(), erro
            );
        }
    }

    private static JasperReport compilar(TemplateRelatorio template) {
        var caminho = PASTA + template.getArquivo() + EXTENSAO;
        try (InputStream jrxml = new ClassPathResource(caminho).getInputStream()) {
            return JasperCompileManager.compileReport(jrxml);
        } catch (IOException | JRException erro) {
            throw new IllegalStateException("Não foi possível compilar " + caminho, erro);
        }
    }

    private static Map<String, ?> comoMapa(Record linha) {
        var campos = new LinkedHashMap<String, Object>();
        for (RecordComponent componente : linha.getClass().getRecordComponents()) {
            campos.put(componente.getName(), valorDe(componente, linha));
        }
        return campos;
    }

    private static Object valorDe(RecordComponent componente, Record linha) {
        try {
            return componente.getAccessor().invoke(linha);
        } catch (ReflectiveOperationException erro) {
            throw new IllegalStateException(
                    "Não foi possível ler o campo " + componente.getName()
                            + " de " + linha.getClass().getSimpleName(), erro
            );
        }
    }
}
