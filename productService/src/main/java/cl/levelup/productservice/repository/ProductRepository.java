package cl.levelup.productservice.repository;

import cl.levelup.productservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    Product findByCodigo(String codigo);

}
