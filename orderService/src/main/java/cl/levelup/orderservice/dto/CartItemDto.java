package cl.levelup.orderservice.dto;

import lombok.Data;

@Data
public class CartItemDto {
    private String productId;
    private String productName;
    private Long unitPrice;
    private Integer quantity;
    private String imagenUrl;
}
