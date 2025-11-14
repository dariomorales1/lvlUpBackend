package cl.levelup.userservice.service;

import cl.levelup.userservice.model.Usuario;
import cl.levelup.userservice.model.UsuarioRequest;
import cl.levelup.userservice.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario findById(String id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @Override
    public Usuario createFromRequest(UsuarioRequest request, String uidFirebase) {

        // Si ya existe, devolverlo
        if (usuarioRepository.existsById(uidFirebase)) {
            return usuarioRepository.findById(uidFirebase)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        }

        // Validar email duplicado
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está en uso");
        }

        Usuario u = new Usuario();
        u.setId(uidFirebase);
        u.setEmail(request.getEmail());
        u.setNombre(request.getNombre());
        u.setFechaNacimiento(request.getFechaNacimiento());
        u.setAvatarUrl(request.getAvatarUrl());
        u.setActivo(true);
        u.setCreadoEn(OffsetDateTime.now());
        u.setActualizadoEn(OffsetDateTime.now());

        return usuarioRepository.save(u);
    }

    @Override
    public Usuario update(String id, UsuarioRequest request) {
        Usuario u = findById(id);

        // Evitar emails duplicados
        usuarioRepository.findByEmail(request.getEmail())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new RuntimeException("El email ya está en uso por otro usuario");
                });

        u.setNombre(request.getNombre());
        u.setFechaNacimiento(request.getFechaNacimiento());
        u.setAvatarUrl(request.getAvatarUrl());
        u.setActualizadoEn(OffsetDateTime.now());

        return usuarioRepository.save(u);
    }

    @Override
    public void delete(String id) {
        usuarioRepository.deleteById(id);
    }
}
