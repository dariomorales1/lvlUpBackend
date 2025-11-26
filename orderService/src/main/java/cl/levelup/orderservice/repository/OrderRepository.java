package cl.levelup.orderservice.repository;

import cl.levelup.orderservice.model.Order;
import cl.levelup.orderservice.repository.projection.UserTotalProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("""
           SELECT o.userId AS userId, SUM(o.finalAmount) AS totalSpent
           FROM Order o
           GROUP BY o.userId
           ORDER BY totalSpent DESC
           """)
    List<UserTotalProjection> findTopUsers(Pageable pageable);

    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);
}
