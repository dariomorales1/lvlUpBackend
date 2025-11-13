package cl.levelup.productservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String descripcionCorta;

    @Column(nullable = false)
    private String descripcionLarga;

    @Column(nullable = false)
    private String categoria;

    @Column(nullable = false)
    private Double precio;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private String imagenUrl;

    @Column(nullable = false)
    private List<String> especificaciones;
}
