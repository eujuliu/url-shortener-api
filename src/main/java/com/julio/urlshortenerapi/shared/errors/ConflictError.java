package com.julio.urlshortenerapi.shared.errors;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;

public class ConflictError extends Error {

  public ConflictError(@Nullable String message, @Nullable int code) {
    super(
      message != null ? message : "Conflict with the current state",
      code != 0 ? code : HttpStatus.CONFLICT.value(),
      "ConflictError"
    );
  }
}
