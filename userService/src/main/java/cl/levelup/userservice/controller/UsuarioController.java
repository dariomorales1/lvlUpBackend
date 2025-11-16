package cl.levelup.userservice.controller;

import cl.levelup.userservice.model.dto.UsuarioPublicRequest;
import cl.levelup.userservice.model.dto.UsuarioRequest;
import cl.levelup.userservice.model.dto.UsuarioResponse;
import cl.levelup.userservice.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
        return usuarioService.findAll();
    }

    @GetMapping("/{id}")
    public UsuarioResponse getOne(@PathVariable("id") String id) {
        return usuarioService.findById(id);
    }

    @GetMapping("/me")
    public UsuarioResponse getCurrentUser(Authentication auth) {
        String uid = (String) auth.getPrincipal();
        return usuarioService.findById(uid);
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> create(
            @Valid @RequestBody UsuarioRequest request,
            Authentication auth
    ) {
        String uid = (String) auth.getPrincipal();
        UsuarioResponse creado = usuarioService.createFromRequest(request, uid);

        return ResponseEntity
                .created(URI.create("/users/" + creado.getId()))
                .body(creado);
    }

    @PutMapping("/{id}")
    public UsuarioResponse update(
            @PathVariable("id") String id,
            @Valid @RequestBody UsuarioRequest request
    ) {
        return usuarioService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    public static class ErrorResponse {
        private String message;

        public ErrorResponse() { }

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    @PostMapping("/public/register")
    public ResponseEntity<UsuarioResponse> registerPublic(
            @Valid @RequestBody UsuarioPublicRequest request
    ) {
        UsuarioResponse creado = usuarioService.createFromPublicRequest(request);
        return ResponseEntity
                .created(URI.create("/users/" + creado.getId()))
                .body(creado);
    }


}