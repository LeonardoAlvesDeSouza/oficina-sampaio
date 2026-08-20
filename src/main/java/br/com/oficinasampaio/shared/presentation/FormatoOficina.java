package br.com.oficinasampaio.shared.presentation;

import br.com.oficinasampaio.shared.domain.NumeroCurto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * Formatação de dados para as telas. O domínio guarda instantes em UTC; o
 * balcão lê horário de Brasília. O cadastro guarda documento e telefone só com
 * dígitos; o balcão lê pontuado, do jeito que está escrito no papel do cliente.
 */
public final class FormatoOficina {

    /**
     * O fuso em que a oficina lê horário. Público porque quem monta um período de
     * relatório precisa do mesmo fuso para transformar dias em instantes — dois
     * fusos diferentes fariam a tela e o relatório discordarem sobre o dia.
     */
    public static final ZoneId FUSO_DA_OFICINA = ZoneId.of("America/Sao_Paulo");

    private static final DateTimeFormatter DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yy 'às' HH:mm");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String SEM_DADO = "—";

    /**
     * Locale fixo em pt-BR, não o da máquina: o mesmo valor tem que sair igual na
     * tela do balcão e no papel, em qualquer computador onde a oficina rodar.
     */
    private static final Locale LOCALE_DA_OFICINA = Locale.of("pt", "BR");

    private static final int DIGITOS_CPF = 11;
    private static final int DIGITOS_CNPJ = 14;
    private static final int DIGITOS_TELEFONE_FIXO = 10;
    private static final int DIGITOS_CELULAR = 11;

    private FormatoOficina() {
    }

    public static String dataHora(Instant instante) {
        if (instante == null) {
            return SEM_DADO;
        }
        return DATA_HORA.format(instante.atZone(FUSO_DA_OFICINA));
    }

    /**
     * Dinheiro em real, com vírgula decimal e ponto de milhar. Para texto montado
     * em Java — nas telas quem formata é o próprio Thymeleaf.
     */
    public static String dinheiro(BigDecimal valor) {
        if (valor == null) {
            return SEM_DADO;
        }
        return String.format(LOCALE_DA_OFICINA, "R$ %,.2f", valor);
    }

    /**
     * Quantidade sem zero à direita: uma peça é "1", não "1,000", e meia hora de
     * serviço é "1,5".
     */
    public static String quantidade(BigDecimal quantidade) {
        if (quantidade == null) {
            return SEM_DADO;
        }
        return quantidade.stripTrailingZeros().toPlainString().replace('.', ',');
    }

    /** Dia sem hora, do jeito que se escreve num documento impresso. */
    public static String data(LocalDate data) {
        if (data == null) {
            return SEM_DADO;
        }
        return DATA.format(data);
    }

    /**
     * Número curto da ordem, para o balcão citar por telefone sem ler um UUID.
     * A regra do número mora no domínio compartilhado: a mesma citação vai
     * gravada na descrição da movimentação do caixa.
     */
    public static String numeroOrdem(UUID id) {
        return NumeroCurto.de(id);
    }

    /**
     * CPF ou CNPJ pontuado. Documento com uma contagem de dígitos que não é nem
     * uma nem outra sai como está: a tela não esconde um dado que o cadastro
     * aceitou, e um valor estranho na lista é justamente o que precisa aparecer.
     *
     * @return nulo quando não há documento, para a tela decidir o que mostrar
     */
    public static String documento(String cpfCnpj) {
        var digitos = somenteDigitos(cpfCnpj);
        if (digitos == null) {
            return null;
        }
        return switch (digitos.length()) {
            case DIGITOS_CPF -> "%s.%s.%s-%s".formatted(
                    digitos.substring(0, 3), digitos.substring(3, 6),
                    digitos.substring(6, 9), digitos.substring(9));
            case DIGITOS_CNPJ -> "%s.%s.%s/%s-%s".formatted(
                    digitos.substring(0, 2), digitos.substring(2, 5),
                    digitos.substring(5, 8), digitos.substring(8, 12),
                    digitos.substring(12));
            default -> cpfCnpj.strip();
        };
    }

    /**
     * Telefone fixo ou celular com DDD. Mesma regra do documento: o que não
     * couber nos dois formatos conhecidos sai como foi cadastrado.
     * <p>
     * A contagem de dígitos sozinha não basta: um 0800 também tem onze dígitos e
     * sairia como "(08) 00123-4567". DDD não começa com zero, e é isso que
     * separa um telefone de discagem gratuita de um celular.
     *
     * @return nulo quando não há telefone
     */
    public static String telefone(String telefone) {
        var digitos = somenteDigitos(telefone);
        if (digitos == null) {
            return null;
        }
        if (digitos.startsWith("0")) {
            return telefone.strip();
        }
        return switch (digitos.length()) {
            case DIGITOS_TELEFONE_FIXO -> "(%s) %s-%s".formatted(
                    digitos.substring(0, 2), digitos.substring(2, 6), digitos.substring(6));
            case DIGITOS_CELULAR -> "(%s) %s-%s".formatted(
                    digitos.substring(0, 2), digitos.substring(2, 7), digitos.substring(7));
            default -> telefone.strip();
        };
    }

    private static String somenteDigitos(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.replaceAll("\\D", "");
    }
}