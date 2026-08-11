package br.com.oficinasampaio.usuario.application;

import br.com.oficinasampaio.shared.domain.RegraNegocioException;

public final class PoliticaSenhaUsuario {

    public static final int TAMANHO_MINIMO = 8;
    public static final int TAMANHO_MAXIMO = 72;
    public static final String MENSAGEM_TAMANHO = "Senha deve possuir de 8 a 72 caracteres";

    private PoliticaSenhaUsuario() {
    }

    public static void validar(String senha) {
        if (senha == null || senha.length() < TAMANHO_MINIMO || senha.length() > TAMANHO_MAXIMO) {
            throw new RegraNegocioException(MENSAGEM_TAMANHO);
        }
    }
}
