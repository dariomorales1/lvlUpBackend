// order-service/src/main/java/cl/levelup/orderservice/service/OrderService.java
package cl.levelup.orderservice.service;

import cl.levelup.orderservice.dto.OrderResponse;
import cl.levelup.orderservice.dto.PointsResponse;
import cl.levelup.orderservice.dto.TopBuyerResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    OrderResponse createOrderFromUserCart(String userId, String authToken);

    OrderResponse getOrderById(UUID id);

    List<OrderResponse> getOrdersByUser(String userId);

    PointsResponse getUserPoints(String userId);

    List<TopBuyerResponse> getTopBuyers(int limit);
}
