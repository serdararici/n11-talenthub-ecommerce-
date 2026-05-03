package com.n11.talenthub.auth.service;

import com.n11.talenthub.auth.config.JwtProperties;
import com.n11.talenthub.auth.dto.AuthResponse;
import com.n11.talenthub.auth.dto.LoginRequest;
import com.n11.talenthub.auth.dto.RegisterRequest;
import com.n11.talenthub.auth.dto.ValidateTokenResponse;
import com.n11.talenthub.auth.entity.RefreshToken;
import com.n11.talenthub.auth.entity.Role;
import com.n11.talenthub.auth.entity.User;
import com.n11.talenthub.auth.exception.TokenException;
import com.n11.talenthub.auth.repository.RefreshTokenRepository;
import com.n11.talenthub.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private JwtProperties jwtProperties;
    @InjectMocks private AuthService authService;

    private User activeUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("hashed-password")
                .firstName("John")
                .lastName("Doe")
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void register_newEmail_returnsTokens() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(activeUser);
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(604800000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(
                RefreshToken.builder().token("refresh-token").user(activeUser)
                        .expiresAt(LocalDateTime.now().plusDays(7)).build());

        AuthResponse response = authService.register(registerRequest);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getRole()).isEqualTo("ROLE_USER");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsIllegalArgument() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email is already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_validCredentials_returnsTokens() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);
        when(jwtService.generateAccessToken(activeUser)).thenReturn("access-token");
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(604800000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(
                RefreshToken.builder().token("refresh-token").user(activeUser)
                        .expiresAt(LocalDateTime.now().plusDays(7)).build());

        AuthResponse response = authService.login(loginRequest);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void login_unknownEmail_throwsBadCredentials() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_wrongPassword_throwsBadCredentials() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_disabledAccount_throwsDisabledException() {
        User disabled = User.builder().id(2L).email("test@example.com")
                .password("hashed-password").firstName("John").lastName("Doe")
                .role(Role.ROLE_USER).enabled(false).build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(disabled));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(DisabledException.class);
    }

    @Test
    void validateToken_validToken_returnsUserInfo() {
        when(jwtService.isTokenValid("valid-token")).thenReturn(true);
        when(jwtService.extractUserId("valid-token")).thenReturn(1L);
        when(jwtService.extractEmail("valid-token")).thenReturn("test@example.com");
        when(jwtService.extractRole("valid-token")).thenReturn("ROLE_USER");

        ValidateTokenResponse response = authService.validateToken("valid-token");

        assertThat(response.isValid()).isTrue();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getRole()).isEqualTo("ROLE_USER");
    }

    @Test
    void validateToken_invalidToken_returnsInvalid() {
        when(jwtService.isTokenValid("bad-token")).thenReturn(false);

        ValidateTokenResponse response = authService.validateToken("bad-token");

        assertThat(response.isValid()).isFalse();
    }

    @Test
    void refresh_validToken_returnsNewTokens() {
        RefreshToken stored = RefreshToken.builder().token("valid-refresh")
                .user(activeUser).expiresAt(LocalDateTime.now().plusDays(1)).build();
        when(refreshTokenRepository.findByToken("valid-refresh")).thenReturn(Optional.of(stored));
        when(jwtService.generateAccessToken(activeUser)).thenReturn("new-access");
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(604800000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(
                RefreshToken.builder().token("new-refresh").user(activeUser)
                        .expiresAt(LocalDateTime.now().plusDays(7)).build());

        AuthResponse response = authService.refresh("valid-refresh");

        assertThat(response.getAccessToken()).isEqualTo("new-access");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh");
    }

    @Test
    void refresh_expiredToken_throwsTokenException() {
        RefreshToken expired = RefreshToken.builder().token("expired-refresh")
                .user(activeUser).expiresAt(LocalDateTime.now().minusDays(1)).build();
        when(refreshTokenRepository.findByToken("expired-refresh")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> authService.refresh("expired-refresh"))
                .isInstanceOf(TokenException.class)
                .hasMessageContaining("expired");

        verify(refreshTokenRepository).delete(expired);
    }

    @Test
    void refresh_unknownToken_throwsTokenException() {
        when(refreshTokenRepository.findByToken("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("unknown"))
                .isInstanceOf(TokenException.class);
    }
}
