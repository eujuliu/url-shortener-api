package com.julio.urlshortenerapi;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.julio.urlshortenerapi.dto.UserRequestDTO;
import com.julio.urlshortenerapi.model.OAuthProvider;
import com.julio.urlshortenerapi.model.User;
import com.julio.urlshortenerapi.repository.OAuthProviderRepository;
import com.julio.urlshortenerapi.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(
  properties = {
    "spring.security.oauth2.client.registration.google.client-id=dummy-google-id",
    "spring.security.oauth2.client.registration.google.client-secret=dummy-google-secret",
    "spring.security.oauth2.client.registration.github.client-id=dummy-github-id",
    "spring.security.oauth2.client.registration.github.client-secret=dummy-github-secret",
    // Exclude auto-configurations
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration," +
      "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
      "org.springframework.boot.autoconfigure.webflux.WebFluxAutoConfiguration",
  }
)
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserRepository userRepository;

  @MockitoBean
  private OAuthProviderRepository oAuthProviderRepository;

  @MockitoBean
  private PasswordEncoder passwordEncoder;

  @Test
  void register_WithValidData_ShouldReturn200() throws Exception {
    UserRequestDTO request = new UserRequestDTO(
      "ValidName",
      "test@example.com",
      "Password!123"
    );

    User mockUser = User.builder()
      .userId(UUID.randomUUID())
      .name("ValidName")
      .email("test@example.com")
      .password("encodedPass")
      .createdAt(LocalDateTime.now())
      .updatedAt(LocalDateTime.now())
      .build();

    OAuthProvider mockProvider = OAuthProvider.builder()
      .userId(mockUser.getUserId())
      .email(mockUser.getEmail())
      .provider("password")
      .emailVerified(false)
      .build();

    when(userRepository.findByEmail("test@example.com")).thenReturn(
      null,
      mockUser
    );
    when(passwordEncoder.encode("Password!123")).thenReturn("encodedPass");
    when(userRepository.save(any(User.class))).thenReturn(mockUser);
    when(oAuthProviderRepository.save(any(OAuthProvider.class))).thenReturn(
      mockProvider
    );

    mockMvc
      .perform(
        post("/api/v1/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request))
          .with(csrf())
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.email").value("test@example.com"))
      .andExpect(jsonPath("$.name").value("ValidName"))
      .andDo(result -> {
        HttpSession session = result.getRequest().getSession();

        assertNotNull(session.getAttribute("user_id"));
        assertEquals("test@example.com", session.getAttribute("user_email"));
        assertEquals("ValidName", session.getAttribute("user_name"));
        assertEquals("password", session.getAttribute("login_provider"));
      });
  }

  @Test
  void register_WithDuplicateEmail_ShouldReturn409() throws Exception {
    UserRequestDTO request = new UserRequestDTO(
      "AnotherName",
      "duplicate@example.com",
      "AnotherPass123!"
    );
    User existingUser = User.builder().email("duplicate@example.com").build();

    when(userRepository.findByEmail("duplicate@example.com")).thenReturn(
      existingUser
    );

    mockMvc
      .perform(
        post("/api/v1/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request))
          .with(csrf())
      )
      .andExpect(status().isConflict());
  }

  @Test
  void register_WithInvalidEmail_ShouldReturn400() throws Exception {
    UserRequestDTO request = new UserRequestDTO(
      "ValidName",
      "invalidemail",
      "ValidPass123!"
    );

    mockMvc
      .perform(
        post("/api/v1/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request))
          .with(csrf())
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value(400))
      .andExpect(jsonPath("$.name").value("ValidationError"))
      .andExpect(
        jsonPath("$.fields[0]").value(
          "email - must be a well-formed email address"
        )
      );
  }

  @Test
  void register_WithPasswordTooShort_ShouldReturn400() throws Exception {
    UserRequestDTO request = new UserRequestDTO(
      "ValidName",
      "test@example.com",
      "Short1!"
    );

    mockMvc
      .perform(
        post("/api/v1/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request))
          .with(csrf())
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value(400))
      .andExpect(jsonPath("$.name").value("ValidationError"))
      .andExpect(
        jsonPath("$.fields[0]").value(
          "password - size must be between 8 and 72"
        )
      );
  }

  @Test
  void register_WithPasswordMissingUppercase_ShouldReturn400()
    throws Exception {
    UserRequestDTO request = new UserRequestDTO(
      "ValidName",
      "test@example.com",
      "validpass123!"
    );

    mockMvc
      .perform(
        post("/api/v1/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request))
          .with(csrf())
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value(400))
      .andExpect(jsonPath("$.name").value("ValidationError"))
      .andExpect(
        jsonPath("$.fields[0]").value(
          "password - Password don't fill the requirements"
        )
      );
  }

  @Test
  void register_WithPasswordMissingLowercase_ShouldReturn400()
    throws Exception {
    UserRequestDTO request = new UserRequestDTO(
      "ValidName",
      "test@example.com",
      "VALIDPASS123!"
    );

    mockMvc
      .perform(
        post("/api/v1/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request))
          .with(csrf())
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value(400))
      .andExpect(jsonPath("$.name").value("ValidationError"))
      .andExpect(
        jsonPath("$.fields[0]").value(
          "password - Password don't fill the requirements"
        )
      );
  }

  @Test
  void register_WithPasswordMissingDigit_ShouldReturn400() throws Exception {
    UserRequestDTO request = new UserRequestDTO(
      "ValidName",
      "test@example.com",
      "ValidPass!"
    );

    mockMvc
      .perform(
        post("/api/v1/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request))
          .with(csrf())
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value(400))
      .andExpect(jsonPath("$.name").value("ValidationError"))
      .andExpect(
        jsonPath("$.fields[0]").value(
          "password - Password don't fill the requirements"
        )
      );
  }

  @Test
  void register_WithPasswordMissingSpecialChar_ShouldReturn400()
    throws Exception {
    UserRequestDTO request = new UserRequestDTO(
      "ValidName",
      "test@example.com",
      "ValidPass123"
    );

    mockMvc
      .perform(
        post("/api/v1/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request))
          .with(csrf())
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value(400))
      .andExpect(jsonPath("$.name").value("ValidationError"))
      .andExpect(
        jsonPath("$.fields[0]").value(
          "password - Password don't fill the requirements"
        )
      );
  }

  @Test
  void register_WithPasswordTooLong_ShouldReturn400() throws Exception {
    String longPassword = "A1!" + "a".repeat(70);
    UserRequestDTO request = new UserRequestDTO(
      "ValidName",
      "test@example.com",
      longPassword
    );

    mockMvc
      .perform(
        post("/api/v1/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request))
          .with(csrf())
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value(400))
      .andExpect(jsonPath("$.name").value("ValidationError"))
      .andExpect(
        jsonPath("$.fields[0]").value(
          "password - size must be between 8 and 72"
        )
      );
  }

  @Test
  void login_WithValidCredentials_ShouldReturn200() throws Exception {
    UserRequestDTO request = new UserRequestDTO(
      null,
      "test@example.com",
      "Password!123"
    );

    User existingUser = User.builder()
      .userId(UUID.randomUUID())
      .name("ValidName")
      .email("test@example.com")
      .password("encodedPass")
      .createdAt(LocalDateTime.now())
      .updatedAt(LocalDateTime.now())
      .build();

    when(userRepository.findByEmail("test@example.com")).thenReturn(
      existingUser
    );
    when(passwordEncoder.matches("Password!123", "encodedPass")).thenReturn(
      true
    );

    mockMvc
      .perform(
        post("/api/v1/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request))
          .with(csrf())
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.email").value("test@example.com"))
      .andExpect(jsonPath("$.name").value("ValidName"))
      .andDo(result -> {
        HttpSession session = result.getRequest().getSession();

        assertNotNull(session.getAttribute("user_id"));
        assertEquals("test@example.com", session.getAttribute("user_email"));
        assertEquals("ValidName", session.getAttribute("user_name"));
        assertEquals("password", session.getAttribute("login_provider"));
      });
  }

  @Test
  void login_WithInvalidEmail_ShouldReturn404() throws Exception {
    UserRequestDTO request = new UserRequestDTO(
      null,
      "nonexistent@example.com",
      "Password!123"
    );

    when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(
      null
    );

    mockMvc
      .perform(
        post("/api/v1/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request))
          .with(csrf())
      )
      .andExpect(status().isNotFound());
  }

  @Test
  void login_WithWrongPassword_ShouldReturn401() throws Exception {
    UserRequestDTO request = new UserRequestDTO(
      null,
      "test@example.com",
      "WrongPassword!123"
    );

    User existingUser = User.builder()
      .userId(UUID.randomUUID())
      .name("ValidName")
      .email("test@example.com")
      .password("encodedPass")
      .createdAt(LocalDateTime.now())
      .updatedAt(LocalDateTime.now())
      .build();

    when(userRepository.findByEmail("test@example.com")).thenReturn(
      existingUser
    );
    when(
      passwordEncoder.matches("WrongPassword!123", "encodedPass")
    ).thenReturn(false);

    mockMvc
      .perform(
        post("/api/v1/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(request))
          .with(csrf())
      )
      .andExpect(status().isUnauthorized());
  }

  @Test
  void getCurrentUser_WithOAuth2_ShouldReturn200() throws Exception {
    User oauthUser = User.builder()
      .userId(UUID.randomUUID())
      .name("OAuthUser")
      .email("oauth@example.com")
      .createdAt(LocalDateTime.now())
      .updatedAt(LocalDateTime.now())
      .build();

    when(userRepository.findByEmail("oauth@example.com")).thenReturn(oauthUser);

    mockMvc
      .perform(
        get("/api/v1/me").with(
          oauth2Login().attributes(attrs ->
            attrs.put("email", "oauth@example.com")
          )
        )
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.email").value("oauth@example.com"))
      .andExpect(jsonPath("$.name").value("OAuthUser"));
  }
}
