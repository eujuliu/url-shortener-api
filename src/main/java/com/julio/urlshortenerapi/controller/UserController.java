package com.julio.urlshortenerapi.controller;

import com.julio.urlshortenerapi.dto.AccessTokenResponseDTO;
import com.julio.urlshortenerapi.dto.UserRequestDTO;
import com.julio.urlshortenerapi.dto.UserResponseDTO;
import com.julio.urlshortenerapi.model.User;
import com.julio.urlshortenerapi.service.JwtService;
import com.julio.urlshortenerapi.service.UserService;
import com.julio.urlshortenerapi.shared.errors.ConflictError;
import com.julio.urlshortenerapi.shared.errors.NotFoundError;
import com.julio.urlshortenerapi.shared.errors.UnauthorizedError;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class UserController {

  @Autowired
  private UserService userService;

  @Autowired
  private JwtService jwtService;

  @Value("${security.jwt.refresh-token.expiration-time}")
  private int refreshTokenExpiration;

  @Value("${security.jwt.refresh-token.cookie.secure}")
  private boolean refreshTokenCookieSecure;

  @PostMapping("/register")
  public AccessTokenResponseDTO register(
    @Validated(UserRequestDTO.OnCreate.class) @RequestBody UserRequestDTO body,
    @CookieValue(value = "refresh_token", required = false) String token,
    HttpServletRequest servletRequest,
    HttpServletResponse servletResponse
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
      getUserIp(servletRequest),
      getUserDevice(servletRequest)
    );

    setRefreshToken(refreshToken, servletResponse);

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
    @CookieValue(value = "refresh_token", required = false) String token,
    HttpServletRequest servletRequest,
    HttpServletResponse servletResponse
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
      getUserIp(servletRequest),
      getUserDevice(servletRequest)
    );

    setRefreshToken(refreshToken, servletResponse);

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
  public String refresh(
    @CookieValue(value = "refresh_token", required = false) String token,
    HttpServletResponse servletResponse
  ) throws UnauthorizedError, NotFoundError {
    if (token != null) {
      throw new UnauthorizedError("Refresh token not found or invalid", 0);
    }

    boolean isValid = jwtService.isRefreshTokenValid(token);

    if (!isValid) {
      throw new UnauthorizedError("Refresh token not found or invalid", 0);
    }

    String email = jwtService.extractUsername(token);

    User user = userService.getUserByEmail(email);

    Map<String, String> tokens = jwtService.refresh(token, user);

    setRefreshToken(tokens.get("refreshToken"), servletResponse);

    return tokens.get("accessToken");
  }

  @GetMapping("/me")
  public AccessTokenResponseDTO getCurrentUser(
    @AuthenticationPrincipal OAuth2User principal,
    HttpServletRequest servletRequest,
    HttpServletResponse servletResponse
  ) throws NotFoundError, UnauthorizedError {
    User user = this.userService.getUserByEmail(
      principal.getAttribute("email")
    );

    String accessToken = jwtService.generateAccessToken(user);
    String refreshToken = jwtService.generateRefreshToken(
      user,
      getUserIp(servletRequest),
      getUserDevice(servletRequest)
    );

    setRefreshToken(refreshToken, servletResponse);

    UserResponseDTO userResponse = UserResponseDTO.builder()
      .userId(user.getUserId().toString())
      .name(user.getName())
      .email(user.getEmail())
      .createdAt(user.getCreatedAt())
      .updatedAt(user.getUpdatedAt())
      .build();

    return AccessTokenResponseDTO.builder()
      .accessToken(accessToken)
      .user(userResponse)
      .build();
  }

  private String getUserDevice(HttpServletRequest request) {
    String userAgent = request.getHeader("User-Agent");

    return userAgent;
  }

  private String getUserIp(HttpServletRequest request) {
    String ipAddress = request.getRemoteAddr();

    String forwardedForHeader = request.getHeader("X-Forwarded-For");

    if (forwardedForHeader != null && !forwardedForHeader.isEmpty()) {
      ipAddress = forwardedForHeader.split(",")[0].trim();
    }

    return ipAddress;
  }

  private void setRefreshToken(
    String token,
    HttpServletResponse servletResponse
  ) {
    Cookie refreshCookie = new Cookie("refresh_token", token);

    refreshCookie.setMaxAge(refreshTokenExpiration);
    refreshCookie.setSecure(refreshTokenCookieSecure);
    refreshCookie.setAttribute("SameSite", "Lax");
    refreshCookie.setHttpOnly(true);
    refreshCookie.setPath("/");

    servletResponse.addCookie(refreshCookie);
  }
}
