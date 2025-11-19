package cl.levelup.userservice.controller;

import cl.levelup.userservice.model.dto.UsuarioPublicRequest;
import cl.levelup.userservice.model.dto.UsuarioRequest;
import cl.levelup.userservice.model.dto.UsuarioResponse;
import cl.levelup.userservice.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import cl.levelup.userservice.model.dto.DireccionRequest;
import cl.levelup.userservice.model.dto.DireccionResponse;
import cl.levelup.userservice.service.DireccionService;
import cl.levelup.userservice.storage.SupabaseStorageService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestPart;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final DireccionService direccionService;
    private final SupabaseStorageService supabaseStorageService;

    public UsuarioController(
            UsuarioService usuarioService,
            DireccionService direccionService,
            SupabaseStorageService supabaseStorageService
    ) {
        this.usuarioService = usuarioService;
        this.direccionService = direccionService;
        this.supabaseStorageService = supabaseStorageService;
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

    @PostMapping(
            path = "/{id}/avatar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<UsuarioResponse> uploadAvatar(
            @PathVariable("id") String id,
            @RequestPart("file") MultipartFile file,
            Authentication auth
    ) {

        try {
            // 1) Subir archivo a Supabase Storage -> URL pública
            String publicUrl = supabaseStorageService.uploadAvatar(id, file);

            // 2) Actualizar avatarUrl del usuario en la BD
            UsuarioResponse actualizado = usuarioService.actualizarAvatar(id, publicUrl);

            return ResponseEntity.ok(actualizado);
        } catch (Exception e) {
            throw new RuntimeException("Error al subir avatar: " + e.getMessage(), e);
        }
    }

    // ========= NUEVO: eliminar avatar del usuario =========
    @DeleteMapping("/{id}/avatar")
    public ResponseEntity<UsuarioResponse> deleteAvatar(
            @PathVariable("id") String id,
            Authentication auth
    ) {
        try {
            // Obtener usuario para conocer la URL actual del avatar
            UsuarioResponse usuario = usuarioService.findById(id);
            String currentAvatarUrl = usuario.getAvatarUrl();

            // Eliminar de Supabase si existe
            if (currentAvatarUrl != null && !currentAvatarUrl.isBlank()) {
                supabaseStorageService.deleteByPublicUrl(currentAvatarUrl);
            }

            // Limpiar avatarUrl en la BD (dejarlo en null o vacío)
            UsuarioResponse actualizado = usuarioService.actualizarAvatar(id, null);

            return ResponseEntity.ok(actualizado);
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar avatar: " + e.getMessage(), e);
        }
    }
    // ======================================================

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
