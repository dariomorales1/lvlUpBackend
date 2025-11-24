package cl.levelup.cartservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final WebClient webClient;

    public Mono<Map> getProductById(String productId) {
        return webClient.get()
                .uri("/products/{id}", productId)
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(e -> Mono.error(new RuntimeException("Product not found: " + productId)));
    }

    public Mono<Boolean> isProductAvailable(String productId, Integer quantity) {
        if (productId == null || quantity == null || quantity <= 0) {
            return Mono.just(false);
        }

        return getProductById(productId)
                .map(product -> {
                    if (product.get("stock") == null || product.get("available") == null) {
                        return false;
                    }

                    int stock = ((Number) product.get("stock")).intValue();
                    boolean available = Boolean.TRUE.equals(product.get("available"));

                    return available && stock >= quantity;
                })
                .onErrorReturn(false);
    }
}