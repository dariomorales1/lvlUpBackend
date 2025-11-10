package cl.levelup.authservice.repository;

import cl.levelup.authservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByUsernameIgnoreCase(String username);
}

