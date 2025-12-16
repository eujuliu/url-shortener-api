package com.julio.urlshortenerapi.shared.errors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(
  value = { "cause", "stackTrace", "suppressed", "localizedMessage" }
)
public class Error extends Exception {

  public final int code;
  public final String name;

  public Error(String message, int code, String name) {
    super(message);
    this.code = code;
    this.name = name;
  }
}
