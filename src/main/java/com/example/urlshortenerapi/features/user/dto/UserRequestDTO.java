/* (C)2025 */
package com.example.urlshortenerapi.features.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserCreateDto {
    @NotBlank
    @Size(min = 5, max = 64)
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Name must contain only letters and numbers")
    private String name;

    @Email
    @Size(max = 254)
    @NotBlank
    @NotNull
    private String email;

    @Null
    @Size(min = 8, max = 72)
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
            message = "Password must contain at least one lowercase and uppercase letter, one digit and one symbol")
    private String password;
}
