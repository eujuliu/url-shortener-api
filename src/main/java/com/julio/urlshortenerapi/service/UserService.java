package com.julio.urlshortenerapi.service;

import com.julio.urlshortenerapi.dto.UserRequestDTO;
import com.julio.urlshortenerapi.dto.UserResponseDTO;
import com.julio.urlshortenerapi.model.OAuthProvider;
import com.julio.urlshortenerapi.model.User;
import com.julio.urlshortenerapi.repository.OAuthProviderRepository;
import com.julio.urlshortenerapi.repository.UserRepository;
import com.julio.urlshortenerapi.shared.errors.ConflictError;
import com.julio.urlshortenerapi.shared.errors.NotFoundError;
import com.julio.urlshortenerapi.shared.errors.UnauthorizedError;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private OAuthProviderRepository oauthProviderRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Override
  public UserDetails loadUserByUsername(String email)
    throws UsernameNotFoundException {
    com.julio.urlshortenerapi.model.User user = userRepository.findByEmail(
      email
    );

    if (user == null) {
      throw new UsernameNotFoundException("User not found");
    }

    return org.springframework.security.core.userdetails.User.withUsername(
      user.getEmail()
    )
      .password(user.getPassword() != null ? user.getPassword() : "")
      .authorities(Collections.emptyList())
      .build();
  }

  public UserResponseDTO create(UserRequestDTO data) throws ConflictError {
    User user = this.userRepository.findByEmail(data.getEmail());

    if (user != null) {
      throw new ConflictError(null, 0);
    }

    user = User.builder()
      .name(data.getName())
      .email(data.getEmail())
      .password(this.passwordEncoder.encode(data.getPassword()))
      .build();

    this.userRepository.save(user);

    OAuthProvider provider = OAuthProvider.builder()
      .email(user.getEmail())
      .userId(user.getUserId())
      .provider("password")
      .emailVerified(false)
      .build();

    this.oauthProviderRepository.save(provider);

    return UserResponseDTO.builder()
      .name(user.getName())
      .email(user.getEmail())
      .userId(user.getUserId().toString())
      .createdAt(user.getCreatedAt())
      .updatedAt(user.getUpdatedAt())
      .build();
  }

  public UserResponseDTO show(UserRequestDTO data)
    throws NotFoundError, ConflictError, UnauthorizedError {
    User user = this.userRepository.findByEmail(data.getEmail());

    if (user == null) {
      throw new NotFoundError(null, 0);
    }

    if (user.getPassword() == null || user.getPassword().isEmpty()) {
      throw new ConflictError(
        "Please login using your Social Provider (Google or Github)",
        0
      );
    }

    if (!passwordEncoder.matches(data.getPassword(), user.getPassword())) {
      throw new UnauthorizedError("Invalid Password or Email", 0);
    }

    return UserResponseDTO.builder()
      .name(user.getName())
      .email(user.getEmail())
      .userId(user.getUserId().toString())
      .createdAt(user.getCreatedAt())
      .updatedAt(user.getUpdatedAt())
      .build();
  }

  public UserResponseDTO getUserByEmail(String email) throws NotFoundError {
    if (email == null) {
      throw new NotFoundError("User not found for this email", 0);
    }

    User user = userRepository.findByEmail(email);

    if (user == null) {
      throw new NotFoundError("User not found for this email", 0);
    }

    return UserResponseDTO.builder()
      .userId(user.getUserId().toString())
      .name(user.getName())
      .email(user.getEmail())
      .createdAt(user.getCreatedAt())
      .updatedAt(user.getUpdatedAt())
      .build();
  }
}
