package cl.levelup.cartservice.controller;

import cl.levelup.cartservice.dto.AddItemRequest;
import cl.levelup.cartservice.dto.CartResponse;
import cl.levelup.cartservice.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
@Tag(name = "Shopping Cart", description = "APIs para gestión de carritos de compras - usuarios autenticados y anónimos")
public class CartController {

    private final CartService cartService;

    // ========= USUARIOS AUTENTICADOS =========

    @Operation(
            summary = "Obtener carrito de usuario",
            description = "Retorna el carrito de compras de un usuario autenticado",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrito obtenido exitosamente"),
            @ApiResponse(responseCode = "404", description = "Carrito no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<CartResponse> getUserCart(
            @Parameter(description = "ID del usuario", example = "user-123", required = true)
            @PathVariable String userId,
            @Parameter(description = "Token JWT de autenticación", required = true)
            @RequestHeader("Authorization") String authHeader) {

        System.out.println("📥 [GET] /user/" + userId + " - Iniciando...");
        String authToken = extractToken(authHeader);
        System.out.println("📥 Token: " + (authToken != null ? "✅ Extraído" : "❌ Error"));

        try {
            CartResponse cart = CartResponse.fromEntity(cartService.getCartByUserId(userId));
            System.out.println("✅ [GET] /user/" + userId + " - Carrito obtenido: " + cart.getItems().size() + " items");
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            System.out.println("❌ [GET] /user/" + userId + " - Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Agregar item al carrito",
            description = "Agrega un producto al carrito del usuario autenticado",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item agregado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o producto no disponible"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping("/user/{userId}/items")
    public ResponseEntity<CartResponse> addItemToCart(
            @Parameter(description = "ID del usuario", example = "user-123", required = true)
            @PathVariable String userId,
            @Parameter(description = "Datos del item a agregar", required = true)
            @Valid @RequestBody AddItemRequest request,
            @Parameter(description = "Token JWT de autenticación", required = true)
            @RequestHeader("Authorization") String authHeader) {

        System.out.println("📥 [POST] /user/" + userId + "/items - Iniciando...");
        System.out.println("📥 Producto: " + request.getProductId() + ", Cantidad: " + request.getQuantity());

        String authToken = extractToken(authHeader);

        try {
            CartResponse cart = CartResponse.fromEntity(
                    cartService.addItemToCart(userId, request, authToken)
            );
            System.out.println("✅ [POST] /user/" + userId + "/items - Item agregado exitosamente");
            return ResponseEntity.status(HttpStatus.CREATED).body(cart);
        } catch (RuntimeException e) {
            System.out.println("❌ [POST] /user/" + userId + "/items - Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "Actualizar cantidad de item",
            description = "Actualiza la cantidad de un producto específico en el carrito",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cantidad actualizada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Item no encontrado en el carrito"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PutMapping("/user/{userId}/items/{productId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @Parameter(description = "ID del usuario", example = "user-123", required = true)
            @PathVariable String userId,
            @Parameter(description = "ID del producto", example = "PROD-001", required = true)
            @PathVariable String productId,
            @Parameter(description = "Nueva cantidad", example = "2", required = true)
            @RequestParam Integer quantity,
            @Parameter(description = "Token JWT de autenticación", required = true)
            @RequestHeader("Authorization") String authHeader) {

        System.out.println("📥 [PUT] /user/" + userId + "/items/" + productId + " - Cantidad: " + quantity);

        String authToken = extractToken(authHeader);

        try {
            CartResponse cart = CartResponse.fromEntity(
                    cartService.updateItemQuantity(userId, productId, quantity, authToken)
            );
            System.out.println("✅ [PUT] /user/" + userId + "/items/" + productId + " - Cantidad actualizada");
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            System.out.println("❌ [PUT] /user/" + userId + "/items/" + productId + " - Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Eliminar item del carrito",
            description = "Remueve un producto específico del carrito del usuario",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Item no encontrado en el carrito"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @DeleteMapping("/user/{userId}/items/{productId}")
    public ResponseEntity<Void> removeItemFromCart(
            @Parameter(description = "ID del usuario", example = "user-123", required = true)
            @PathVariable String userId,
            @Parameter(description = "ID del producto", example = "PROD-001", required = true)
            @PathVariable String productId,
            @Parameter(description = "Token JWT de autenticación", required = true)
            @RequestHeader("Authorization") String authHeader) {

        System.out.println("📥 [DELETE] /user/" + userId + "/items/" + productId + " - Eliminando item...");

        String authToken = extractToken(authHeader);

        try {
            cartService.removeItemFromCart(userId, productId, authToken);
            System.out.println("✅ [DELETE] /user/" + userId + "/items/" + productId + " - Item eliminado");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.out.println("❌ [DELETE] /user/" + userId + "/items/" + productId + " - Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Vaciar carrito",
            description = "Elimina todos los items del carrito del usuario",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Carrito vaciado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Carrito no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @DeleteMapping("/user/{userId}/clear")
    public ResponseEntity<Void> clearCart(
            @Parameter(description = "ID del usuario", example = "user-123", required = true)
            @PathVariable String userId,
            @Parameter(description = "Token JWT de autenticación", required = true)
            @RequestHeader("Authorization") String authHeader) {

        System.out.println("📥 [DELETE] /user/" + userId + "/clear - Vaciar carrito...");

        String authToken = extractToken(authHeader);

        try {
            cartService.clearCart(userId, authToken);
            System.out.println("✅ [DELETE] /user/" + userId + "/clear - Carrito vaciado");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.out.println("❌ [DELETE] /user/" + userId + "/clear - Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    // ========= USUARIOS ANÓNIMOS =========

    @Operation(
            summary = "Obtener carrito de invitado",
            description = "Retorna el carrito de compras de un usuario anónimo (guest)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrito guest obtenido exitosamente"),
            @ApiResponse(responseCode = "404", description = "Carrito guest no encontrado")
    })
    @GetMapping("/guest/{sessionId}")
    public ResponseEntity<CartResponse> getGuestCart(
            @Parameter(description = "ID de sesión del usuario anónimo", example = "session-abc123", required = true)
            @PathVariable String sessionId) {
        System.out.println("📥 [GET] /guest/" + sessionId + " - Obteniendo carrito guest...");

        try {
            CartResponse cart = CartResponse.fromEntity(cartService.getCartBySessionId(sessionId));
            System.out.println("✅ [GET] /guest/" + sessionId + " - Carrito guest obtenido: " + cart.getItems().size() + " items");
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            System.out.println("❌ [GET] /guest/" + sessionId + " - Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Agregar item a carrito de invitado",
            description = "Agrega un producto al carrito de un usuario anónimo"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item agregado exitosamente al carrito guest"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o producto no disponible")
    })
    @PostMapping("/guest/{sessionId}/items")
    public ResponseEntity<CartResponse> addItemToGuestCart(
            @Parameter(description = "ID de sesión del usuario anónimo", example = "session-abc123", required = true)
            @PathVariable String sessionId,
            @Parameter(description = "Datos del item a agregar", required = true)
            @Valid @RequestBody AddItemRequest request) {

        System.out.println("📥 [POST] /guest/" + sessionId + "/items - Producto: " + request.getProductId() + ", Cantidad: " + request.getQuantity());

        try {
            CartResponse cart = CartResponse.fromEntity(
                    cartService.addItemToGuestCart(sessionId, request)
            );
            System.out.println("✅ [POST] /guest/" + sessionId + "/items - Item agregado a guest cart");
            return ResponseEntity.status(HttpStatus.CREATED).body(cart);
        } catch (RuntimeException e) {
            System.out.println("❌ [POST] /guest/" + sessionId + "/items - Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "Actualizar cantidad en carrito de invitado",
            description = "Actualiza la cantidad de un producto en el carrito de usuario anónimo"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cantidad actualizada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Item no encontrado en el carrito guest")
    })
    @PutMapping("/guest/{sessionId}/items/{productId}")
    public ResponseEntity<CartResponse> updateGuestItemQuantity(
            @Parameter(description = "ID de sesión del usuario anónimo", example = "session-abc123", required = true)
            @PathVariable String sessionId,
            @Parameter(description = "ID del producto", example = "PROD-001", required = true)
            @PathVariable String productId,
            @Parameter(description = "Nueva cantidad", example = "3", required = true)
            @RequestParam Integer quantity) {

        System.out.println("📥 [PUT] /guest/" + sessionId + "/items/" + productId + " - Cantidad: " + quantity);

        try {
            CartResponse cart = CartResponse.fromEntity(
                    cartService.updateGuestItemQuantity(sessionId, productId, quantity)
            );
            System.out.println("✅ [PUT] /guest/" + sessionId + "/items/" + productId + " - Cantidad guest actualizada");
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            System.out.println("❌ [PUT] /guest/" + sessionId + "/items/" + productId + " - Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Eliminar item de carrito de invitado",
            description = "Remueve un producto específico del carrito de usuario anónimo"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Item no encontrado en el carrito guest")
    })
    @DeleteMapping("/guest/{sessionId}/items/{productId}")
    public ResponseEntity<Void> removeItemFromGuestCart(
            @Parameter(description = "ID de sesión del usuario anónimo", example = "session-abc123", required = true)
            @PathVariable String sessionId,
            @Parameter(description = "ID del producto", example = "PROD-001", required = true)
            @PathVariable String productId) {

        System.out.println("📥 [DELETE] /guest/" + sessionId + "/items/" + productId + " - Eliminando item guest...");

        try {
            cartService.removeItemFromGuestCart(sessionId, productId);
            System.out.println("✅ [DELETE] /guest/" + sessionId + "/items/" + productId + " - Item guest eliminado");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.out.println("❌ [DELETE] /guest/" + sessionId + "/items/" + productId + " - Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Vaciar carrito de invitado",
            description = "Elimina todos los items del carrito de usuario anónimo"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Carrito guest vaciado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Carrito guest no encontrado")
    })
    @DeleteMapping("/guest/{sessionId}/clear")
    public ResponseEntity<Void> clearGuestCart(
            @Parameter(description = "ID de sesión del usuario anónimo", example = "session-abc123", required = true)
            @PathVariable String sessionId) {
        System.out.println("📥 [DELETE] /guest/" + sessionId + "/clear - Vaciar carrito guest...");

        try {
            cartService.clearGuestCart(sessionId);
            System.out.println("✅ [DELETE] /guest/" + sessionId + "/clear - Carrito guest vaciado");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.out.println("❌ [DELETE] /guest/" + sessionId + "/clear - Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    // ========= MIGRACIÓN =========

    @Operation(
            summary = "Migrar carrito de invitado a usuario",
            description = "Transfiere todos los items de un carrito de invitado a un usuario autenticado",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Migración completada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Error en la migración"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping("/migrate/{sessionId}/to/{userId}")
    public ResponseEntity<CartResponse> migrateGuestCartToUser(
            @Parameter(description = "ID de sesión del usuario anónimo", example = "session-abc123", required = true)
            @PathVariable String sessionId,
            @Parameter(description = "ID del usuario autenticado", example = "user-123", required = true)
            @PathVariable String userId,
            @Parameter(description = "Token JWT de autenticación", required = true)
            @RequestHeader("Authorization") String authHeader) {

        System.out.println("🔄 [POST] /migrate/" + sessionId + "/to/" + userId + " - Migrando carrito...");

        String authToken = extractToken(authHeader);

        try {
            CartResponse cart = CartResponse.fromEntity(
                    cartService.migrateGuestCartToUser(sessionId, userId)
            );
            System.out.println("✅ [POST] /migrate/" + sessionId + "/to/" + userId + " - Migración completada");
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            System.out.println("❌ [POST] /migrate/" + sessionId + "/to/" + userId + " - Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // ========= ENDPOINT DE HEALTH =========

    @Operation(summary = "Health check", description = "Verifica el estado del servicio de carritos")
    @ApiResponse(responseCode = "200", description = "Servicio funcionando correctamente")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        System.out.println("🏥 [GET] /health - Health check");
        return ResponseEntity.ok("Cart Service is healthy - " + System.currentTimeMillis());
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        System.out.println("❌ Token inválido o ausente: " + authHeader);
        throw new RuntimeException("Invalid authorization header");
    }
}