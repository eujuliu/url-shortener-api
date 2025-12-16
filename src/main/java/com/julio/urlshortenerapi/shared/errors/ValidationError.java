package com.julio.urlshortenerapi.shared.errors;

import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;

public class ValidationError extends Error {

  @Nullable
  public final List<String> fields;

  public ValidationError(
    @Nullable String message,
    @Nullable int code,
    @Nullable List<String> fields
  ) {
    super(
      message != null ? message : "A validation error happens",
      code != 0 ? code : HttpStatus.BAD_REQUEST.value(),
      "ValidationError"
    );
    this.fields = fields != null ? fields : null;
  }
}
