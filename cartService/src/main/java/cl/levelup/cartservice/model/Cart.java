package cl.levelup.cartservice.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carts")
@Data
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // Para usuarios autenticados
    @Column(name = "user_id", unique = true)
    private String userId;

    // Para usuarios anónimos
    @Column(name = "session_id", unique = true)
    private String sessionId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getTotalAmount() {
        if (items == null || items.isEmpty()) return 0L;
        return items.stream()
                .mapToLong(item -> item.getUnitPrice() * item.getQuantity())
                .sum();
    }
}
