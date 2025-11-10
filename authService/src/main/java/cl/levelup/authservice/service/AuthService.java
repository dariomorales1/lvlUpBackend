package cl.levelup.authservice.service;

import cl.levelup.authservice.dto.LoginRequest;
import cl.levelup.authservice.dto.RegisterRequest;
import cl.levelup.authservice.dto.UserDTO;
import cl.levelup.authservice.model.User;
import cl.levelup.authservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserDTO register(RegisterRequest req) {
        // Validaciones simples
        userRepository.findByEmailIgnoreCase(req.getEmail()).ifPresent(u -> {
            throw new IllegalArgumentException("Email ya registrado");
        });
        userRepository.findByUsernameIgnoreCase(req.getUsername()).ifPresent(u -> {
            throw new IllegalArgumentException("Username ya registrado");
        });

        String hash = passwordEncoder.encode(req.getPassword());
        User user = new User(req.getEmail(), req.getUsername(), hash);
        User saved = userRepository.save(user);
        return new UserDTO(saved.getId(), saved.getEmail(), saved.getUsername());
    }

    @Transactional(readOnly = true)
    public UserDTO login(LoginRequest req) {
        User user = userRepository.findByUsernameIgnoreCase(req.getUsernameOrEmail())
                .or(() -> userRepository.findByEmailIgnoreCase(req.getUsernameOrEmail()))
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!user.getEnabled()) {
            throw new IllegalStateException("Usuario deshabilitado");
        }

        boolean ok = passwordEncoder.matches(req.getPassword(), user.getPasswordHash());
        if (!ok) throw new IllegalArgumentException("Credenciales inválidas");

        // Aquí podrías emitir un JWT más adelante. Por ahora devolvemos el perfil básico.
        return new UserDTO(user.getId(), user.getEmail(), user.getUsername());
    }
}

