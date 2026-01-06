package com.julio.urlshortenerapi.controller;

import com.julio.urlshortenerapi.component.ControllerHelpers;
import com.julio.urlshortenerapi.dto.AccessTokenResponseDTO;
import com.julio.urlshortenerapi.dto.UserRequestDTO;
import com.julio.urlshortenerapi.dto.UserResponseDTO;
import com.julio.urlshortenerapi.model.User;
import com.julio.urlshortenerapi.service.JwtService;
import com.julio.urlshortenerapi.service.UserService;
import com.julio.urlshortenerapi.shared.errors.ConflictError;
import com.julio.urlshortenerapi.shared.errors.NotFoundError;
import com.julio.urlshortenerapi.shared.errors.UnauthorizedError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class UserController {

  @Autowired
  private UserService userService;

  @Autowired
  private JwtService jwtService;

  @PostMapping("/register")
  public AccessTokenResponseDTO register(
    @Validated(UserRequestDTO.OnCreate.class) @RequestBody UserRequestDTO body,
    @CookieValue(name = "refresh_token", required = false) String token,
    HttpServletRequest sRequest,
    HttpServletResponse sResponse
  ) throws ConflictError, Exception {
    if (token != null) {
      jwtService.revoke(token);
    }

    User user = this.userService.create(
      body.getEmail(),
      body.getName(),
      body.getPassword()
    );

    String accessToken = jwtService.generateAccessToken(user);
    String refreshToken = jwtService.generateRefreshToken(
      user,
      ControllerHelpers.getUserIp(sRequest),
      ControllerHelpers.getUserDevice(sRequest)
    );

    ControllerHelpers.setRefreshToken(refreshToken, sResponse);

    UserResponseDTO userResponse = UserResponseDTO.builder()
      .userId(user.getUserId().toString())
      .name(user.getName())
      .email(user.getEmail())
      .createdAt(user.getCreatedAt())
      .updatedAt(user.getUpdatedAt())
      .build();

    return AccessTokenResponseDTO.builder()
      .user(userResponse)
      .accessToken(accessToken)
      .build();
  }

  @PostMapping("/login")
  public AccessTokenResponseDTO login(
    @Validated(UserRequestDTO.OnLogin.class) @RequestBody UserRequestDTO body,
    @CookieValue(name = "refresh_token", required = false) String token,
    HttpServletRequest sRequest,
    HttpServletResponse sResponse
  ) throws NotFoundError, ConflictError, UnauthorizedError {
    if (token != null) {
      jwtService.revoke(token);
    }

    User user = this.userService.getUserByEmailAndPassword(
      body.getEmail(),
      body.getPassword()
    );

    String accessToken = jwtService.generateAccessToken(user);
    String refreshToken = jwtService.generateRefreshToken(
      user,
      ControllerHelpers.getUserIp(sRequest),
      ControllerHelpers.getUserDevice(sRequest)
    );

    ControllerHelpers.setRefreshToken(refreshToken, sResponse);

    UserResponseDTO userResponse = UserResponseDTO.builder()
      .userId(user.getUserId().toString())
      .name(user.getName())
      .email(user.getEmail())
      .createdAt(user.getCreatedAt())
      .updatedAt(user.getUpdatedAt())
      .build();

    return AccessTokenResponseDTO.builder()
      .user(userResponse)
      .accessToken(accessToken)
      .build();
  }

  @GetMapping("/refresh")
  public AccessTokenResponseDTO refresh(
    @CookieValue("refresh_token") String token,
    HttpServletResponse sResponse
  ) throws UnauthorizedError, NotFoundError {
    boolean isValid = jwtService.isRefreshTokenValid(token);

    if (!isValid) {
      throw new UnauthorizedError("Refresh token invalid", 0);
    }

    String email = jwtService.extractUsername(token);

    User user = userService.getUserByEmail(email);

    Map<String, String> tokens = jwtService.refresh(token, user);

    ControllerHelpers.setRefreshToken(tokens.get("refreshToken"), sResponse);

    UserResponseDTO userResponse = UserResponseDTO.builder()
      .userId(user.getUserId().toString())
      .name(user.getName())
      .email(user.getEmail())
      .createdAt(user.getCreatedAt())
      .updatedAt(user.getUpdatedAt())
      .build();

    return AccessTokenResponseDTO.builder()
      .accessToken(tokens.get("accessToken"))
      .user(userResponse)
      .build();
  }

  @ExceptionHandler(MissingRequestCookieException.class)
  public ResponseEntity<UnauthorizedError> cookieError(
    MissingRequestCookieException ex
  ) {
    UnauthorizedError error = new UnauthorizedError(
      "Refresh token cookie is required",
      0
    );

    return ResponseEntity.status(error.code).body(error);
  }
}
