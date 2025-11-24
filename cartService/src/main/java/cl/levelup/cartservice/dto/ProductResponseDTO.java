package cl.levelup.cartservice.dto;

import lombok.Data;

@Data
public class ProductResponseDTO {
    private String id;
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private Boolean available;
}