package com.julio.urlshortenerapi.service;

import com.julio.urlshortenerapi.model.RefreshToken;
import com.julio.urlshortenerapi.model.User;
import com.julio.urlshortenerapi.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JwtService {

  @Value("${security.jwt.secret-key}")
  private String secretKey;

  @Value("${security.jwt.access-token.expiration-time}")
  private long accessTokenExpiration;

  @Value("${security.jwt.refresh-token.expiration-time}")
  private long refreshTokenExpiration;

  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  public String extractUsername(String token) {
    log.debug("Extracting username from token");
    return extractClaim(token, Claims::getSubject);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    log.debug("Extracting claim from token");
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  public String generateAccessToken(User user) {
    log.info("Generating access token for user: {}", user.getEmail());
    Map<String, Object> claims = new HashMap<>();

    claims.put("email", user.getEmail());

    log.debug("Access token generated successfully");
    return createToken(claims, user.getEmail(), accessTokenExpiration);
  }

  public String generateRefreshToken(User user, String ip, String device) {
    log.info(
      "Generating refresh token for user: {}, ip: {}, device: {}",
      user.getEmail(),
      ip,
      device
    );

    Map<String, Object> claims = new HashMap<>();

    claims.put("email", user.getEmail());

    String token = createToken(claims, user.getEmail(), refreshTokenExpiration);

    RefreshToken refreshToken = RefreshToken.builder()
      .id(token)
      .username(user.getEmail())
      .ip(ip)
      .device(device)
      .ttl(refreshTokenExpiration / 1000)
      .build();

    refreshTokenRepository.save(refreshToken);

    log.info("Refresh token saved to repository for user: {}", user.getEmail());
    return token;
  }

  public Map<String, String> refresh(String oldRefreshToken, User user) {
    log.info(
      "Refreshing tokens for user: {}, old token: {}",
      user.getEmail(),
      oldRefreshToken.substring(0, 10) + "..."
    );

    boolean isValid = isRefreshTokenValid(oldRefreshToken);

    if (!isValid) {
      return null;
    }

    RefreshToken oldToken = refreshTokenRepository
      .findById(oldRefreshToken)
      .get();

    log.debug("Retrieved old refresh token from repository");

    refreshTokenRepository.deleteById(oldRefreshToken);

    log.info("Deleted old refresh token from repository");

    String accessToken = generateAccessToken(user);
    String refreshToken = generateRefreshToken(
      user,
      oldToken.getIp(),
      oldToken.getDevice()
    );

    Map<String, String> tokenPair = new HashMap<>();

    tokenPair.put("accessToken", accessToken);
    tokenPair.put("refreshToken", refreshToken);

    log.info(
      "Token refresh completed successfully for user: {}",
      user.getEmail()
    );

    return tokenPair;
  }

  public boolean revoke(String oldRefreshToken) {
    log.info(
      "Revoking refresh token: {}",
      oldRefreshToken.substring(0, 10) + "..."
    );

    boolean isValid = isRefreshTokenValid(oldRefreshToken);

    if (!isValid) {
      return false;
    }

    refreshTokenRepository.deleteById(oldRefreshToken);
    log.info("Refresh token revoked successfully");

    return true;
  }

  private String createToken(
    Map<String, Object> claims,
    String subject,
    long expiration
  ) {
    log.debug("Creating JWT token with expiration: {} ms", expiration);
    return Jwts.builder()
      .setClaims(claims)
      .setSubject(subject)
      .setIssuedAt(new Date(System.currentTimeMillis()))
      .setExpiration(new Date(System.currentTimeMillis() + expiration))
      .signWith(getSignInKey(), SignatureAlgorithm.HS256)
      .compact();
  }

  public boolean isAccessTokenValid(String token, User user) {
    log.debug("Validating access token for user: {}", user.getEmail());
    final String username = extractUsername(token);
    boolean isValid =
      username.equals(user.getEmail()) && !isTokenExpired(token);
    log.debug("Access token validation result: {}", isValid);
    return isValid;
  }

  public boolean isRefreshTokenValid(String refreshJwt) {
    log.debug("Validating refresh token");
    Claims claims = extractAllClaims(refreshJwt);

    if (!refreshTokenRepository.existsById(refreshJwt)) {
      log.warn("Refresh token does not exist in repository");
      return false;
    }

    RefreshToken storedToken = refreshTokenRepository
      .findById(refreshJwt)
      .get();

    String storedTokenSubject = extractUsername(storedToken.getId());

    if (!claims.getSubject().equals(storedTokenSubject)) {
      log.warn("Refresh token subject mismatch");
      return false;
    }

    if (isTokenExpired(refreshJwt)) {
      log.warn("Refresh token is expired");
      return false;
    }

    log.debug("Refresh token is valid");
    return true;
  }

  private boolean isTokenExpired(String token) {
    boolean expired = extractExpiration(token).before(new Date());
    log.debug("Token expired check: {}", expired);
    return expired;
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private Claims extractAllClaims(String token) {
    log.debug("Extracting all claims from token");
    return Jwts.parserBuilder()
      .setSigningKey(getSignInKey())
      .build()
      .parseClaimsJws(token)
      .getBody();
  }

  private Key getSignInKey() {
    log.debug("Generating signing key");
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
