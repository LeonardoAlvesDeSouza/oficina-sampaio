package br.com.oficinasampaio.relatorio.infrastructure.jasper;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Lê o texto de dentro de um PDF, inflando os fluxos de conteúdo.
 * <p>
 * Existe por um motivo específico: o JasperReports não desenha o texto que não
 * cabe na altura declarada do elemento, e não reclama. Um total que desaparece do
 * relatório passaria por um teste que só confere se o arquivo é um PDF válido —
 * foi exatamente o que aconteceu com a placa e com o total da ordem. Conferir o
 * texto impresso é o que transforma esse silêncio em falha de build.
 */
final class TextoDoPdf {

    private static final String INICIO = "stream";
    private static final String FIM = "endstream";

    private TextoDoPdf() {
    }

    static String de(byte[] pdf) {
        var bruto = new String(pdf, StandardCharsets.ISO_8859_1);
        var texto = new StringBuilder();

        var cursor = 0;
        while (true) {
            var inicio = bruto.indexOf(INICIO, cursor);
            if (inicio < 0) {
                break;
            }
            var fim = bruto.indexOf(FIM, inicio);
            if (fim < 0) {
                break;
            }
            var comeco = inicio + INICIO.length();
            while (comeco < fim && (bruto.charAt(comeco) == '\r' || bruto.charAt(comeco) == '\n')) {
                comeco++;
            }
            texto.append(inflar(
                    bruto.substring(comeco, fim).getBytes(StandardCharsets.ISO_8859_1)
            ));
            cursor = fim + FIM.length();
        }

        // O PDF escapa parênteses dentro do texto; para procurar um valor
        // impresso, o que interessa é o caractere.
        return texto.toString().replace("\\(", "(").replace("\\)", ")");
    }

    private static String inflar(byte[] comprimido) {
        var inflater = new Inflater();
        inflater.setInput(comprimido);
        var saida = new ByteArrayOutputStream();
        var pedaco = new byte[4096];
        try {
            while (!inflater.finished()) {
                var lidos = inflater.inflate(pedaco);
                if (lidos == 0) {
                    break;
                }
                saida.write(pedaco, 0, lidos);
            }
        } catch (DataFormatException erro) {
            // Fluxo que não é texto comprimido — fonte ou imagem. Não interessa.
            return "";
        } finally {
            inflater.end();
        }
        return saida.toString(StandardCharsets.ISO_8859_1);
    }
}
