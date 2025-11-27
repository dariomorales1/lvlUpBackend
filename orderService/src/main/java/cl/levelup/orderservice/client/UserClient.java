package cl.levelup.orderservice.client;

import cl.levelup.orderservice.dto.UserSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.user-service.base-url}")
    private String userServiceBaseUrl;

    public Mono<UserSummaryDto> getUserById(String userId, String authHeader) {
        return webClientBuilder.build()
                .get()
                .uri(userServiceBaseUrl + "/users/{userId}", userId)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToMono(UserSummaryDto.class);
    }
}
