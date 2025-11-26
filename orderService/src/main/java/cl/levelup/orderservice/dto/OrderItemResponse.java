// order-service/src/main/java/cl/levelup/orderservice/dto/OrderItemResponse.java
package cl.levelup.orderservice.dto;

import lombok.Data;

@Data
public class OrderItemResponse {
    private String productId;
    private String productName;
    private Long unitPrice;
    private Integer quantity;
    private String imagenUrl;
    private Long subtotal;
}
