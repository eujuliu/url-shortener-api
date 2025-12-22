package com.julio.urlshortenerapi.controller;

import com.julio.urlshortenerapi.dto.UserRequestDTO;
import com.julio.urlshortenerapi.dto.UserResponseDTO;
import com.julio.urlshortenerapi.service.UserService;
import com.julio.urlshortenerapi.shared.errors.ConflictError;
import com.julio.urlshortenerapi.shared.errors.NotFoundError;
import com.julio.urlshortenerapi.shared.errors.UnauthorizedError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.validation.annotation.Validated;
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
  private RememberMeServices rememberMeServices;

  @PostMapping("/register")
  public UserResponseDTO register(
    @Validated(
      UserRequestDTO.OnCreate.class
    ) @RequestBody UserRequestDTO request,
    HttpServletRequest servletRequest,
    HttpServletResponse servletResponse
  ) throws ConflictError, Exception {
    UserResponseDTO user = this.userService.create(request);

    UsernamePasswordAuthenticationToken auth = this.updateSession(
      user.getEmail(),
      user.getUserId(),
      user.getName(),
      servletRequest
    );

    this.rememberMeServices.loginSuccess(servletRequest, servletResponse, auth);

    return user;
  }

  @PostMapping("/login")
  public UserResponseDTO login(
    @Validated(
      UserRequestDTO.OnLogin.class
    ) @RequestBody UserRequestDTO request,
    HttpServletRequest servletRequest,
    HttpServletResponse servletResponse
  ) throws NotFoundError, ConflictError, UnauthorizedError {
    UserResponseDTO user = this.userService.show(request);

    UsernamePasswordAuthenticationToken auth = this.updateSession(
      user.getEmail(),
      user.getUserId(),
      user.getName(),
      servletRequest
    );

    this.rememberMeServices.loginSuccess(servletRequest, servletResponse, auth);

    return user;
  }

  @GetMapping("/me")
  public UserResponseDTO getCurrentUser(
    @AuthenticationPrincipal OAuth2User principal
  ) throws NotFoundError {
    return this.userService.getUserByEmail(principal.getAttribute("email"));
  }

  private UsernamePasswordAuthenticationToken updateSession(
    String email,
    String userId,
    String name,
    HttpServletRequest request
  ) {
    UsernamePasswordAuthenticationToken auth =
      new UsernamePasswordAuthenticationToken(
        email,
        null,
        Collections.emptyList()
      );

    SecurityContext sc = SecurityContextHolder.getContext();
    sc.setAuthentication(auth);

    HttpSession session = request.getSession(true);
    session.setAttribute(
      HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
      sc
    );
    session.setAttribute("user_id", userId);
    session.setAttribute("user_name", name);
    session.setAttribute("user_email", email);
    session.setAttribute("login_provider", "password");

    return auth;
  }
}
