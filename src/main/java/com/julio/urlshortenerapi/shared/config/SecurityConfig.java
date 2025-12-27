package com.julio.urlshortenerapi.shared.config;

import com.julio.urlshortenerapi.component.JwtAuthenticationFilter;
import com.julio.urlshortenerapi.component.OAuth2SuccessHandler;
import com.julio.urlshortenerapi.service.OAuth2Service;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Autowired
  private OAuth2Service oauth2Service;

  @Autowired
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Autowired
  private OAuth2SuccessHandler oAuth2SuccessHandler;

  @Bean
  public SecurityFilterChain securityFilterChain(
    HttpSecurity http,
    @Value("${security.oauth2.success-url}") String successUrl,
    @Value("${security.oauth2.failure-url}") String failureUrl
  ) throws Exception {
    http.csrf(
      csrf -> csrf.disable()
      // .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
      // .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
      // .ignoringRequestMatchers(
      //   "/api/v1/login",
      //   "/api/v1/register",
      //   "/api/v1/refresh"
      // )
    );

    http.sessionManagement(session ->
      session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    );

    http.addFilterBefore(
      jwtAuthenticationFilter,
      UsernamePasswordAuthenticationFilter.class
    );

    http.authorizeHttpRequests(auth -> {
      auth
        .requestMatchers(HttpMethod.POST, "/api/v1/login", "/api/v1/register")
        .permitAll();
      auth
        .requestMatchers("/actuator/**", "/api/v1/login/**", "/api/v1/refresh")
        .permitAll();
      auth.anyRequest().authenticated();
    });

    http.oauth2Login(oauth2 -> {
      oauth2.authorizationEndpoint(auth -> {
        auth.baseUri("/api/v1/login/oauth2/authorization");
      });
      oauth2.redirectionEndpoint(redirection ->
        redirection.baseUri("/api/v1/login/oauth2/code/*")
      );

      oAuth2SuccessHandler.setDefaultTargetUrl(successUrl);
      oauth2.successHandler(oAuth2SuccessHandler);

      oauth2.failureUrl(failureUrl);

      oauth2.userInfoEndpoint(info -> {
        info.userService(oauth2Service);
      });
    });

    http.exceptionHandling(exceptions -> {
      exceptions.authenticationEntryPoint(
        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
      );
    });

    return http.build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource(
    @Value("${security.cors.allowed-origins}") List<String> allowedOrigins,
    @Value("${security.cors.allowed-methods}") List<String> allowedMethods,
    @Value("${security.cors.allowed-headers}") List<String> allowedHeaders,
    @Value("${security.cors.max-age}") long maxAge
  ) {
    CorsConfiguration configuration = new CorsConfiguration();

    configuration.setAllowedOrigins(allowedOrigins);
    configuration.setAllowedMethods(allowedMethods);
    configuration.setAllowedHeaders(allowedHeaders);
    configuration.setMaxAge(maxAge);
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source =
      new UrlBasedCorsConfigurationSource();

    source.registerCorsConfiguration("/**", configuration);

    return source;
  }
}
