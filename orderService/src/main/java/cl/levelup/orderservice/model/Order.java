package cl.levelup.orderservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Column(name = "discount_percent", nullable = false)
    private Integer discountPercent;

    @Column(name = "final_amount", nullable = false)
    private Long finalAmount;

    @Column(name = "points_granted", nullable = false)
    private Long pointsGranted;

    @Column(name = "points_spent", nullable = false)
    private Long pointsSpent;

    @Column(name = "used_points_discount", nullable = false)
    @Builder.Default
    private boolean usedPointsDiscount = false;

    @Column(name = "used_email_discount", nullable = false)
    @Builder.Default
    private boolean usedEmailDiscount = false;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
}
