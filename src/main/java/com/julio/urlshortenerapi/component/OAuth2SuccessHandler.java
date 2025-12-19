package com.julio.urlshortenerapi.component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2SuccessHandler
  extends SimpleUrlAuthenticationSuccessHandler {

  @Override
  public void onAuthenticationSuccess(
    HttpServletRequest request,
    HttpServletResponse response,
    Authentication authentication
  ) throws IOException, ServletException {
    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    String userId = oAuth2User.getAttribute("user_id");
    String name = oAuth2User.getAttribute("name");
    String email = oAuth2User.getAttribute("email");
    String provider = oAuth2User.getAttribute("provider");

    HttpSession session = request.getSession();
    session.setAttribute("user_id", userId);
    session.setAttribute("user_name", name);
    session.setAttribute("user_email", email);
    session.setAttribute("login_provider", provider);

    super.onAuthenticationSuccess(request, response, authentication);
  }
}
