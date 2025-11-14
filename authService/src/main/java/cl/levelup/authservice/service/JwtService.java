package cl.levelup.authservice.service;

import cl.levelup.authservice.config.JwtProperties;
import cl.levelup.authservice.model.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtProperties props;
    private final Algorithm algorithm;

    public JwtService(JwtProperties props) {
        this.props = props;
        this.algorithm = Algorithm.HMAC256(props.getSecret());
    }

    public String generateAccessToken(Usuario user) {
        Instant now = Instant.now();
        Instant exp = now.plus(props.getAccessTokenExpirationMinutes(), ChronoUnit.MINUTES);

        return JWT.create()
                .withSubject(user.getId())
                .withClaim("email", user.getEmail())
                .withClaim("rol", user.getRol())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(exp))
                .withJWTId(UUID.randomUUID().toString())
                .sign(algorithm);
    }

    public String generateRefreshToken(Usuario user) {
        Instant now = Instant.now();
        Instant exp = now.plus(props.getRefreshTokenExpirationDays(), ChronoUnit.DAYS);

        return JWT.create()
                .withSubject(user.getId())
                .withClaim("type", "refresh")
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(exp))
                .withJWTId(UUID.randomUUID().toString())
                .sign(algorithm);
    }

    public String getUserIdFromToken(String token) {
        return JWT.require(algorithm)
                .build()
                .verify(token)
                .getSubject();
    }

    public String extractEmailFromRefreshToken(String refreshToken) {
        try {
            DecodedJWT decodedJWT = JWT.require(algorithm)
                    .build()
                    .verify(refreshToken);
            return decodedJWT.getClaim("email").asString();
        } catch (JWTVerificationException e) {
            throw new RuntimeException("Refresh token inválido: " + e.getMessage());
        }
    }

    public boolean validateRefreshToken(String refreshToken) {
        try {
            DecodedJWT decodedJWT = JWT.require(algorithm)
                    .build()
                    .verify(refreshToken);

            String tokenType = decodedJWT.getClaim("type").asString();
            return "refresh".equals(tokenType);

        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public String refreshAccessToken(String refreshToken) {
        try {
            DecodedJWT decodedJWT = JWT.require(algorithm)
                    .build()
                    .verify(refreshToken);

            String tokenType = decodedJWT.getClaim("type").asString();
            if (!"refresh".equals(tokenType)) {
                throw new RuntimeException("Token no es un refresh token válido");
            }

            String userId = decodedJWT.getSubject();
            String email = decodedJWT.getClaim("email").asString();
            String rol = decodedJWT.getClaim("rol").asString();

            Usuario tempUser = new Usuario();
            tempUser.setId(userId);
            tempUser.setEmail(email);
            tempUser.setRol(rol);

            return generateAccessToken(tempUser);

        } catch (JWTVerificationException e) {
            throw new RuntimeException("Refresh token inválido: " + e.getMessage());
        }
    }

    public String getEmailFromToken(String token) {
        try {
            DecodedJWT decodedJWT = JWT.require(algorithm)
                    .build()
                    .verify(token);
            return decodedJWT.getClaim("email").asString();
        } catch (JWTVerificationException e) {
            throw new RuntimeException("Token inválido: " + e.getMessage());
        }
    }

    public String getRolFromToken(String token) {
        try {
            DecodedJWT decodedJWT = JWT.require(algorithm)
                    .build()
                    .verify(token);
            return decodedJWT.getClaim("rol").asString();
        } catch (JWTVerificationException e) {
            throw new RuntimeException("Token inválido: " + e.getMessage());
        }
    }
}