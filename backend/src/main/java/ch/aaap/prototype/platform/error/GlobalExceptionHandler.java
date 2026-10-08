package ch.aaap.prototype.platform.error;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * The one place that turns exceptions into RFC 9457 ProblemDetail responses. Services throw
 * NotFoundException / ConflictException and never build error responses themselves. Validation
 * errors carry {@code errors: [{field, message}]}, which the frontend maps onto form fields.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(NotFoundException.class)
  ProblemDetail notFound(NotFoundException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
  }

  @ExceptionHandler(ConflictException.class)
  ProblemDetail conflict(ConflictException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
  }

  // Two saves of the same version at once: the service's version check passes for both, the
  // database's @Version check rejects the second.
  @ExceptionHandler(OptimisticLockingFailureException.class)
  ProblemDetail concurrentUpdate(OptimisticLockingFailureException e) {
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.CONFLICT, "This was changed by someone else. Reload it and try again.");
  }

  @ExceptionHandler(Exception.class)
  ProblemDetail unexpected(Exception e) {
    log.error("Unhandled exception", e);
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong.");
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException e,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    List<Map<String, String>> errors =
        e.getBindingResult().getFieldErrors().stream()
            .map(GlobalExceptionHandler::fieldError)
            .toList();
    return ResponseEntity.badRequest().body(validationProblem(errors));
  }

  @Override
  protected ResponseEntity<Object> handleHandlerMethodValidationException(
      HandlerMethodValidationException e,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    List<Map<String, String>> errors =
        e.getParameterValidationResults().stream()
            .flatMap(
                result ->
                    result.getResolvableErrors().stream()
                        .map(
                            error ->
                                Map.of(
                                    "field",
                                    String.valueOf(result.getMethodParameter().getParameterName()),
                                    "message",
                                    String.valueOf(error.getDefaultMessage()))))
            .toList();
    return ResponseEntity.badRequest().body(validationProblem(errors));
  }

  private static ProblemDetail validationProblem(List<Map<String, String>> errors) {
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed.");
    problem.setProperty("errors", errors);
    return problem;
  }

  private static Map<String, String> fieldError(FieldError error) {
    return Map.of("field", error.getField(), "message", String.valueOf(error.getDefaultMessage()));
  }
}
