package com.julio.urlshortenerapi.shared.errors;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;

public class InternalServerError extends Error {

  public InternalServerError(@Nullable String message, @Nullable int code) {
    super(
      message != null ? message : "An unexpected error occurred",
      code != 0 ? code : HttpStatus.INTERNAL_SERVER_ERROR.value(),
      "InternalServerError"
    );
  }
}
