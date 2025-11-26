// order-service/src/main/java/cl/levelup/orderservice/dto/OrderResponse.java
package cl.levelup.orderservice.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class OrderResponse {
    private UUID id;
    private String userId;
    private Long totalAmount;
    private Double discountPercent;
    private Long finalAmount;
    private Long pointsGranted;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
}
