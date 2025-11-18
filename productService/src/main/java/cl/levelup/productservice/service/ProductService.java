package cl.levelup.productservice.service;

import cl.levelup.productservice.model.Product;
import cl.levelup.productservice.model.ProductSpecification;
import cl.levelup.productservice.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findByCodigo(String codigo) {
        return productRepository.findByCodigo(codigo);
    }

    public void add(Product productRequest) {
        // Mapear strings a ProductSpecification si vienen como lista de strings
        if (productRequest.getEspecificaciones() != null && !productRequest.getEspecificaciones().isEmpty()) {
            List<ProductSpecification> specs = productRequest.getEspecificaciones().stream()
                    .map(desc -> {
                        ProductSpecification ps = new ProductSpecification();
                        ps.setSpecification(desc.getSpecification()); // si recibes objetos, ajusta
                        ps.setProduct(productRequest);
                        return ps;
                    }).toList();
            productRequest.setEspecificaciones(specs);
        }

        productRepository.save(productRequest);
    }

    public void delete(int id) {
        productRepository.deleteById(id);
    }

    public void update(Product existing, Product data) {
        existing.setNombre(data.getNombre());
        existing.setDescripcionCorta(data.getDescripcionCorta());
        existing.setDescripcionLarga(data.getDescripcionLarga());
        existing.setCategoria(data.getCategoria());
        existing.setPrecio(data.getPrecio());
        existing.setStock(data.getStock());
        existing.setImagenUrl(data.getImagenUrl());

        if (data.getEspecificaciones() != null) {
            // Limpiar existentes y agregar nuevos
            existing.getEspecificaciones().clear();
            data.getEspecificaciones().forEach(ps -> {
                ps.setProduct(existing);
                existing.getEspecificaciones().add(ps);
            });
        }

        productRepository.save(existing);
    }

    public void partialUpdate(Product existing, Product data) {
        if (data.getNombre() != null) existing.setNombre(data.getNombre());
        if (data.getDescripcionCorta() != null) existing.setDescripcionCorta(data.getDescripcionCorta());
        if (data.getDescripcionLarga() != null) existing.setDescripcionLarga(data.getDescripcionLarga());
        if (data.getCategoria() != null) existing.setCategoria(data.getCategoria());
        if (data.getPrecio() != null) existing.setPrecio(data.getPrecio());
        if (data.getStock() != null) existing.setStock(data.getStock());
        if (data.getImagenUrl() != null) existing.setImagenUrl(data.getImagenUrl());
        if (data.getEspecificaciones() != null) {
            existing.getEspecificaciones().clear();
            data.getEspecificaciones().forEach(ps -> {
                ps.setProduct(existing);
                existing.getEspecificaciones().add(ps);
            });
        }

        productRepository.save(existing);
    }

    public boolean isPartialUpdate(Product req) {
        return (req.getNombre() == null ||
                req.getDescripcionCorta() == null ||
                req.getDescripcionLarga() == null ||
                req.getCategoria() == null ||
                req.getPrecio() == null ||
                req.getStock() == null ||
                req.getImagenUrl() == null ||
                req.getEspecificaciones() == null);
    }
}