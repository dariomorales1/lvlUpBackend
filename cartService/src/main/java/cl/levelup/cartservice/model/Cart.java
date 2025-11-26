package cl.levelup.cartservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carts")
@Data
@Schema(description = "Entidad que representa un carrito de compras")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Schema(description = "ID único del carrito", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "ID del usuario autenticado (null para usuarios anónimos)", example = "user-123")
    @Column(name = "user_id", unique = true)
    private String userId;

    @Schema(description = "ID de sesión para usuarios anónimos", example = "session-abc123")
    @Column(name = "session_id", unique = true)
    private String sessionId;

    @Schema(description = "Lista de items en el carrito")
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @Schema(description = "Fecha y hora de última actualización", example = "2024-01-15T10:30:00")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    @Schema(description = "Calcula el monto total del carrito en centavos")
    public Long getTotalAmount() {
        if (items == null || items.isEmpty()) return 0L;
        return items.stream()
                .mapToLong(item -> item.getUnitPrice() * item.getQuantity())
                .sum();
    }
}