package com.julio.urlshortenerapi.component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public final class ControllerHelpers {

  private static long refreshTokenExpiration;
  private static boolean refreshTokenCookieSecure;

  @Value("${security.jwt.refresh-token.expiration-time}")
  public void setRefreshTokenExpiration(long expiration) {
    refreshTokenExpiration = expiration;
  }

  @Value("${security.jwt.refresh-token.cookie.secure}")
  public void setRefreshTokenCookieSecure(boolean secure) {
    refreshTokenCookieSecure = secure;
  }

  public static String getUserDevice(HttpServletRequest request) {
    String userAgent = request.getHeader("User-Agent");

    return userAgent;
  }

  public static String getUserIp(HttpServletRequest request) {
    String ipAddress = request.getRemoteAddr();

    String forwardedForHeader = request.getHeader("X-Forwarded-For");

    if (forwardedForHeader != null && !forwardedForHeader.isEmpty()) {
      ipAddress = forwardedForHeader.split(",")[0].trim();
    }

    return ipAddress;
  }

  public static void setRefreshToken(
    String token,
    HttpServletResponse response
  ) {
    Cookie refreshCookie = new Cookie("refresh_token", token);

    System.out.print(
      "the refresh token expiration is " + refreshTokenExpiration
    );

    refreshCookie.setMaxAge((int) (refreshTokenExpiration / 1000));
    refreshCookie.setSecure(refreshTokenCookieSecure);
    refreshCookie.setAttribute("SameSite", "Lax");
    refreshCookie.setHttpOnly(true);
    refreshCookie.setPath("/");

    response.addCookie(refreshCookie);
  }
}
