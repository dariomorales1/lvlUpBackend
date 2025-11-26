package cl.levelup.cartservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "cart_items")
@Data
@Schema(description = "Entidad que representa un item en el carrito de compras")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Schema(description = "ID único del item", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Carrito al que pertenece este item")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonIgnore
    private Cart cart;

    @Schema(description = "ID del producto", example = "PROD-001", required = true)
    @Column(name = "product_id", nullable = false)
    private String productId;

    @Schema(description = "Nombre del producto", example = "Laptop Gaming", required = true)
    @Column(name = "product_name", nullable = false)
    private String productName;

    @Schema(description = "Precio unitario en centavos", example = "129999", required = true)
    @Column(name = "unit_price", nullable = false)
    private Long unitPrice;

    @Schema(description = "Cantidad del producto", example = "2", required = true)
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Schema(description = "URL de la imagen del producto", example = "https://example.com/product.jpg")
    @Column(name = "imagenurl")
    private String imagenUrl;

    @Schema(description = "Calcula el subtotal del item en centavos")
    public Long getSubtotal() {
        return unitPrice * quantity;
    }
}