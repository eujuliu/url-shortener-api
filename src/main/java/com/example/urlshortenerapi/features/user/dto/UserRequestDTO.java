/* (C)2025 */
package com.example.urlshortenerapi.features.user.dto;

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
    @NotBlank
    @Size(min = 5, max = 64)
    @Pattern(
            regexp = "^[A-Za-z0-9]+$",
            message = "Name must contain only letters and numbers"
    )
    private String name;

    @Email
    @Size(max = 254)
    @NotBlank
    @NotNull
    private String email;

    @Size(min = 8, max = 72)
    @Pattern.List({
            @Pattern(
                    regexp = ".*[a-z].*",
                    message = "Password must contain at least 1 lowercase letter"),
            @Pattern(
                    regexp = ".*[A-Z].*",
                    message = "Password must contain at least 1 uppercase letter"),
            @Pattern(regexp = ".*\\d.*", message = "Password must contain at least 1 digit"),
            @Pattern(
                    regexp = ".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*",
                    message = "Password must contain at least 1 special character")
    })
    private String password;
}
