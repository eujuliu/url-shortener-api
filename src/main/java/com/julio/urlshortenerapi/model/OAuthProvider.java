package com.julio.urlshortenerapi.model;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("oauth_providers")
@Builder
@Data
@ToString
public class OAuthProvider {

  @PrimaryKeyColumn(
    name = "user_id",
    ordinal = 0,
    type = PrimaryKeyType.PARTITIONED
  )
  @Setter(AccessLevel.NONE)
  private UUID userId;

  @PrimaryKeyColumn(
    name = "provider",
    ordinal = 1,
    type = PrimaryKeyType.PARTITIONED
  )
  @Setter(AccessLevel.NONE)
  private String provider;

  @Indexed
  private String email;

  @Column("email_verified")
  @Builder.Default
  private boolean emailVerified = false;

  @Setter(AccessLevel.NONE)
  @Column("created_at")
  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();
}
