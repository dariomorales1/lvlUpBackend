package cl.levelup.authservice.repository;

import cl.levelup.authservice.model.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface AuthSessionRepository extends JpaRepository<AuthSession, String> {

    Optional<AuthSession> findByRefreshTokenAndRevokedFalse(String refreshTokenHash);

    Optional<AuthSession> findByRefreshTokenAndRevokedFalseAndExpiresAtAfter(
            String refreshTokenHash,
            OffsetDateTime now
    );
}
