package com.julio.urlshortenerapi.shared.errors;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalControllerAdvice {

  private static final Logger logger = LoggerFactory.getLogger(
    GlobalControllerAdvice.class
  );

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Error> handleValidationException(
    MethodArgumentNotValidException ex
  ) {
    List<String> errors = ex
      .getBindingResult()
      .getFieldErrors()
      .stream()
      .map(error ->
        String.format("%s - %s", error.getField(), error.getDefaultMessage())
      )
      .toList();

    ValidationError error = new ValidationError(null, 0, errors);

    logger.error(error.fields.toString());

    return ResponseEntity.status(error.code).body(error);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Error> handleAllExceptions(Exception ex) {
    logger.error(
      String.format("%s: %s", ex.getClass().getName(), ex.getMessage())
    );

    if (ex instanceof Error) {
      Error e = (Error) ex;

      ResponseEntity<Error> response = ResponseEntity.status(e.code).body(e);

      return response;
    }

    InternalServerError error = new InternalServerError(null, 0);

    return ResponseEntity.status(error.code).body(error);
  }
}
