/* (C)2025 */
package com.example.urlshortenerapi.features.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.jspecify.annotations.Nullable;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("users")
@ToString(exclude = {"password"})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @PrimaryKey
    @Column("user_id")
    @Builder.Default
    private UUID userId = UUID.randomUUID();

    private String name;

    @Indexed
    private String email;

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.NONE)
    @JsonIgnore
    @Nullable
    private String password;

    @Setter(AccessLevel.NONE)
    @Column("created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column("updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
