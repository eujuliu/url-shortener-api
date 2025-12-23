package com.julio.urlshortenerapi.shared.config;

import com.julio.urlshortenerapi.model.User;
import com.julio.urlshortenerapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class ApplicationConfig {

  @Autowired
  private UserRepository userRepository;

  @Bean
  UserDetailsService userDetailsService() {
    return username -> {
      User user = userRepository.findByEmail(username);

      if (user == null) {
        throw new UsernameNotFoundException("User not found");
      }

      return user;
    };
  }

  @Bean
  BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
