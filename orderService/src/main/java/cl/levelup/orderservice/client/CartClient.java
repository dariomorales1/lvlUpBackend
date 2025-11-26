// order-service/src/main/java/cl/levelup/orderservice/client/CartClient.java
package cl.levelup.orderservice.client;

import cl.levelup.orderservice.dto.CartDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@RequiredArgsConstructor
public class CartClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.cart-service.base-url}")
    private String cartServiceBaseUrl;

    public CartDto getUserCart(String userId, String authToken) {
        try {
            return webClientBuilder
                    .baseUrl(cartServiceBaseUrl)
                    .build()
                    .get()
                    .uri("/carts/user/{userId}", userId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                    .retrieve()
                    .bodyToMono(CartDto.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            // carrito no encontrado
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error llamando a cart-service: " + e.getMessage(), e);
        }
    }

    public void clearUserCart(String userId, String authToken) {
        try {
            webClientBuilder
                    .baseUrl(cartServiceBaseUrl)
                    .build()
                    .delete()
                    .uri("/carts/user/{userId}/clear", userId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            // Si no hay carrito, lo ignoramos
        } catch (Exception e) {
            throw new RuntimeException("Error vaciando carrito en cart-service: " + e.getMessage(), e);
        }
    }
}
