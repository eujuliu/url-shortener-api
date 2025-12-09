package com.example.urlshortenerapi.shared.errors;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponseDTO(
        LocalDateTime timestamp,
        String error,
        String message,
        List<FieldError> fieldErrors
) {
    public record FieldError(
            String field,
            String message
    ) {
    }
}
