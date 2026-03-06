package com.antivirus.server.repository;

import com.antivirus.server.model.SessionStatus;
import com.antivirus.server.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
    Optional<UserSession> findByRefreshTokenAndStatus(String refreshToken, SessionStatus status);
    List<UserSession> findByUserEmailAndStatus(String userEmail, SessionStatus status);

    @Query("SELECT us FROM UserSession us WHERE us.userEmail = :userEmail AND us.status = 'ACTIVE' AND us.refreshTokenExpiry > :now")
    List<UserSession> findActiveSessionsByUserEmail(@Param("userEmail") String userEmail, @Param("now") Instant now);

    @Modifying
    @Query("UPDATE UserSession us SET us.status = 'REVOKED' WHERE us.userEmail = :userEmail AND us.status = 'ACTIVE'")
    void revokeAllActiveSessionsByUserEmail(@Param("userEmail") String userEmail);

    @Modifying
    @Query("DELETE FROM UserSession us WHERE us.refreshTokenExpiry < :now")
    void deleteExpiredSessions(@Param("now") Instant now);
}
