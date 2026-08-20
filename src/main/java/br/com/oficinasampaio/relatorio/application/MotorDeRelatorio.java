package br.com.oficinasampaio.relatorio.application;

import java.util.List;
import java.util.Map;

/**
 * A porta do motor de relatórios. Quem pede um documento entrega dados prontos e
 * escolhe o template, do mesmo jeito que um controller escolhe uma página: nada
 * aqui menciona JasperReports, e trocar o motor não mexe em quem imprime.
 * <p>
 * As linhas chegam como {@code record} porque é o que a aplicação já produz. Cabe
 * ao motor traduzir isso para o formato que a ferramenta entende.
 */
@FunctionalInterface
public interface MotorDeRelatorio {

    byte[] emitirPdf(
            TemplateRelatorio template,
            Map<String, Object> parametros,
            List<? extends Record> linhas
    );
}
