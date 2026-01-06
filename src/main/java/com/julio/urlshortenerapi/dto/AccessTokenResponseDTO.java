package com.julio.urlshortenerapi.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccessTokenResponseDTO {

  private UserResponseDTO user;
  private String accessToken;
}
