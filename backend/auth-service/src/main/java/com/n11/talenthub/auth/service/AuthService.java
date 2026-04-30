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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.ROLE_USER)
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = createRefreshToken(user);

        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        if (!user.isEnabled()) {
            throw new DisabledException("Account is disabled");
        }

        refreshTokenRepository.deleteByUser(user);

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = createRefreshToken(user);

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }

    @Transactional(readOnly = true)
    public ValidateTokenResponse validateToken(String token) {
        if (!jwtService.isTokenValid(token)) {
            return ValidateTokenResponse.builder().valid(false).build();
        }

        return ValidateTokenResponse.builder()
                .valid(true)
                .userId(jwtService.extractUserId(token))
                .email(jwtService.extractEmail(token))
                .role(jwtService.extractRole(token))
                .build();
    }

    public AuthResponse refresh(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new TokenException("Invalid refresh token"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenException("Refresh token has expired, please log in again");
        }

        User user = refreshToken.getUser();
        refreshTokenRepository.delete(refreshToken);

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken newRefreshToken = createRefreshToken(user);

        return buildAuthResponse(user, accessToken, newRefreshToken.getToken());
    }

    private RefreshToken createRefreshToken(User user) {
        long expirationSeconds = jwtProperties.getRefreshTokenExpiration() / 1000;
        RefreshToken token = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(expirationSeconds))
                .build();
        return refreshTokenRepository.save(token);
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .build();
    }
}
