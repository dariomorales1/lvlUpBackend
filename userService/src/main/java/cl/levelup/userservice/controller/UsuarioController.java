package cl.levelup.userservice.controller;

import cl.levelup.userservice.model.Usuario;
import cl.levelup.userservice.model.UsuarioRequest;
import cl.levelup.userservice.model.UsuarioResponse;
import cl.levelup.userservice.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioResponse> getAll() {
        return usuarioService.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public UsuarioResponse getOne(@PathVariable("id") String id) {
        return toResponse(usuarioService.findById(id));
    }

    @GetMapping("/me")
    public UsuarioResponse getCurrentUser(Authentication auth) {
        String uid = (String) auth.getPrincipal();
        return toResponse(usuarioService.findById(uid));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> create(
            @Valid @RequestBody UsuarioRequest request,
            Authentication auth
    ) {
        String uid = (String) auth.getPrincipal();
        Usuario creado = usuarioService.createFromRequest(request, uid);

        return ResponseEntity
                .created(URI.create("/users/" + creado.getId()))
                .body(toResponse(creado));
    }

    @PutMapping("/{id}")
    public UsuarioResponse update(
            @PathVariable("id") String id,
            @Valid @RequestBody UsuarioRequest request
    ) {
        return toResponse(usuarioService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private UsuarioResponse toResponse(Usuario u) {
        UsuarioResponse r = new UsuarioResponse();
        r.setId(u.getId());
        r.setEmail(u.getEmail());
        r.setNombre(u.getNombre());
        r.setFechaNacimiento(u.getFechaNacimiento());
        r.setAvatarUrl(u.getAvatarUrl());
        r.setActivo(u.isActivo());
        return r;
    }
}
