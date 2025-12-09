/* (C)2025 */
package com.example.urlshortenerapi.features.user;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends CassandraRepository<User, UUID> {
    User findByEmail(String email);

    User findByUserId(UUID user_id);
}
