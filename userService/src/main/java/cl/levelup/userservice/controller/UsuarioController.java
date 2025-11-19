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
import cl.levelup.userservice.model.dto.DireccionRequest;
import cl.levelup.userservice.model.dto.DireccionResponse;
import cl.levelup.userservice.service.DireccionService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final DireccionService direccionService;

    public UsuarioController(UsuarioService usuarioService, DireccionService direccionService) {
        this.usuarioService = usuarioService;
        this.direccionService = direccionService;
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

    // Listar direcciones del usuario actual
    @GetMapping("/me/direcciones")
    public List<DireccionResponse> listarDirecciones(Authentication auth) {
        String uid = (String) auth.getPrincipal();
        return direccionService.listarPorUsuario(uid);
    }

    // Crear nueva dirección usuario actual
    @PostMapping("/me/direcciones")
    public ResponseEntity<DireccionResponse> crearDireccion(
            @Valid @RequestBody DireccionRequest request,
            Authentication auth
    ) {
        String uid = (String) auth.getPrincipal();
        DireccionResponse creado = direccionService.crearParaUsuario(uid, request);
        return ResponseEntity
                .created(URI.create("/users/me/direcciones/" + creado.getId()))
                .body(creado);
    }

    // Actualizar dirección (solo si pertenece al usuario actual)
    @PutMapping("/me/direcciones/{id}")
    public DireccionResponse actualizarDireccion(
            @PathVariable("id") Long id,
            @Valid @RequestBody DireccionRequest request,
            Authentication auth
    ) {
        String uid = (String) auth.getPrincipal();
        return direccionService.actualizarParaUsuario(uid, id, request);
    }

    // Eliminar dirección del usuario actual
    @DeleteMapping("/me/direcciones/{id}")
    public ResponseEntity<Void> eliminarDireccion(
            @PathVariable("id") Long id,
            Authentication auth
    ) {
        String uid = (String) auth.getPrincipal();
        direccionService.eliminarParaUsuario(uid, id);
        return ResponseEntity.noContent().build();
    }

}