package cl.levelup.productservice.service;

import cl.levelup.productservice.model.Product;
import cl.levelup.productservice.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findById(int id) {
        return productRepository.findById(id).orElse(null);
    }

    public void add(Product productRequest) {
        productRepository.save(productRequest);
    }

    public void update(int id, Product productRequest) {
        Product existing = findById(id);
        if(existing != null) {

            existing.setCodigo(productRequest.getCodigo());
            existing.setNombre(productRequest.getNombre());
            existing.setDescripcionCorta(productRequest.getDescripcionCorta());
            existing.setDescripcionLarga(productRequest.getDescripcionLarga());
            existing.setCategoria(productRequest.getCategoria());
            existing.setPrecio(productRequest.getPrecio());
            existing.setStock(productRequest.getStock());
            existing.setImagenUrl(productRequest.getImagenUrl());

            productRepository.save(existing);
        }
    }
}
