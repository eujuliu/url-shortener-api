package com.julio.urlshortenerapi.controller;

import com.julio.urlshortenerapi.dto.UserRequestDTO;
import com.julio.urlshortenerapi.dto.UserResponseDTO;
import com.julio.urlshortenerapi.service.UserService;
import com.julio.urlshortenerapi.shared.errors.ConflictError;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class UserController {

  @Autowired
  private UserService userService;

  @PostMapping("/user")
  private UserResponseDTO create(@RequestBody @Valid UserRequestDTO request)
    throws ConflictError {
    return this.userService.create(request);
  }
}
