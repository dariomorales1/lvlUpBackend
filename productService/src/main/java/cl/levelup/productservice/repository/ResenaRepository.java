package cl.levelup.productservice.repository;

import cl.levelup.productservice.model.Resena;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResenaRepository extends JpaRepository<Resena, Long> {

    List<Resena> findByProduct_Codigo(String productCode);

    Optional<Resena> findByIdAndProduct_Codigo(Long id, String productCode);

}
