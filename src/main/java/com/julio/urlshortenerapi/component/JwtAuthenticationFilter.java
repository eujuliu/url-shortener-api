package com.julio.urlshortenerapi.component;

import com.julio.urlshortenerapi.model.User;
import com.julio.urlshortenerapi.service.JwtService;
import com.julio.urlshortenerapi.shared.errors.UnauthorizedError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger LOG = LoggerFactory.getLogger(
    JwtAuthenticationFilter.class
  );

  @Autowired
  private HandlerExceptionResolver handlerExceptionResolver;

  @Autowired
  private JwtService jwtService;

  @Autowired
  private UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(
    @NonNull HttpServletRequest request,
    @NonNull HttpServletResponse response,
    @NonNull FilterChain filterChain
  ) throws ServletException, IOException {
    final String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      LOG.error(
        "User {} without Bearer Header",
        ControllerHelpers.getUserIp(request)
      );
      filterChain.doFilter(request, response);
      return;
    }

    try {
      final String jwt = authHeader.substring(7);
      final String userEmail = jwtService.extractUsername(jwt);

      Authentication authentication =
        SecurityContextHolder.getContext().getAuthentication();

      if (userEmail != null && authentication == null) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(
          userEmail
        );

        if (jwtService.isAccessTokenValid(jwt, (User) userDetails)) {
          UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(
              userDetails,
              null,
              userDetails.getAuthorities()
            );

          authToken.setDetails(
            new WebAuthenticationDetailsSource().buildDetails(request)
          );

          SecurityContextHolder.getContext().setAuthentication(authToken);

          LOG.info(
            "created security context for user id {}",
            userDetails.getUsername()
          );
        }
      }

      filterChain.doFilter(request, response);
    } catch (Exception exception) {
      LOG.error(
        "Error processing request: {} {}",
        request.getMethod(),
        request.getRequestURI(),
        exception
      );

      UnauthorizedError error = new UnauthorizedError(
        "JWT authentication failed",
        0
      );

      handlerExceptionResolver.resolveException(request, response, null, error);
    }
  }
}
