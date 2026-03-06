package com.antivirus.server.service;

import com.antivirus.server.model.SessionStatus;
import com.antivirus.server.model.UserSession;
import com.antivirus.server.repository.UserSessionRepository;
import com.antivirus.server.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserSessionRepository userSessionRepository;
    private final UserService userService;

    @Transactional
    public Map<String, String> generateTokenPair(UserDetails userDetails, String deviceId) {
        try {
            String accessToken = jwtTokenUtil.generateAccessToken(userDetails);
            String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);

            Instant accessTokenExpiry = jwtTokenUtil.getAccessTokenExpiryInstant();
            Instant refreshTokenExpiry = jwtTokenUtil.getRefreshTokenExpiryInstant();

            UserSession session = UserSession.builder()
                    .userEmail(userDetails.getUsername())
                    .deviceId(deviceId != null ? deviceId : UUID.randomUUID().toString())
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .accessTokenExpiry(accessTokenExpiry)
                    .refreshTokenExpiry(refreshTokenExpiry)
                    .status(SessionStatus.ACTIVE)
                    .createdAt(Instant.now())
                    .build();

            userSessionRepository.save(session);
            log.info("Created new session for user: {}", userDetails.getUsername());

            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", accessToken);
            tokens.put("refreshToken", refreshToken);
            tokens.put("tokenType", "Bearer");

            return tokens;
        } catch (Exception e) {
            log.error("Error generating token pair: {}", e.getMessage(), e);
            throw new RuntimeException("Token generation failed", e);
        }
    }

    @Transactional
    public Map<String, String> refreshTokens(String refreshToken) {
        if (!jwtTokenUtil.validateRefreshToken(refreshToken)) {
            throw new SecurityException("Invalid refresh token");
        }

        UserSession existingSession = userSessionRepository
                .findByRefreshTokenAndStatus(refreshToken, SessionStatus.ACTIVE)
                .orElseThrow(() -> new SecurityException("Session not found or already used"));

        if (existingSession.getRefreshTokenExpiry().isBefore(Instant.now())) {
            existingSession.setStatus(SessionStatus.REVOKED);
            userSessionRepository.save(existingSession);
            throw new SecurityException("Refresh token expired");
        }

        existingSession.setStatus(SessionStatus.USED);
        existingSession.setUpdatedAt(Instant.now());
        userSessionRepository.save(existingSession);

        String username = jwtTokenUtil.getUsernameFromToken(refreshToken);
        UserDetails userDetails = userService.loadUserByUsername(username);

        String newAccessToken = jwtTokenUtil.generateAccessToken(userDetails);
        String newRefreshToken = jwtTokenUtil.generateRefreshToken(userDetails);

        UserSession newSession = UserSession.builder()
                .userEmail(username)
                .deviceId(existingSession.getDeviceId())
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .accessTokenExpiry(jwtTokenUtil.getAccessTokenExpiryInstant())
                .refreshTokenExpiry(jwtTokenUtil.getRefreshTokenExpiryInstant())
                .status(SessionStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();

        userSessionRepository.save(newSession);
        log.info("Refreshed tokens for user: {}", username);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", newAccessToken);
        tokens.put("refreshToken", newRefreshToken);
        tokens.put("tokenType", "Bearer");

        return tokens;
    }

    @Transactional
    public void revokeAllUserSessions(String userEmail) {
        userSessionRepository.revokeAllActiveSessionsByUserEmail(userEmail);
        log.info("Revoked all sessions for user: {}", userEmail);
    }

    public boolean validateAccessToken(String token) {
        return jwtTokenUtil.validateAccessToken(token);
    }

    public String getUsernameFromToken(String token) {
        return jwtTokenUtil.getUsernameFromToken(token);
    }
}
