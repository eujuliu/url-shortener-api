package com.julio.urlshortenerapi.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDTO {

  private String userId;
  private String name;
  private String email;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
