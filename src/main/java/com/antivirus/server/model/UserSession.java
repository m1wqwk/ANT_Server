package com.antivirus.server.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "access_token", length = 512)
    private String accessToken;

    @Column(name = "refresh_token", length = 512, unique = true)
    private String refreshToken;

    @Column(name = "access_token_expiry")
    private Instant accessTokenExpiry;

    @Column(name = "refresh_token_expiry")
    private Instant refreshTokenExpiry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;
}