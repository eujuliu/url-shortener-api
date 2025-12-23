package com.julio.urlshortenerapi.repository;

import com.julio.urlshortenerapi.model.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository
  extends CrudRepository<RefreshToken, String> {}
