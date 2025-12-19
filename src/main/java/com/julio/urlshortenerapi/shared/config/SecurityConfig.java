package com.julio.urlshortenerapi.shared.config;

import com.julio.urlshortenerapi.component.OAuth2SuccessHandler;
import com.julio.urlshortenerapi.service.OAuth2Service;
import com.julio.urlshortenerapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Autowired
  private OAuth2Service oauth2Service;

  @Autowired
  private OAuth2SuccessHandler oAuth2SuccessHandler;

  @Bean
  public SecurityFilterChain securityFilterChain(
    HttpSecurity http,
    TokenBasedRememberMeServices rememberMeServices,
    @Value("${app.security.oauth2.success-url}") String successUrl,
    @Value("${app.security.oauth2.failure-url}") String failureUrl,
    @Value("${app.security.remember-me.key}") String rememberMeKey
  ) throws Exception {
    http.csrf(csrf ->
      csrf
        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
        .ignoringRequestMatchers("/api/v1/login", "/api/v1/register")
    );

    http.authorizeHttpRequests(auth -> {
      auth
        .requestMatchers(HttpMethod.POST, "/api/v1/login", "/api/v1/register")
        .permitAll();
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

      oauth2.successHandler(this.oAuth2SuccessHandler);

      this.oAuth2SuccessHandler.setDefaultTargetUrl(successUrl);
      oauth2.failureUrl(failureUrl);

      oauth2.userInfoEndpoint(info -> {
        info.userService(this.oauth2Service);
      });
    });

    http.rememberMe(remember -> {
      remember.rememberMeServices(rememberMeServices);
    });

    http.exceptionHandling(exceptions -> {
      exceptions.authenticationEntryPoint(new Http403ForbiddenEntryPoint());
    });

    return http.build();
  }

  @Bean
  public TokenBasedRememberMeServices rememberMeServices(
    UserService userService,
    @Value("${app.security.remember-me.key}") String rememberMeKey
  ) {
    TokenBasedRememberMeServices rememberMeServices =
      new TokenBasedRememberMeServices(rememberMeKey, userService);

    rememberMeServices.setTokenValiditySeconds(604800);
    rememberMeServices.setAlwaysRemember(true);
    return rememberMeServices;
  }
}
