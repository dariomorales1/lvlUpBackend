package cl.levelup.authservice.model.dto;

import lombok.Getter;

@Getter
public class AuthResponse {

    private final String accessToken;
    private final String refreshToken;
    private final String tokenType;
    private final String userId;
    private final String email;
    private final String rol;

    public AuthResponse(String accessToken, String refreshToken, String tokenType, String userId, String email, String rol) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.userId = userId;
        this.email = email;
        this.rol = rol;
    }
}
