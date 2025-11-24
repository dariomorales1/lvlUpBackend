package cl.levelup.cartservice.repository;

import cl.levelup.cartservice.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    // Para usuarios autenticados
    Optional<Cart> findByUserId(String userId);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.userId = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") String userId);

    // NUEVO: Para usuarios anónimos
    Optional<Cart> findBySessionId(String sessionId);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.sessionId = :sessionId")
    Optional<Cart> findBySessionIdWithItems(@Param("sessionId") String sessionId);

    void deleteByUserId(String userId);
    void deleteBySessionId(String sessionId);
}