// order-service/src/main/java/cl/levelup/orderservice/client/UserClient.java
package cl.levelup.orderservice.client;

import cl.levelup.orderservice.dto.UsuarioDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@RequiredArgsConstructor
public class UserClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.user-service.base-url}")
    private String userServiceBaseUrl;

    public UsuarioDto getUserById(String userId, String authToken) {
        try {
            return webClientBuilder
                    .baseUrl(userServiceBaseUrl)
                    .build()
                    .get()
                    .uri("/users/{id}", userId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                    .retrieve()
                    .bodyToMono(UsuarioDto.class)
                    .block(); // seguimos modelo imperativo como en el resto del MS
        } catch (WebClientResponseException.NotFound e) {
            // Usuario no encontrado
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error llamando a user-service: " + e.getMessage(), e);
        }
    }
}
