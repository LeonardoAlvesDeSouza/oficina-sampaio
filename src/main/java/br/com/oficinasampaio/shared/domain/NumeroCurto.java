package br.com.oficinasampaio.shared.domain;

import java.util.UUID;

/**
 * Identificador curto de um registro. É como a oficina cita uma ordem no balcão
 * e no telefone, e é o que vai escrito na descrição da movimentação do caixa —
 * ninguém lê um UUID inteiro em voz alta.
 * <p>
 * Vive no domínio compartilhado porque o mesmo número aparece na tela e no dado
 * gravado: se o tamanho mudar, os dois precisam mudar juntos.
 */
public final class NumeroCurto {

    private static final int TAMANHO = 8;

    private NumeroCurto() {
    }

    public static String de(UUID id) {
        return id.toString().substring(0, TAMANHO);
    }
}