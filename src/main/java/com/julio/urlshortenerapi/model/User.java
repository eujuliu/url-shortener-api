package com.julio.urlshortenerapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import lombok.ToString;
import org.jspecify.annotations.Nullable;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Table("users")
@ToString(exclude = { "password" })
@Data
@Builder
public class User implements UserDetails {

  @PrimaryKey
  @Column("user_id")
  @Builder.Default
  private UUID userId = UUID.randomUUID();

  private String name;

  @Indexed
  private String email;

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

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of();
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
