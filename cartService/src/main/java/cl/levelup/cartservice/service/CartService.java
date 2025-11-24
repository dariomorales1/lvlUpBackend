package cl.levelup.cartservice.service;

import cl.levelup.cartservice.dto.AddItemRequest;
import cl.levelup.cartservice.model.Cart;
import cl.levelup.cartservice.model.CartItem;
import cl.levelup.cartservice.repository.CartItemRepository;
import cl.levelup.cartservice.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    private final UserService userService;

    // ========= MÉTODOS PARA USUARIOS AUTENTICADOS =========

    @Transactional(readOnly = true)
    public Cart getCartByUserId(String userId) {
        return cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> createEmptyUserCart(userId));
    }

    @Transactional
    public Cart addItemToCart(String userId, AddItemRequest request, String authToken) {
        // Validar que el usuario existe
        Boolean userExists = userService.userExists(userId, authToken)
                .onErrorReturn(false)
                .blockOptional()
                .orElse(false);

        if (!userExists) {
            throw new RuntimeException("User not found: " + userId);
        }

        return processAddItem(userId, null, request, authToken);
    }

    @Transactional
    public Cart updateItemQuantity(String userId, String productId, Integer quantity, String authToken) {
        Boolean userExists = userService.userExists(userId, authToken)
                .onErrorReturn(false)
                .blockOptional()
                .orElse(false);

        if (!userExists) {
            throw new RuntimeException("User not found: " + userId);
        }

        return processUpdateQuantity(userId, null, productId, quantity);
    }

    @Transactional
    public void removeItemFromCart(String userId, String productId, String authToken) {
        Boolean userExists = userService.userExists(userId, authToken)
                .onErrorReturn(false)
                .blockOptional()
                .orElse(false);

        if (!userExists) {
            throw new RuntimeException("User not found: " + userId);
        }

        processRemoveItem(userId, null, productId);
    }

    @Transactional
    public void clearCart(String userId, String authToken) {
        Boolean userExists = userService.userExists(userId, authToken)
                .onErrorReturn(false)
                .blockOptional()
                .orElse(false);

        if (!userExists) {
            throw new RuntimeException("User not found: " + userId);
        }

        Cart cart = getCartByUserId(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    // ========= MÉTODOS PARA USUARIOS ANÓNIMOS =========

    @Transactional(readOnly = true)
    public Cart getCartBySessionId(String sessionId) {
        return cartRepository.findBySessionIdWithItems(sessionId)
                .orElseGet(() -> createEmptyGuestCart(sessionId));
    }

    @Transactional
    public Cart addItemToGuestCart(String sessionId, AddItemRequest request) {
        return processAddItem(null, sessionId, request, null);
    }

    @Transactional
    public Cart updateGuestItemQuantity(String sessionId, String productId, Integer quantity) {
        return processUpdateQuantity(null, sessionId, productId, quantity);
    }

    @Transactional
    public void removeItemFromGuestCart(String sessionId, String productId) {
        // 🔧 Antes estaba processRemoveItem(sessionId, null, productId) (parámetros corridos)
        processRemoveItem(null, sessionId, productId);
    }

    @Transactional
    public void clearGuestCart(String sessionId) {
        Cart cart = getCartBySessionId(sessionId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    // ========= MIGRACIÓN DE CARRITO =========

    @Transactional
    public Cart migrateGuestCartToUser(String sessionId, String userId) {
        Cart guestCart = getCartBySessionId(sessionId);
        Cart userCart = getCartByUserId(userId);

        if (guestCart.getItems().isEmpty()) {
            return userCart;
        }

        for (CartItem guestItem : guestCart.getItems()) {
            Optional<CartItem> existingItem =
                    cartItemRepository.findByUserIdAndProductId(userId, guestItem.getProductId());

            if (existingItem.isPresent()) {
                CartItem item = existingItem.get();
                item.setQuantity(item.getQuantity() + guestItem.getQuantity());
                cartItemRepository.save(item);
            } else {
                CartItem newItem = new CartItem();
                newItem.setCart(userCart);
                newItem.setProductId(guestItem.getProductId());
                newItem.setProductName(guestItem.getProductName());
                newItem.setUnitPrice(guestItem.getUnitPrice());
                newItem.setQuantity(guestItem.getQuantity());
                // 🔹 Importante: conservar también la imagen al migrar
                newItem.setImagenUrl(guestItem.getImagenUrl());

                cartItemRepository.save(newItem);
            }
        }

        cartRepository.delete(guestCart);
        return getCartByUserId(userId);
    }

    // ========= MÉTODOS PRIVADOS AUXILIARES =========

    private Cart processAddItem(String userId, String sessionId, AddItemRequest request, String authToken) {
        // 1) Obtener información del producto
        Map<String, Object> productInfo = getProductInfo(request.getProductId(), authToken);

        if (productInfo == null) {
            throw new RuntimeException("Product not found: " + request.getProductId());
        }

        // 2) Validar disponibilidad usando productInfo
        if (!isProductAvailable(request.getProductId(), request.getQuantity(), productInfo)) {
            throw new RuntimeException("Product not available in requested quantity");
        }

        // 3) Obtener o crear carrito
        Cart cart = getOrCreateCart(userId, sessionId);

        // 4) Ver si el item ya existe
        Optional<CartItem> existingItem =
                findExistingItem(userId, sessionId, request.getProductId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
        } else {
            CartItem newItem = createCartItem(cart, request, productInfo);
            cartItemRepository.save(newItem);
        }

        // 5) Retornar carrito actualizado
        return getUpdatedCart(userId, sessionId);
    }

    private Cart processUpdateQuantity(String userId, String sessionId, String productId, Integer quantity) {
        Optional<CartItem> itemOpt = findExistingItem(userId, sessionId, productId);

        if (itemOpt.isPresent()) {
            CartItem item = itemOpt.get();
            if (quantity <= 0) {
                cartItemRepository.delete(item);
            } else {
                item.setQuantity(quantity);
                cartItemRepository.save(item);
            }
        }

        return getUpdatedCart(userId, sessionId);
    }

    private void processRemoveItem(String userId, String sessionId, String productId) {
        if (userId != null) {
            cartItemRepository.deleteByUserIdAndProductId(userId, productId);
        } else {
            cartItemRepository.deleteBySessionIdAndProductId(sessionId, productId);
        }
    }

    private Map<String, Object> getProductInfo(String productId, String authToken) {
        try {
            Map<String, Object> product = productService.getProductById(productId).block();
            System.out.println("📦 getProductInfo - product: " + product);
            return product;
        } catch (Exception e) {
            System.out.println("📦 getProductInfo - error: " + e.getMessage());
            throw new RuntimeException("Error getting product info: " + e.getMessage());
        }
    }

    private boolean isProductAvailable(String productId,
                                       Integer quantity,
                                       Map<String, Object> productInfo) {

        System.out.println("📦 isProductAvailable - productInfo: " + productInfo);

        if (quantity == null || quantity <= 0) return false;

        Object stockObj = productInfo.get("stock");
        Object availableObj = productInfo.get("available");
        Object activoObj = productInfo.get("activo");

        boolean available =
                Boolean.TRUE.equals(availableObj) ||
                        Boolean.TRUE.equals(activoObj) ||
                        (availableObj == null && activoObj == null);

        int stock = (stockObj == null)
                ? Integer.MAX_VALUE
                : ((Number) stockObj).intValue();

        return available && stock >= quantity;
    }

    private Cart getOrCreateCart(String userId, String sessionId) {
        if (userId != null) {
            return cartRepository.findByUserIdWithItems(userId)
                    .orElseGet(() -> createEmptyUserCart(userId));
        } else {
            return cartRepository.findBySessionIdWithItems(sessionId)
                    .orElseGet(() -> createEmptyGuestCart(sessionId));
        }
    }

    private Optional<CartItem> findExistingItem(String userId, String sessionId, String productId) {
        if (userId != null) {
            return cartItemRepository.findByUserIdAndProductId(userId, productId);
        } else {
            return cartItemRepository.findBySessionIdAndProductId(sessionId, productId);
        }
    }

    private CartItem createCartItem(Cart cart, AddItemRequest request, Map<String, Object> productInfo) {
        CartItem newItem = new CartItem();
        newItem.setCart(cart);
        newItem.setProductId(request.getProductId());

        // Nombre: "name" o "nombre"
        Object nameObj = productInfo.get("name");
        if (nameObj == null) {
            nameObj = productInfo.get("nombre");
        }
        if (nameObj == null) {
            throw new RuntimeException("Product name not found for productId: " + request.getProductId());
        }
        newItem.setProductName(nameObj.toString());

        // Precio: "price" o "precio"
        Object priceObj = productInfo.get("price");
        if (priceObj == null) {
            priceObj = productInfo.get("precio");
        }
        if (priceObj == null) {
            throw new RuntimeException("Product price not found for productId: " + request.getProductId());
        }
        long price = ((Number) priceObj).longValue();
        newItem.setUnitPrice(price);

        // 🔹 Imagen: "imageUrl" o "imagenUrl" (tu ProductController usa "imagenUrl")
        Object imageObj = productInfo.get("imageUrl");
        if (imageObj == null) {
            imageObj = productInfo.get("imagenUrl");
        }
        if (imageObj == null) {
            // Si quieres que nunca sea null porque en la columna es NOT NULL,
            // puedes poner un placeholder o lanzar excepción.
            imageObj = "/default-product.jpg";
        }
        newItem.setImagenUrl(imageObj.toString());

        newItem.setQuantity(request.getQuantity());
        return newItem;
    }

    private Cart getUpdatedCart(String userId, String sessionId) {
        if (userId != null) {
            return cartRepository.findByUserIdWithItems(userId)
                    .orElseGet(() -> createEmptyUserCart(userId));
        } else {
            return cartRepository.findBySessionIdWithItems(sessionId)
                    .orElseGet(() -> createEmptyGuestCart(sessionId));
        }
    }

    private Cart createEmptyUserCart(String userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        return cartRepository.save(cart);
    }

    private Cart createEmptyGuestCart(String sessionId) {
        Cart cart = new Cart();
        cart.setSessionId(sessionId);
        return cartRepository.save(cart);
    }
}
