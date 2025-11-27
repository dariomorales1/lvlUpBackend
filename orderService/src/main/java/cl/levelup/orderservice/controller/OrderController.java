package cl.levelup.orderservice.controller;

import cl.levelup.orderservice.dto.CreateOrderRequest;
import cl.levelup.orderservice.dto.OrderResponse;
import cl.levelup.orderservice.dto.PointsSummaryResponse;
import cl.levelup.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "APIs para gestión de pedidos")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Health check OrderService")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Order Service is healthy - " + System.currentTimeMillis());
    }

    @Operation(
            summary = "Crear pedido desde el carrito",
            description = "Crea una orden para el usuario a partir de su carrito actual. " +
                    "Puede opcionalmente usar sus puntos para habilitar el descuento (solo TOP 5)."
    )
    @PostMapping("/user/{userId}")
    public ResponseEntity<OrderResponse> createOrder(
            @Parameter(description = "ID del usuario", required = true)
            @PathVariable String userId,
            @RequestBody(required = false) CreateOrderRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        boolean usePointsDiscount = request != null && request.isUsePointsDiscount();
        OrderResponse response =
                orderService.createOrderFromCart(userId, usePointsDiscount, authHeader);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Obtener órdenes de un usuario",
            description = "Retorna el historial de compras del usuario, con sus productos."
    )
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUser(
            @PathVariable String userId,
            @RequestHeader("Authorization") String authHeader
    ) {
        List<OrderResponse> orders = orderService.getOrdersByUser(userId);
        return ResponseEntity.ok(orders);
    }

    @Operation(
            summary = "Obtener puntos actuales del usuario",
            description = "Retorna el total de puntos disponibles (puntos ganados - puntos gastados)."
    )
    @GetMapping("/user/{userId}/points")
    public ResponseEntity<PointsSummaryResponse> getUserPoints(
            @PathVariable String userId,
            @RequestHeader("Authorization") String authHeader
    ) {
        Long points = orderService.getCurrentPoints(userId);
        PointsSummaryResponse resp = PointsSummaryResponse.builder()
                .userId(userId)
                .totalPoints(points)
                .build();
        return ResponseEntity.ok(resp);
    }
}
