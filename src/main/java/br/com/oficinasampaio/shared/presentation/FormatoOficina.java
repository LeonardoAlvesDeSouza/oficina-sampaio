package br.com.oficinasampaio.shared.presentation;

import br.com.oficinasampaio.shared.domain.NumeroCurto;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Formatação de dados para as telas. O domínio guarda instantes em UTC; o
 * balcão lê horário de Brasília. O cadastro guarda documento e telefone só com
 * dígitos; o balcão lê pontuado, do jeito que está escrito no papel do cliente.
 */
public final class FormatoOficina {

    private static final ZoneId FUSO_DA_OFICINA = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yy 'às' HH:mm");
    private static final String SEM_DADO = "—";

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