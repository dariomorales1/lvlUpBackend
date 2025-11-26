package cl.levelup.cartservice.dto;

import cl.levelup.cartservice.model.Cart;
import cl.levelup.cartservice.model.CartItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Schema(description = "Respuesta con los datos del carrito de compras")
public class CartResponse {

    @Schema(description = "ID único del carrito", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "ID del usuario (null para usuarios anónimos)", example = "user-123")
    private String userId;

    @Schema(description = "Lista de items en el carrito")
    private List<CartItem> items;

    @Schema(description = "Monto total del carrito en centavos", example = "29990")
    private Long totalAmount;

    @Schema(description = "Fecha y hora de última actualización", example = "2024-01-15T10:30:00")
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