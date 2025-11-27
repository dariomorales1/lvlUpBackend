package cl.levelup.orderservice.client;

import cl.levelup.orderservice.dto.CartResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CartClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.cart-service.base-url}")
    private String cartServiceBaseUrl;

    public Mono<CartResponseDto> getUserCart(String userId, String authHeader) {
        return webClientBuilder.build()
                .get()
                .uri(cartServiceBaseUrl + "/carts/user/{userId}", userId)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToMono(CartResponseDto.class);
    }

    public Mono<Void> clearUserCart(String userId, String authHeader) {
        return webClientBuilder.build()
                .delete()
                .uri(cartServiceBaseUrl + "/carts/user/{userId}/clear", userId)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
