package br.com.oficinasampaio.relatorio.application;

/**
 * Os documentos que a oficina imprime. O nome do arquivo JRXML fica aqui e não
 * espalhado em strings: template errado é erro de compilação, não de runtime.
 */
public enum TemplateRelatorio {
    ORDEM_SERVICO("ordem-servico"),
    FATURAMENTO("faturamento"),
    CAIXA("caixa");

    private final String arquivo;

    TemplateRelatorio(String arquivo) {
        this.arquivo = arquivo;
    }

    public String getArquivo() {
        return arquivo;
    }
}
