package cl.levelup.cartservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {

    private final WebClient webClient;

    public Mono<Boolean> userExists(String userId, String authToken) {
        return webClient.get()
                .uri("/users/{id}", userId)
                .header("Authorization", "Bearer " + authToken)
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getStatusCode().is2xxSuccessful())
                .onErrorReturn(false);
    }
}