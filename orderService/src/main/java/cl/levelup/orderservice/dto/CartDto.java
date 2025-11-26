// order-service/src/main/java/cl/levelup/orderservice/dto/CartDto.java
package cl.levelup.orderservice.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CartDto {
    private String userId;
    private String sessionId;
    private List<CartItemDto> items = new ArrayList<>();

    public Long getTotalAmount() {
        if (items == null || items.isEmpty()) return 0L;
        return items.stream()
                .mapToLong(CartItemDto::getSubtotal)
                .sum();
    }
}
