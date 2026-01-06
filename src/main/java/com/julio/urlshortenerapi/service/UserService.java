package com.julio.urlshortenerapi.service;

import com.julio.urlshortenerapi.model.OAuthProvider;
import com.julio.urlshortenerapi.model.User;
import com.julio.urlshortenerapi.repository.OAuthProviderRepository;
import com.julio.urlshortenerapi.repository.UserRepository;
import com.julio.urlshortenerapi.shared.errors.ConflictError;
import com.julio.urlshortenerapi.shared.errors.NotFoundError;
import com.julio.urlshortenerapi.shared.errors.UnauthorizedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private OAuthProviderRepository oauthProviderRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  public User create(String email, String name, String password)
    throws ConflictError, Exception {
    User user = this.userRepository.findByEmail(email);

    if (user != null) {
      throw new ConflictError(null, 0);
    }

    user = User.builder()
      .name(name)
      .email(email)
      .password(this.passwordEncoder.encode(password))
      .build();

    this.userRepository.save(user);

    LOG.debug("added new user to repository");

    OAuthProvider provider = OAuthProvider.builder()
      .email(user.getEmail())
      .userId(user.getUserId())
      .provider("password")
      .emailVerified(false)
      .build();

    this.oauthProviderRepository.save(provider);
    LOG.debug(
      "added new oauth to repository for user {}",
      user.getUserId().toString()
    );

    return user;
  }

  public User getUserByEmailAndPassword(String email, String password)
    throws NotFoundError, ConflictError, UnauthorizedError {
    User user = this.userRepository.findByEmail(email);

    if (user == null) {
      throw new NotFoundError(null, 0);
    }

    if (user.getPassword() == null || user.getPassword().isEmpty()) {
      throw new ConflictError(
        "Please login using your Social Provider (Google or Github)",
        0
      );
    }

    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new UnauthorizedError("Invalid Password or Email", 0);
    }

    LOG.debug("get user {} by email and password", user.getUserId().toString());

    return user;
  }

  public User getUserByEmail(String email) throws NotFoundError {
    if (email == null) {
      throw new NotFoundError("User not found for this email", 0);
    }

    User user = userRepository.findByEmail(email);

    if (user == null) {
      throw new NotFoundError("User not found for this email", 0);
    }

    LOG.debug("get user {} by email", user.getUserId().toString());

    return user;
  }
}
