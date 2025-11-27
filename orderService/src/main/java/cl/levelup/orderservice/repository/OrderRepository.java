package cl.levelup.orderservice.repository;

import cl.levelup.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);

    @Query("SELECT COALESCE(SUM(o.pointsGranted) - SUM(o.pointsSpent), 0) " +
            "FROM Order o WHERE o.userId = :userId")
    Long getCurrentPointsForUser(String userId);

    @Query("SELECT o.userId " +
            "FROM Order o " +
            "GROUP BY o.userId " +
            "ORDER BY COALESCE(SUM(o.pointsGranted) - SUM(o.pointsSpent), 0) DESC")
    List<String> findUserRankingByPoints();
}
