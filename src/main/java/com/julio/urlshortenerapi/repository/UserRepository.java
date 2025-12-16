package com.julio.urlshortenerapi.repository;

import com.julio.urlshortenerapi.model.User;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CassandraRepository<User, UUID> {
  @Nullable
  User findByEmail(String email);

  @Nullable
  User findByUserId(UUID userId);
}
