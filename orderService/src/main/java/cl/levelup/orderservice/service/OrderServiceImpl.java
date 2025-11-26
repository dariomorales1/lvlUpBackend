// order-service/src/main/java/cl/levelup/orderservice/service/OrderServiceImpl.java
package cl.levelup.orderservice.service;

import cl.levelup.orderservice.client.CartClient;
import cl.levelup.orderservice.client.UserClient;
import cl.levelup.orderservice.dto.*;
import cl.levelup.orderservice.model.Order;
import cl.levelup.orderservice.model.OrderItem;
import cl.levelup.orderservice.repository.OrderRepository;
import cl.levelup.orderservice.repository.projection.UserTotalProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartClient cartClient;
    private final UserClient userClient;

    private static final double TOP_BUYER_DISCOUNT = 0.15;
    private static final double DUOC_DISCOUNT = 0.20;
    private static final int TOP_BUYER_LIMIT = 5;

    @Override
    public OrderResponse createOrderFromUserCart(String userId, String authToken) {

        // 1) Obtener carrito del usuario
        CartDto cart = cartClient.getUserCart(userId, authToken);
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        // 2) Calcular total del carrito
        long totalAmount = cart.getTotalAmount(); // puede ser en centavos; tú decides la convención

        // 3) Obtener usuario para revisar email
        UsuarioDto usuario = userClient.getUserById(userId, authToken);

        // 4) Calcular descuentos
        double discountPercent = 0.0;

        if (isTopBuyer(userId)) {
            discountPercent += TOP_BUYER_DISCOUNT;
        }

        if (usuario != null && usuario.getEmail() != null &&
                usuario.getEmail().toLowerCase().endsWith("@duocuc.cl")) {
            discountPercent += DUOC_DISCOUNT;
        }

        // límite máximo 35%
        if (discountPercent > 0.35) {
            discountPercent = 0.35;
        }

        long finalAmount = Math.round(totalAmount * (1.0 - discountPercent));
        if (finalAmount < 0) {
            finalAmount = 0;
        }

        // 5) Puntos = monto en pesos (aquí uso directamente totalAmount)
        long pointsGranted = totalAmount;

        // 6) Mapear a entidad Order + OrderItems
        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setDiscountPercent(discountPercent);
        order.setFinalAmount(finalAmount);
        order.setPointsGranted(pointsGranted);

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(ci -> {
                    OrderItem oi = new OrderItem();
                    oi.setOrder(order);
                    oi.setProductId(ci.getProductId());
                    oi.setProductName(ci.getProductName());
                    oi.setUnitPrice(ci.getUnitPrice());
                    oi.setQuantity(ci.getQuantity());
                    oi.setImagenUrl(ci.getImagenUrl());
                    oi.setSubtotal(ci.getSubtotal());
                    return oi;
                })
                .collect(Collectors.toList());

        order.setItems(orderItems);

        // 7) Guardar orden
        Order saved = orderRepository.save(order);

        // 8) Vaciar carrito
        cartClient.clearUserCart(userId, authToken);

        // 9) Devolver DTO
        return toResponse(saved);
    }

    private boolean isTopBuyer(String userId) {
        List<UserTotalProjection> topUsers =
                orderRepository.findTopUsers(PageRequest.of(0, TOP_BUYER_LIMIT));

        return topUsers.stream()
                .anyMatch(u -> userId.equals(u.getUserId()));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));
        return toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUser(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PointsResponse getUserPoints(String userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        long totalPoints = orders.stream()
                .mapToLong(Order::getPointsGranted)
                .sum();
        return new PointsResponse(userId, totalPoints);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopBuyerResponse> getTopBuyers(int limit) {
        int realLimit = (limit <= 0 || limit > TOP_BUYER_LIMIT) ? TOP_BUYER_LIMIT : limit;

        return orderRepository.findTopUsers(PageRequest.of(0, realLimit))
                .stream()
                .map(p -> new TopBuyerResponse(p.getUserId(), p.getTotalSpent()))
                .collect(Collectors.toList());
    }

    // ====== Mapper ======

    private OrderResponse toResponse(Order order) {
        OrderResponse resp = new OrderResponse();
        resp.setId(order.getId());
        resp.setUserId(order.getUserId());
        resp.setTotalAmount(order.getTotalAmount());
        resp.setDiscountPercent(order.getDiscountPercent());
        resp.setFinalAmount(order.getFinalAmount());
        resp.setPointsGranted(order.getPointsGranted());
        resp.setCreatedAt(order.getCreatedAt());

        List<OrderItemResponse> items = order.getItems().stream()
                .map(oi -> {
                    OrderItemResponse ir = new OrderItemResponse();
                    ir.setProductId(oi.getProductId());
                    ir.setProductName(oi.getProductName());
                    ir.setUnitPrice(oi.getUnitPrice());
                    ir.setQuantity(oi.getQuantity());
                    ir.setImagenUrl(oi.getImagenUrl());
                    ir.setSubtotal(oi.getSubtotal());
                    return ir;
                })
                .collect(Collectors.toList());

        resp.setItems(items);
        return resp;
    }
}
