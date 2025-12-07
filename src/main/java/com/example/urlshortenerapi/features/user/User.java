/* (C)2025 */
package com.example.urlshortenerapi.features.user;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jspecify.annotations.Nullable;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("users")
@ToString(exclude = {"password"})
@Getter(AccessLevel.PUBLIC)
@Setter(AccessLevel.PUBLIC)
public class User {
    @PrimaryKey
    private UUID user_id = UUID.randomUUID();

    private String name;
    private String email;

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.NONE)
    @Nullable
    private String password;

    private String picture_url;
    private String locale;

    @Setter(AccessLevel.NONE)
    private LocalDateTime created_at = LocalDateTime.now();

    private LocalDateTime updated_at = LocalDateTime.now();

    public User(
            @Nullable UUID user_id,
            String name,
            String email,
            @Nullable String password,
            String picture_url,
            String locale,
            @Nullable LocalDateTime created_at,
            @Nullable LocalDateTime updated_at) {
        this.user_id = user_id != null ? user_id : this.user_id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.picture_url = picture_url;
        this.locale = locale;
        this.created_at = created_at != null ? created_at : this.created_at;
        this.updated_at = updated_at != null ? updated_at : this.updated_at;
    }
}
