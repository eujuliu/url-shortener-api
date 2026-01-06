package com.julio.urlshortenerapi.model;

import java.io.Serializable;
import java.util.UUID;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@PrimaryKeyClass
public class OAuthProviderKey implements Serializable {

  @PrimaryKeyColumn(
    name = "user_id",
    ordinal = 0,
    type = PrimaryKeyType.PARTITIONED
  )
  private UUID userId;

  @PrimaryKeyColumn(
    name = "provider",
    ordinal = 1,
    type = PrimaryKeyType.PARTITIONED
  )
  private String provider;
}
