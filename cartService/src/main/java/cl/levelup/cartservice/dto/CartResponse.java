package cl.levelup.cartservice.dto;

import cl.levelup.cartservice.model.Cart;
import cl.levelup.cartservice.model.CartItem;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class CartResponse {
    private UUID id;
    private String userId;
    private List<CartItem> items;
    private Long totalAmount;
    private LocalDateTime updatedAt;

    public static CartResponse fromEntity(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setUserId(cart.getUserId());
        response.setItems(cart.getItems());
        response.setTotalAmount(cart.getTotalAmount());
        response.setUpdatedAt(cart.getUpdatedAt());
        return response;
    }
}
