package cl.levelup.orderservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class OrderItemResponse {
    private UUID id;
    private String productId;
    private String name;
    private String image;
    private Long price;
    private Integer quantity;
}
