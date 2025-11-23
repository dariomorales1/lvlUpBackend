package cl.levelup.productservice.service;

import cl.levelup.productservice.model.Product;
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

    public Product add(Product product) {
        return productRepository.save(product);
    }

    public void delete(String codigo) {
        Product existing = productRepository.findByCodigo(codigo);
        if (existing != null) {
            productRepository.delete(existing);
        }
    }

    /**
     * Determina si la actualización es parcial:
     * si falta alguno de los campos "principales", asumimos PATCH.
     */
    public boolean isPartialUpdate(Product req) {
        return req.getNombre() == null
                || req.getDescripcionCorta() == null
                || req.getDescripcionLarga() == null
                || req.getCategoria() == null
                || req.getPrecio() == null
                || req.getStock() == null
                || req.getImagenUrl() == null;
    }

    public void partialUpdate(Product existing, Product req) {
        if (req.getNombre() != null) {
            existing.setNombre(req.getNombre());
        }
        if (req.getDescripcionCorta() != null) {
            existing.setDescripcionCorta(req.getDescripcionCorta());
        }
        if (req.getDescripcionLarga() != null) {
            existing.setDescripcionLarga(req.getDescripcionLarga());
        }
        if (req.getCategoria() != null) {
            existing.setCategoria(req.getCategoria());
        }
        if (req.getPrecio() != null) {
            existing.setPrecio(req.getPrecio());
        }
        if (req.getStock() != null) {
            existing.setStock(req.getStock());
        }
        if (req.getImagenUrl() != null) {
            existing.setImagenUrl(req.getImagenUrl());
        }

        productRepository.save(existing);
    }

    public void update(Product existing, Product req) {
        existing.setNombre(req.getNombre());
        existing.setDescripcionCorta(req.getDescripcionCorta());
        existing.setDescripcionLarga(req.getDescripcionLarga());
        existing.setCategoria(req.getCategoria());
        existing.setPrecio(req.getPrecio());
        existing.setStock(req.getStock());
        existing.setImagenUrl(req.getImagenUrl());

        productRepository.save(existing);
    }
}
