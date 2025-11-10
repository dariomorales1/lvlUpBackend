package cl.levelup.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @Email @NotBlank
    private String email;

    @NotBlank @Size(min = 3, max = 60)
    private String username;

    @NotBlank @Size(min = 6, max = 100)
    private String password;

    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }

    public void setEmail(String email) { this.email = email; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
}

