package cl.levelup.authservice.model;

import jakarta.validation.constraints.NotBlank;

public class AuthRequest {

    @NotBlank
    private String firebaseIdToken;

    public String getFirebaseIdToken() {
        return firebaseIdToken;
    }

    public void setFirebaseIdToken(String firebaseIdToken) {
        this.firebaseIdToken = firebaseIdToken;
    }
}
