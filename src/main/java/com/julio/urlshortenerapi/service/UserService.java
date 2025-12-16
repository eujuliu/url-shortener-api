package com.julio.urlshortenerapi.service;

import com.julio.urlshortenerapi.dto.UserRequestDTO;
import com.julio.urlshortenerapi.dto.UserResponseDTO;
import com.julio.urlshortenerapi.model.User;
import com.julio.urlshortenerapi.repository.UserRepository;
import com.julio.urlshortenerapi.shared.errors.ConflictError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  public UserResponseDTO create(UserRequestDTO data) throws ConflictError {
    User exists = this.userRepository.findByEmail(data.getEmail());

    if (exists != null) {
      throw new ConflictError(null, 0);
    }

    User user = User.builder()
      .name(data.getName())
      .email(data.getEmail())
      .password(this.passwordEncoder.encode(data.getPassword()))
      .build();

    this.userRepository.save(user);

    return UserResponseDTO.builder()
      .name(user.getName())
      .email(user.getEmail())
      .userId(user.getUserId().toString())
      .createdAt(user.getCreatedAt())
      .updatedAt(user.getUpdatedAt())
      .build();
  }
}
