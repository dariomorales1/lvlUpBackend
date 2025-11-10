package cl.levelup.userservice.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class VersionController {
    @GetMapping("/version")
    public Map<String, String> version() {
        return Map.of("service", "userService", "version", "1.0.0", "status", "OK");
    }
}
