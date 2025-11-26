// order-service/src/main/java/cl/levelup/orderservice/dto/CartItemDto.java
package cl.levelup.orderservice.dto;

import lombok.Data;

@Data
public class CartItemDto {
    private String productId;
    private String productName;
    private Long unitPrice;   // mismo formato que en Cart (centavos normalmente)
    private Integer quantity;
    private String imagenUrl;

    public Long getSubtotal() {
        return unitPrice * quantity;
    }
}
