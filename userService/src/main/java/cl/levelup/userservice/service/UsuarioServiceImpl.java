package cl.levelup.userservice.service;

import cl.levelup.userservice.model.Usuario;
import cl.levelup.userservice.model.dto.UsuarioPublicRequest;
import cl.levelup.userservice.model.dto.UsuarioRequest;
import cl.levelup.userservice.model.dto.UsuarioResponse;
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
    public List<UsuarioResponse> findAll() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse findById(String id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return toResponse(usuario);
    }

    @Override
    public UsuarioResponse createFromRequest(UsuarioRequest request, String uidFirebase) {
        if (usuarioRepository.existsById(uidFirebase)) {
            Usuario existente = usuarioRepository.findById(uidFirebase)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            return toResponse(existente);
        }

        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está en uso");
        }

        Usuario usuario = new Usuario();
        usuario.setId(uidFirebase);
        usuario.setEmail(request.getEmail());
        usuario.setNombre(request.getNombre());
        usuario.setFechaNacimiento(request.getFechaNacimiento());
        usuario.setAvatarUrl(request.getAvatarUrl());
        usuario.setActivo(true);
        usuario.setRol("USER");
        usuario.setCreadoEn(OffsetDateTime.now());
        usuario.setActualizadoEn(OffsetDateTime.now());

        Usuario guardado = usuarioRepository.save(usuario);
        return toResponse(guardado);
    }

    @Override
    public UsuarioResponse update(String id, UsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuarioRepository.findByEmail(request.getEmail())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new RuntimeException("El email ya está en uso por otro usuario");
                });

        usuario.setEmail(request.getEmail());
        usuario.setNombre(request.getNombre());
        usuario.setFechaNacimiento(request.getFechaNacimiento());
        usuario.setAvatarUrl(request.getAvatarUrl());
        usuario.setRol(request.getRol());
        usuario.setActivo(request.getActivo());
        usuario.setActualizadoEn(OffsetDateTime.now());

        Usuario actualizado = usuarioRepository.save(usuario);
        return toResponse(actualizado);
    }

    @Override
    public void delete(String id) {
        usuarioRepository.deleteById(id);
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        UsuarioResponse response = new UsuarioResponse();
        response.setId(usuario.getId());
        response.setEmail(usuario.getEmail());
        response.setNombre(usuario.getNombre());
        response.setFechaNacimiento(usuario.getFechaNacimiento());
        response.setAvatarUrl(usuario.getAvatarUrl());
        response.setActivo(usuario.isActivo());
        response.setRol(usuario.getRol());
        response.setCreadoEn(usuario.getCreadoEn());
        response.setActualizadoEn(usuario.getActualizadoEn());
        return response;
    }

    @Override
    public UsuarioResponse createFromPublicRequest(UsuarioPublicRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está en uso");
        }

        if (usuarioRepository.existsById(request.getFirebaseUid())) {
            throw new RuntimeException("El usuario ya existe en la base de datos");
        }

        Usuario usuario = new Usuario();
        usuario.setId(request.getFirebaseUid());
        usuario.setEmail(request.getEmail());
        usuario.setNombre(request.getNombre());
        usuario.setFechaNacimiento(request.getFechaNacimiento());
        usuario.setAvatarUrl(request.getAvatarUrl() != null ? request.getAvatarUrl() : "");
        usuario.setActivo(true);
        usuario.setRol(request.getRol() != null ? request.getRol() : "USER");
        usuario.setCreadoEn(OffsetDateTime.now());
        usuario.setActualizadoEn(OffsetDateTime.now());

        Usuario guardado = usuarioRepository.save(usuario);
        return toResponse(guardado);
    }

    @Override
    public UsuarioResponse actualizarAvatar(String id, String avatarUrl) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setAvatarUrl(avatarUrl);
        usuario.setActualizadoEn(OffsetDateTime.now());

        Usuario guardado = usuarioRepository.save(usuario);
        return toResponse(guardado);
    }
}
