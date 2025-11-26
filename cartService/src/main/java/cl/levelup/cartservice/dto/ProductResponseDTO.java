package cl.levelup.cartservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Respuesta con datos del producto para integración")
public class ProductResponseDTO {

    @Schema(description = "ID único del producto", example = "PROD-001")
    private String id;

    @Schema(description = "Nombre del producto", example = "Laptop Gaming")
    private String name;

    @Schema(description = "Descripción del producto", example = "Laptop para gaming con RTX 4060")
    private String description;

    @Schema(description = "Precio del producto", example = "1299.99")
    private Double price;

    @Schema(description = "Stock disponible", example = "50")
    private Integer stock;

    @Schema(description = "Indica si el producto está disponible", example = "true")
    private Boolean available;
}