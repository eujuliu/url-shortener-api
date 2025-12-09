/* (C)2025 */
package com.example.urlshortenerapi.features.user;

import com.example.urlshortenerapi.features.user.dto.UserRequestDTO;
import com.example.urlshortenerapi.features.user.dto.UserResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserResponseDTO create(UserRequestDTO request) {
        User exists = this.userRepository.findByEmail(request.getEmail());

        if (exists != null) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        User user =
                User.builder()
                        .name(request.getName())
                        .email(request.getEmail())
                        .password(this.passwordEncoder.encode(request.getPassword()))
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
