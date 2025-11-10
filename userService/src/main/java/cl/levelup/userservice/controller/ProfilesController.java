package cl.levelup.userservice.controller;

import cl.levelup.userservice.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfilesController {

    private final ProfileService service;

    public ProfilesController(ProfileService service) {
        this.service = service;
    }

    @GetMapping("/perfiles/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(401).body("JWT invalido o no existente");
        }
        String sub = jwt.getSubject();
        String token = jwt.getTokenValue();

        return service.getMyProfile(sub, token)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
