package com.julio.urlshortenerapi.shared.errors;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;

public class UnauthorizedError extends Error {

  public UnauthorizedError(@Nullable String message, @Nullable int code) {
    super(
      message != null ? message : "Not authorized to access the resource",
      code != 0 ? code : HttpStatus.UNAUTHORIZED.value(),
      "UnauthorizedError"
    );
  }
}
