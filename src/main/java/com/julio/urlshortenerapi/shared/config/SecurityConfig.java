package com.julio.urlshortenerapi.shared.config;

import com.julio.urlshortenerapi.service.OAuth2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Autowired
  private OAuth2Service oauth2Service;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(
    HttpSecurity http,
    @Value("${app.security.oauth2.success-url}") String successUrl,
    @Value("${app.security.oauth2.failure-url}") String failureUrl
  ) throws Exception {
    http.csrf(csrf ->
      csrf
        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
        .ignoringRequestMatchers("/api/v1/user")
    );

    http.authorizeHttpRequests(auth -> {
      auth.requestMatchers(HttpMethod.POST, "/api/v1/user").permitAll();
      auth.requestMatchers("/actuator/**", "/api/v1/login/**").permitAll();
      auth.anyRequest().authenticated();
    });

    http.oauth2Login(oauth2 -> {
      oauth2.authorizationEndpoint(auth -> {
        auth.baseUri("/api/v1/login/oauth2/authorization");
      });
      oauth2.redirectionEndpoint(redirection ->
        redirection.baseUri("/api/v1/login/oauth2/code/*")
      );

      oauth2.defaultSuccessUrl(successUrl, true);
      oauth2.failureUrl(failureUrl);

      oauth2.userInfoEndpoint(info -> {
        info.userService(this.oauth2Service);
      });
    });

    http.exceptionHandling(exceptions -> {
      exceptions.authenticationEntryPoint(new Http403ForbiddenEntryPoint());
    });

    return http.build();
  }
}
