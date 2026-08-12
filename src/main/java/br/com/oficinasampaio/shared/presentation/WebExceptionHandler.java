package br.com.oficinasampaio.shared.presentation;

import br.com.oficinasampaio.shared.domain.RecursoNaoEncontradoException;
import br.com.oficinasampaio.shared.domain.RegraNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class WebExceptionHandler {

    public static final String MENSAGEM_CONFLITO =
            "A ordem foi alterada por outro usuário. Recarregue a página e tente novamente.";

    private static final String TELA_DE_AVISO = "erro/aviso";

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ModelAndView recursoNaoEncontrado(RecursoNaoEncontradoException exception) {
        return aviso(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ModelAndView regraDeNegocioViolada(RegraNegocioException exception) {
        return aviso(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ModelAndView conflitoDeConcorrencia() {
        return aviso(HttpStatus.CONFLICT, MENSAGEM_CONFLITO);
    }

    /**
     * A mensagem da exceção é escrita para o usuário — é regra de negócio, não
     * rastreamento — então vai para a tela como detalhe do aviso. O status HTTP
     * continua o mesmo de antes: o que muda é só quem lê o resultado.
     */
    private static ModelAndView aviso(HttpStatus status, String detalhe) {
        var tela = new ModelAndView(TELA_DE_AVISO, status);
        tela.addObject("aviso", AvisoDeErro.de(status.value(), detalhe));
        return tela;
    }
}