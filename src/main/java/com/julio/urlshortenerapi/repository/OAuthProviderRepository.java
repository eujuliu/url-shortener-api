package com.julio.urlshortenerapi.repository;

import com.julio.urlshortenerapi.model.OAuthProvider;
import com.julio.urlshortenerapi.model.OAuthProviderKey;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OAuthProviderRepository
  extends CassandraRepository<OAuthProvider, OAuthProviderKey> {
  @Nullable
  @Query("SELECT * FROM oauth_providers WHERE user_id = ?0")
  OAuthProvider findByUserId(UUID userId);

  @Nullable
  @Query("SELECT * FROM oauth_providers WHERE user_id = ?0 AND provider = ?1")
  OAuthProvider findByUserIdAndProvider(UUID userId, String provider);

  @Nullable
  @Query("SELECT * FROM oauth_providers WHERE email = ?0")
  OAuthProvider findByEmail(String email);
}
