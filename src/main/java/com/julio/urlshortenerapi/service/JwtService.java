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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
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
    return extractClaim(token, Claims::getSubject);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  public String generateAccessToken(User user) {
    Map<String, Object> claims = new HashMap<>();

    claims.put("email", user.getEmail());

    return createToken(claims, user.getEmail(), accessTokenExpiration);
  }

  public String generateRefreshToken(User user, String ip, String device) {
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

    return token;
  }

  public Map<String, String> refresh(String oldRefreshToken, User user) {
    boolean isValid = isRefreshTokenValid(oldRefreshToken);

    if (!isValid) {
      return null;
    }

    RefreshToken oldToken = refreshTokenRepository
      .findById(oldRefreshToken)
      .get();

    refreshTokenRepository.deleteById(oldRefreshToken);

    String accessToken = generateAccessToken(user);
    String refreshToken = generateRefreshToken(
      user,
      oldToken.getIp(),
      oldToken.getDevice()
    );

    Map<String, String> tokenPair = new HashMap<>();

    tokenPair.put("accessToken", accessToken);
    tokenPair.put("refreshToken", refreshToken);

    return tokenPair;
  }

  public boolean revoke(String oldRefreshToken) {
    boolean isValid = isRefreshTokenValid(oldRefreshToken);

    if (!isValid) {
      return false;
    }

    refreshTokenRepository.deleteById(oldRefreshToken);

    return true;
  }

  private String createToken(
    Map<String, Object> claims,
    String subject,
    long expiration
  ) {
    return Jwts.builder()
      .setClaims(claims)
      .setSubject(subject)
      .setIssuedAt(new Date(System.currentTimeMillis()))
      .setExpiration(new Date(System.currentTimeMillis() + expiration))
      .signWith(getSignInKey(), SignatureAlgorithm.HS256)
      .compact();
  }

  public boolean isAccessTokenValid(String token, User user) {
    final String username = extractUsername(token);
    return username.equals(user.getEmail()) && !isTokenExpired(token);
  }

  public boolean isRefreshTokenValid(String refreshJwt) {
    Claims claims = extractAllClaims(refreshJwt);

    if (!refreshTokenRepository.existsById(refreshJwt)) {
      return false;
    }

    if (
      !claims
        .getSubject()
        .equals(refreshTokenRepository.findById(refreshJwt).get().getUsername())
    ) {
      return false;
    }

    if (isTokenExpired(refreshJwt)) {
      return false;
    }

    return true;
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
      .setSigningKey(getSignInKey())
      .build()
      .parseClaimsJws(token)
      .getBody();
  }

  private Key getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
