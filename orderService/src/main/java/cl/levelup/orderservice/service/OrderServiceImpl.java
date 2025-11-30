package cl.levelup.orderservice.service;

import cl.levelup.orderservice.client.CartClient;
import cl.levelup.orderservice.client.UserClient;
import cl.levelup.orderservice.dto.CartItemDto;
import cl.levelup.orderservice.dto.CartResponseDto;
import cl.levelup.orderservice.dto.OrderResponse;
import cl.levelup.orderservice.dto.UserSummaryDto;
import cl.levelup.orderservice.model.Order;
import cl.levelup.orderservice.model.OrderItem;
import cl.levelup.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final int POINTS_DISCOUNT_PERCENT = 15;
    private static final int EMAIL_DISCOUNT_PERCENT = 20;

    private final OrderRepository orderRepository;
    private final CartClient cartClient;
    private final UserClient userClient;

    @Override
    @Transactional
    public OrderResponse createOrderFromCart(String userId, boolean usePointsDiscount, String authHeader) {

        Mono<UserSummaryDto> userMono = userClient.getUserById(userId, authHeader);
        UserSummaryDto user = userMono.block();
        if (user == null) {
            throw new IllegalStateException("User not found: " + userId);
        }

        Mono<CartResponseDto> cartMono = cartClient.getUserCart(userId, authHeader);
        CartResponseDto cart = cartMono.block();
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty for user: " + userId);
        }

        long totalAmount = cart.getItems().stream()
                .mapToLong(ci -> (ci.getUnitPrice() != null ? ci.getUnitPrice() : 0L) *
                        (ci.getQuantity() != null ? ci.getQuantity() : 0))
                .sum();

        Long currentPoints = orderRepository.getCurrentPointsForUser(userId);
        if (currentPoints == null) currentPoints = 0L;

        boolean isTop5 = isUserInTop5ByPoints(userId);

        boolean canApplyPointsDiscount = usePointsDiscount && isTop5 && currentPoints > 0;

        boolean hasDuocEmail = user.getEmail() != null &&
                user.getEmail().toLowerCase().endsWith("@duocuc.cl");

        int discountPercent = 0;
        if (canApplyPointsDiscount) {
            discountPercent += POINTS_DISCOUNT_PERCENT;
        }
        if (hasDuocEmail) {
            discountPercent += EMAIL_DISCOUNT_PERCENT;
        }

        if (discountPercent > 35) {
            discountPercent = 35;
        }

        long finalAmount = totalAmount;
        if (discountPercent > 0) {
            finalAmount = totalAmount * (100 - discountPercent) / 100;
        }

        long pointsGranted = finalAmount;
        long pointsSpent = canApplyPointsDiscount ? currentPoints : 0L;

        Order order = new Order();
        order.setUserId(userId);
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalAmount(totalAmount);
        order.setDiscountPercent(discountPercent);
        order.setFinalAmount(finalAmount);
        order.setPointsGranted(pointsGranted);
        order.setPointsSpent(pointsSpent);
        order.setUsedPointsDiscount(canApplyPointsDiscount);
        order.setUsedEmailDiscount(hasDuocEmail);

        var orderItems = cart.getItems().stream()
                .map(ci -> mapCartItemToOrderItem(ci, order))
                .collect(Collectors.toList());

        order.setItems(orderItems);

        Order saved = orderRepository.save(order);

        cartClient.clearUserCart(userId, authHeader).block();

        return OrderResponse.fromEntity(saved);
    }

    private OrderItem mapCartItemToOrderItem(CartItemDto ci, Order order) {
        return OrderItem.builder()
                .order(order)
                .productId(ci.getProductId())
                .name(ci.getProductName())
                .image(ci.getImagenUrl())
                .price(ci.getUnitPrice() != null ? ci.getUnitPrice() : 0L)
                .quantity(ci.getQuantity() != null ? ci.getQuantity() : 0)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUser(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long getCurrentPoints(String userId) {
        Long pts = orderRepository.getCurrentPointsForUser(userId);
        return pts != null ? pts : 0L;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserInTop5ByPoints(String userId) {
        List<String> ranking = orderRepository.findUserRankingByPoints();
        return ranking.stream()
                .limit(5)
                .anyMatch(id -> id.equals(userId));
    }
}
