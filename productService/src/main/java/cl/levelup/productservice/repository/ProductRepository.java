package cl.levelup.productservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import cl.levelup.productservice.model.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    public Product findByCodigo(String codigo);
    public void deleteByCodigo(String codigo);
}
