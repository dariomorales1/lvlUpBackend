package cl.levelup.authservice.controller;

import cl.levelup.authservice.dto.*;
import cl.levelup.authservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        try {
            UserDTO user = authService.register(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse(ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            UserDTO user = authService.login(req);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse(ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse(ex.getMessage()));
        }
    }

    @GetMapping("/health")
    public MessageResponse health() {
        return new MessageResponse("OK");
    }
}

