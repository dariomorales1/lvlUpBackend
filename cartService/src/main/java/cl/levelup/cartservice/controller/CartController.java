package cl.levelup.cartservice.controller;

import cl.levelup.cartservice.dto.AddItemRequest;
import cl.levelup.cartservice.dto.CartResponse;
import cl.levelup.cartservice.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // ========= USUARIOS AUTENTICADOS =========

    @GetMapping("/user/{userId}")
    public ResponseEntity<CartResponse> getUserCart(
            @PathVariable String userId,
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

    @PostMapping("/user/{userId}/items")
    public ResponseEntity<CartResponse> addItemToCart(
            @PathVariable String userId,
            @Valid @RequestBody AddItemRequest request,
            @RequestHeader("Authorization") String authHeader) {

        System.out.println("📥 [POST] /user/" + userId + "/items - Iniciando...");
        System.out.println("📥 Producto: " + request.getProductId() + ", Cantidad: " + request.getQuantity());
        System.out.println("📥 Auth Header: " + (authHeader != null ? "PRESENTE" : "AUSENTE"));

        String authToken = extractToken(authHeader);
        System.out.println("📥 Token extraído: " + (authToken != null ? "✅ " + authToken.substring(0, Math.min(20, authToken.length())) + "..." : "❌"));

        try {
            CartResponse cart = CartResponse.fromEntity(
                    cartService.addItemToCart(userId, request, authToken)
            );
            System.out.println("✅ [POST] /user/" + userId + "/items - Item agregado exitosamente");
            System.out.println("✅ Carrito actual: " + cart.getItems().size() + " items, Total: " + cart.getTotalAmount());
            return ResponseEntity.status(HttpStatus.CREATED).body(cart);
        } catch (RuntimeException e) {
            System.out.println("❌ [POST] /user/" + userId + "/items - Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/user/{userId}/items/{productId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @PathVariable String userId,
            @PathVariable String productId,
            @RequestParam Integer quantity,
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

    @DeleteMapping("/user/{userId}/items/{productId}")
    public ResponseEntity<Void> removeItemFromCart(
            @PathVariable String userId,
            @PathVariable String productId,
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

    @DeleteMapping("/user/{userId}/clear")
    public ResponseEntity<Void> clearCart(
            @PathVariable String userId,
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

    @GetMapping("/guest/{sessionId}")
    public ResponseEntity<CartResponse> getGuestCart(@PathVariable String sessionId) {
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

    @PostMapping("/guest/{sessionId}/items")
    public ResponseEntity<CartResponse> addItemToGuestCart(
            @PathVariable String sessionId,
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

    @PutMapping("/guest/{sessionId}/items/{productId}")
    public ResponseEntity<CartResponse> updateGuestItemQuantity(
            @PathVariable String sessionId,
            @PathVariable String productId,
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

    @DeleteMapping("/guest/{sessionId}/items/{productId}")
    public ResponseEntity<Void> removeItemFromGuestCart(
            @PathVariable String sessionId,
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

    @DeleteMapping("/guest/{sessionId}/clear")
    public ResponseEntity<Void> clearGuestCart(@PathVariable String sessionId) {
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

    @PostMapping("/migrate/{sessionId}/to/{userId}")
    public ResponseEntity<CartResponse> migrateGuestCartToUser(
            @PathVariable String sessionId,
            @PathVariable String userId,
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