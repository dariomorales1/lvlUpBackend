package cl.levelup.orderservice.service;

import cl.levelup.orderservice.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrderFromCart(String userId, boolean usePointsDiscount, String authHeader);

    List<OrderResponse> getOrdersByUser(String userId);

    Long getCurrentPoints(String userId);

    boolean isUserInTop5ByPoints(String userId);
}
