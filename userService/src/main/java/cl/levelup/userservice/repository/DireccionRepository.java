package cl.levelup.userservice.repository;

import cl.levelup.userservice.model.Direccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DireccionRepository extends JpaRepository<Direccion, Long> {
    List<Direccion> findByUsuarioIdOrderByCreadoEnDesc(String usuarioId);
}
