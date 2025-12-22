package com.julio.urlshortenerapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {

  public interface OnCreate {}

  public interface OnLogin {}

  @NotBlank(groups = OnCreate.class)
  @Size(min = 5, max = 64, groups = OnCreate.class)
  @NotNull(groups = OnCreate.class)
  @Pattern(
    regexp = "^[A-Za-z0-9]+$",
    message = "Name must contain only letters and numbers",
    groups = OnCreate.class
  )
  private String name;

  @Email(groups = { OnCreate.class, OnLogin.class })
  @NotBlank(groups = { OnCreate.class, OnLogin.class })
  @NotNull(groups = { OnCreate.class, OnLogin.class })
  @Size(max = 254)
  private String email;

  @NotBlank(groups = { OnCreate.class, OnLogin.class })
  @NotNull(groups = { OnCreate.class, OnLogin.class })
  @Size(min = 8, max = 72, groups = { OnCreate.class, OnLogin.class })
  @Pattern(
    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).+$",
    message = "Password don't fill the requirements",
    groups = { OnCreate.class, OnLogin.class }
  )
  private String password;
}
