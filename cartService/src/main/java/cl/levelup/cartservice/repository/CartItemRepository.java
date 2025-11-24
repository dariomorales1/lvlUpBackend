package cl.levelup.cartservice.repository;

import cl.levelup.cartservice.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    // Para usuarios autenticados
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.userId = :userId AND ci.productId = :productId")
    Optional<CartItem> findByUserIdAndProductId(@Param("userId") String userId,
                                                @Param("productId") String productId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.userId = :userId AND ci.productId = :productId")
    void deleteByUserIdAndProductId(@Param("userId") String userId,
                                    @Param("productId") String productId);

    // NUEVO: Para usuarios anónimos
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.sessionId = :sessionId AND ci.productId = :productId")
    Optional<CartItem> findBySessionIdAndProductId(@Param("sessionId") String sessionId,
                                                   @Param("productId") String productId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.sessionId = :sessionId AND ci.productId = :productId")
    void deleteBySessionIdAndProductId(@Param("sessionId") String sessionId,
                                       @Param("productId") String productId);
}