package com.julio.urlshortenerapi.shared.errors;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;

public class NotFoundError extends Error {

  public NotFoundError(@Nullable String message, @Nullable int code) {
    super(
      message != null ? message : "Is not possible to find this resource",
      code != 0 ? code : HttpStatus.NOT_FOUND.value(),
      "NotFoundError"
    );
  }
}
