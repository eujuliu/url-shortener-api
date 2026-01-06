package com.julio.urlshortenerapi.component;

import com.julio.urlshortenerapi.model.User;
import com.julio.urlshortenerapi.service.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2SuccessHandler
  extends SimpleUrlAuthenticationSuccessHandler {

  private static final Logger LOG = LoggerFactory.getLogger(
    OAuth2SuccessHandler.class
  );

  @Autowired
  private JwtService jwtService;

  @Value("${security.jwt.refresh-token.expiration-time}")
  private int refreshTokenExpiration;

  @Value("${security.jwt.refresh-token.cookie.secure}")
  private boolean refreshTokenCookieSecure;

  @Override
  public void onAuthenticationSuccess(
    HttpServletRequest sRequest,
    HttpServletResponse sResponse,
    Authentication authentication
  ) throws IOException, ServletException {
    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    String userId = oAuth2User.getAttribute("user_id");
    String email = oAuth2User.getAttribute("email");
    String name = oAuth2User.getAttribute("name");

    User user = User.builder()
      .userId(UUID.fromString(userId))
      .email(email)
      .name(name)
      .build();

    String refreshToken = jwtService.generateRefreshToken(
      user,
      ControllerHelpers.getUserIp(sRequest),
      ControllerHelpers.getUserDevice(sRequest)
    );

    ControllerHelpers.setRefreshToken(refreshToken, sResponse);

    LOG.info(
      "oauth2 login completed with success for user id {}",
      user.getUserId().toString()
    );

    super.onAuthenticationSuccess(sRequest, sResponse, authentication);
  }
}
