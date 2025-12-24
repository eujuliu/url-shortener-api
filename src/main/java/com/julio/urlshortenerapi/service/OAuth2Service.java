package com.julio.urlshortenerapi.service;

import com.julio.urlshortenerapi.model.OAuthProvider;
import com.julio.urlshortenerapi.model.User;
import com.julio.urlshortenerapi.repository.OAuthProviderRepository;
import com.julio.urlshortenerapi.repository.UserRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OAuth2Service extends DefaultOAuth2UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private OAuthProviderRepository oAuthProviderRepository;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest request)
    throws OAuth2AuthenticationException {
    OAuth2User oAuthUser = super.loadUser(request);

    String registrationId = request.getClientRegistration().getRegistrationId();

    Map<String, Object> attributes = oAuthUser.getAttributes();
    String name;
    String email;
    Boolean emailVerified;

    switch (registrationId) {
      case "google":
        name = (String) attributes.get("name");
        email = (String) attributes.get("email");
        emailVerified = (Boolean) attributes.get("email_verified");
        break;
      case "github":
        name = (String) attributes.get("name");

        if (name == null) {
          name = (String) attributes.get("login");
        }

        email = (String) attributes.get("email");
        if (email == null) {
          email = this.fetchGitHubEmail(request);
        }

        emailVerified = true;

        break;
      default:
        throw new OAuth2AuthenticationException(
          "Unsupported OAuth2 provider: " + registrationId
        );
    }

    User user = this.userRepository.findByEmail(email);

    if (user == null) {
      user = User.builder().name(name).email(email).build();

      this.userRepository.save(user);
    }

    OAuthProvider oAuthProvider =
      this.oAuthProviderRepository.findByUserIdAndProvider(
        user.getUserId(),
        registrationId
      );

    if (oAuthProvider == null) {
      oAuthProvider = OAuthProvider.builder()
        .userId(user.getUserId())
        .provider(registrationId)
        .email(email)
        .emailVerified(emailVerified != null ? emailVerified : false)
        .build();

      this.oAuthProviderRepository.save(oAuthProvider);
    }

    Map<String, Object> customAttributes = new HashMap<>(attributes);

    customAttributes.put("user_id", user.getUserId().toString());
    customAttributes.put("email", email);
    customAttributes.put("name", name);
    customAttributes.put("provider", registrationId);

    // return new DefaultOAuth2User(
    //   oAuthUser.getAuthorities(),
    //   customAttributes,
    //   "email"
    // );
    return oAuthUser;
  }

  private String fetchGitHubEmail(OAuth2UserRequest request) {
    String emailApiUrl = "https://api.github.com/user/emails";
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();

    headers.setBearerAuth(request.getAccessToken().getTokenValue());

    HttpEntity<String> entity = new HttpEntity<>("", headers);

    try {
      ResponseEntity<List<Map<String, Object>>> response =
        restTemplate.exchange(
          emailApiUrl,
          HttpMethod.GET,
          entity,
          new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

      List<Map<String, Object>> emails = response.getBody();

      if (emails != null) {
        return emails
          .stream()
          .filter(e -> (Boolean) e.get("primary"))
          .map(e -> (String) e.get("email"))
          .findFirst()
          .orElse(null);
      }
    } catch (Exception e) {
      throw new OAuth2AuthenticationException(
        "Error getting Github primary email"
      );
    }
    return null;
  }
}
