package br.com.oficinasampaio.shared.presentation;

import br.com.oficinasampaio.shared.domain.RecursoNaoEncontradoException;
import br.com.oficinasampaio.shared.domain.RegraNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class WebExceptionHandler {

    public static final String MENSAGEM_CONFLITO =
            "A ordem foi alterada por outro usuário. Recarregue a página e tente novamente.";

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<String> recursoNaoEncontrado(RecursoNaoEncontradoException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<String> regraDeNegocioViolada(RegraNegocioException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<String> conflitoDeConcorrencia() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(MENSAGEM_CONFLITO);
    }
}
