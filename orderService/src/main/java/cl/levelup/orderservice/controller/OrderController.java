// order-service/src/main/java/cl/levelup/orderservice/controller/OrderController.java
package cl.levelup.orderservice.controller;

import cl.levelup.orderservice.dto.OrderResponse;
import cl.levelup.orderservice.dto.PointsResponse;
import cl.levelup.orderservice.dto.TopBuyerResponse;
import cl.levelup.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "APIs para creación de pedidos, puntos y top compradores")
public class OrderController {

    private final OrderService orderService;

    @Operation(
            summary = "Crear pedido desde carrito de usuario",
            description = "Genera una orden desde el carrito actual del usuario, aplica descuentos y puntos",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Orden creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Carrito vacío o error de negocio"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<OrderResponse> createOrderFromCart(
            @Parameter(description = "ID del usuario", required = true)
            @PathVariable String userId,
            @Parameter(description = "Token JWT de autenticación", required = true)
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = extractToken(authHeader);
        OrderResponse created = orderService.createOrderFromUserCart(userId, usePointsDiscount,token);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Obtener orden por ID")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @Parameter(description = "ID de la orden", required = true)
            @PathVariable UUID orderId
    ) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @Operation(summary = "Listar órdenes de un usuario")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUser(
            @Parameter(description = "ID del usuario", required = true)
            @PathVariable String userId
    ) {
        return ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }

    @Operation(summary = "Obtener puntos acumulados de un usuario")
    @GetMapping("/user/{userId}/points")
    public ResponseEntity<PointsResponse> getUserPoints(
            @Parameter(description = "ID del usuario", required = true)
            @PathVariable String userId
    ) {
        return ResponseEntity.ok(orderService.getUserPoints(userId));
    }

    @Operation(summary = "Top compradores (para aplicar 15% de descuento)")
    @GetMapping("/top-buyers")
    public ResponseEntity<List<TopBuyerResponse>> getTopBuyers(
            @Parameter(description = "Cantidad máxima a retornar (máx. 5)")
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(orderService.getTopBuyers(limit));
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Invalid authorization header");
    }
}
