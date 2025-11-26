package cl.levelup.cartservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Schema(description = "Solicitud para agregar un item al carrito")
public class AddItemRequest {

    @Schema(description = "ID único del producto", example = "PROD-001", required = true)
    @NotBlank(message = "Product ID is required")
    private String productId;

    @Schema(description = "Cantidad del producto a agregar", example = "2", required = true)
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
}