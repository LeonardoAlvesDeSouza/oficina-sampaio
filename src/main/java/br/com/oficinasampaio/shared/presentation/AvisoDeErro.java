package br.com.oficinasampaio.shared.presentation;

/**
 * O que a tela de fim de linha mostra. Traduz o código HTTP para uma frase que
 * o balcão entende: quem está atendendo um cliente não deve ver a página branca
 * do servidor nem o texto cru de uma exceção.
 * <p>
 * O código continua na tela porque é o que o suporte pede quando alguém liga.
 *
 * @param detalhe explicação específica do caso, quando existe uma que ajude a
 *                resolver; nula quando só o código é conhecido
 */
public record AvisoDeErro(int codigo, String titulo, String explicacao, String detalhe) {

    public static final int ERRO_INTERNO = 500;

    private static final int PEDIDO_RECUSADO = 400;
    private static final int SEM_PERMISSAO = 403;
    private static final int NAO_ENCONTRADO = 404;
    private static final int ALTERACAO_CRUZADA = 409;

    public static AvisoDeErro de(int codigo, String detalhe) {
        return switch (codigo) {
            case PEDIDO_RECUSADO -> new AvisoDeErro(codigo,
                    "Pedido recusado",
                    "A oficina não aceitou esses dados. Confira o que foi digitado e tente de novo.",
                    detalhe);
            case SEM_PERMISSAO -> new AvisoDeErro(codigo,
                    "Área restrita",
                    "Seu acesso não abre esta parte do sistema. Fale com quem administra a oficina.",
                    detalhe);
            case NAO_ENCONTRADO -> new AvisoDeErro(codigo,
                    "Não encontramos",
                    "Esse registro não existe ou o endereço está errado.",
                    detalhe);
            case ALTERACAO_CRUZADA -> new AvisoDeErro(codigo,
                    "Alteração cruzada",
                    "Outra pessoa mexeu neste registro antes de você.",
                    detalhe);
            default -> new AvisoDeErro(codigo,
                    "Falha no sistema",
                    "O problema foi aqui dentro, não no que você fez. Tente de novo em instantes.",
                    detalhe);
        };
    }
}