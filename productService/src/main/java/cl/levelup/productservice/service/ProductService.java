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

    public Product findByCode(String codigo) {
        return productRepository.findByCodigo(codigo);
    }

    public void add(Product productRequest) {
        productRepository.save(productRequest);
    }

    public void delete(int id) {
        productRepository.deleteById(id);
    }

    public Product findByCodigo(String codigo) {
        return productRepository.findByCodigo(codigo);
    }

    public void update(Product existing, Product data) {
        existing.setNombre(data.getNombre());
        existing.setDescripcionCorta(data.getDescripcionCorta());
        existing.setDescripcionLarga(data.getDescripcionLarga());
        existing.setCategoria(data.getCategoria());
        existing.setPrecio(data.getPrecio());
        existing.setStock(data.getStock());
        existing.setImagenUrl(data.getImagenUrl());
        productRepository.save(existing);
    }

}
