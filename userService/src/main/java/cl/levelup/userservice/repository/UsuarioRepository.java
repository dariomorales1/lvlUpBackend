package cl.levelup.userservice.repository;

import cl.levelup.userservice.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    boolean existsByEmail(String email);
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findAll();
}

