package cl.levelup.authservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.core.ParameterizedTypeReference;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final WebClient webClient;
    private final String supabaseUrl;
    private final String anonKey;

    public AuthController(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.anon_key}") String anonKey
    ) {
        System.out.println("=== AUTH CONTROLLER CONSTRUCTOR ===");
        System.out.println("Supabase URL from @Value: " + supabaseUrl);
        System.out.println("Anon Key from @Value: " + anonKey);

        if (supabaseUrl == null || supabaseUrl.isEmpty()) {
            System.out.println("ERROR: supabase.url is null or empty!");
        }
        if (anonKey == null || anonKey.isEmpty()) {
            System.out.println("ERROR: supabase.anon_key is null or empty!");
        }

        this.supabaseUrl = supabaseUrl;
        this.anonKey = anonKey;

        this.webClient = WebClient.builder()
                .baseUrl(supabaseUrl + "/auth/v1")
                .defaultHeader("apikey", anonKey)
                .defaultHeader("Content-Type", "application/json")
                .build();

        System.out.println("=== AUTH CONTROLLER INITIALIZED ===");
        System.out.println("WebClient baseUrl: " + supabaseUrl + "/auth/v1");
    }

    // LOGIN
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        System.out.println("=== LOGIN REQUEST RECEIVED ===");
        System.out.println("Email: " + email);
        System.out.println("Supabase URL: " + this.supabaseUrl);
        System.out.println("Anon Key (first 20 chars): " + (this.anonKey != null ? this.anonKey.substring(0, Math.min(20, this.anonKey.length())) : "NULL"));

        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/token")
                        .queryParam("grant_type", "password")
                        .build())
                .bodyValue(Map.of("email", email, "password", password))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    System.out.println("=== SUPABASE ERROR RESPONSE ===");
                    System.out.println("HTTP Status: " + response.statusCode());
                    System.out.println("Response Headers: " + response.headers().asHttpHeaders());

                    return response.bodyToMono(String.class)
                            .flatMap(errorBody -> {
                                System.out.println("Error Body: " + errorBody);
                                System.out.println("Full URL: " + this.supabaseUrl + "/auth/v1/token?grant_type=password");
                                return Mono.error(new RuntimeException("Login Error (" + response.statusCode() + "): " + errorBody));
                            });
                })
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnSuccess(result -> {
                    System.out.println("=== LOGIN SUCCESS ===");
                    System.out.println("Response: " + result);
                })
                .doOnError(error -> {
                    System.out.println("=== LOGIN PROCESS ERROR ===");
                    System.out.println("Error: " + error.getMessage());
                    error.printStackTrace();
                });
    }

    // REGISTRO
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> register(@RequestBody Map<String, String> userData) {
        String email = userData.get("email");
        String password = userData.get("password");

        System.out.println("=== REGISTER REQUEST ===");
        System.out.println("Email: " + email);
        System.out.println("Supabase URL: " + this.supabaseUrl);

        return webClient.post()
                .uri("/signup")
                .bodyValue(Map.of("email", email, "password", password))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    System.out.println("=== SUPABASE REGISTER ERROR ===");
                    System.out.println("HTTP Status: " + response.statusCode());

                    return response.bodyToMono(String.class)
                            .flatMap(errorBody -> {
                                System.out.println("Error Body: " + errorBody);
                                return Mono.error(new RuntimeException("Register Error (" + response.statusCode() + "): " + errorBody));
                            });
                })
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnSuccess(result -> {
                    System.out.println("=== REGISTER SUCCESS ===");
                    System.out.println("Response: " + result);
                })
                .doOnError(error -> {
                    System.out.println("=== REGISTER PROCESS ERROR ===");
                    System.out.println("Error: " + error.getMessage());
                });
    }
}