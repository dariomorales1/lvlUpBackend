// order-service/src/main/java/cl/levelup/orderservice/model/Order.java
package cl.levelup.orderservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    // Monto total del carrito (antes de descuentos)
    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    // Porcentaje de descuento aplicado (0.0, 0.15, 0.20, 0.35)
    @Column(name = "discount_percent", nullable = false)
    private Double discountPercent;

    // Monto final después de aplicar descuentos
    @Column(name = "final_amount", nullable = false)
    private Long finalAmount;

    // Puntos otorgados (= monto en pesos según tu regla)
    @Column(name = "points_granted", nullable = false)
    private Long pointsGranted;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
