package cl.levelup.orderservice.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CartResponseDto {
    private UUID id;
    private String userId;
    private String sessionId;
    private List<CartItemDto> items;
    private Long totalAmount;
    private Integer totalItems;
}
