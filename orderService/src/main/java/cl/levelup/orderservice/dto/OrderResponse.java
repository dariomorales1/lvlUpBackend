package cl.levelup.orderservice.dto;

import cl.levelup.orderservice.model.Order;
import cl.levelup.orderservice.model.OrderItem;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
public class OrderResponse {

    private UUID id;
    private String userId;
    private LocalDateTime createdAt;

    private Long totalAmount;
    private Integer discountPercent;
    private Long finalAmount;

    private Long pointsGranted;
    private Long pointsSpent;

    private boolean usedPointsDiscount;
    private boolean usedEmailDiscount;

    private List<OrderItemResponse> items;

    public static OrderResponse fromEntity(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .createdAt(order.getCreatedAt())
                .totalAmount(order.getTotalAmount())
                .discountPercent(order.getDiscountPercent())
                .finalAmount(order.getFinalAmount())
                .pointsGranted(order.getPointsGranted())
                .pointsSpent(order.getPointsSpent())
                .usedPointsDiscount(order.isUsedPointsDiscount())
                .usedEmailDiscount(order.isUsedEmailDiscount())
                .items(order.getItems().stream()
                        .map(OrderResponse::mapItem)
                        .collect(Collectors.toList()))
                .build();
    }

    private static OrderItemResponse mapItem(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .name(item.getName())
                .image(item.getImage())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .build();
    }
}
