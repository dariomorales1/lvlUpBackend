package cl.levelup.userservice.controller;

import cl.levelup.userservice.model.dto.*;
import cl.levelup.userservice.service.UsuarioService;
import cl.levelup.userservice.service.DireccionService;
import cl.levelup.userservice.storage.SupabaseStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "APIs para gestión de usuarios, avatares y direcciones")
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

    @Operation(summary = "Obtener todos los usuarios", description = "Retorna lista de todos los usuarios (requiere autenticación)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping
    public List<UsuarioResponse> getAll() {
        return usuarioService.findAll();
    }

    @Operation(summary = "Obtener usuario por ID", description = "Retorna un usuario específico por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/{id}")
    public UsuarioResponse getOne(
            @Parameter(description = "ID del usuario", example = "user-123", required = true)
            @PathVariable("id") String id) {
        return usuarioService.findById(id);
    }

    @Operation(summary = "Obtener usuario actual", description = "Retorna el perfil del usuario autenticado actualmente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil de usuario obtenido"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/me")
    public UsuarioResponse getCurrentUser(Authentication auth) {
        String uid = (String) auth.getPrincipal();
        return usuarioService.findById(uid);
    }

    @Operation(summary = "Crear usuario", description = "Crea un nuevo usuario (requiere autenticación)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "409", description = "El usuario ya existe")
    })
    @PostMapping
    public ResponseEntity<UsuarioResponse> create(
            @Parameter(description = "Datos del usuario a crear", required = true)
            @Valid @RequestBody UsuarioRequest request,
            Authentication auth) {
        String uid = (String) auth.getPrincipal();
        UsuarioResponse creado = usuarioService.createFromRequest(request, uid);

        return ResponseEntity
                .created(URI.create("/users/" + creado.getId()))
                .body(creado);
    }

    @Operation(summary = "Actualizar usuario", description = "Actualiza los datos de un usuario existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PutMapping("/{id}")
    public UsuarioResponse update(
            @Parameter(description = "ID del usuario a actualizar", required = true)
            @PathVariable("id") String id,
            @Parameter(description = "Datos actualizados del usuario", required = true)
            @Valid @RequestBody UsuarioRequest request) {
        return usuarioService.update(id, request);
    }

    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario del sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID del usuario a eliminar", required = true)
            @PathVariable("id") String id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Subir avatar",
            description = "Sube una imagen de avatar para el usuario y actualiza la URL en el perfil"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Avatar subido exitosamente"),
            @ApiResponse(responseCode = "400", description = "Archivo inválido o error en upload"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping(
            path = "/{id}/avatar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<UsuarioResponse> uploadAvatar(
            @Parameter(description = "ID del usuario", required = true)
            @PathVariable("id") String id,
            @Parameter(
                    description = "Archivo de imagen (JPG, PNG, etc.)",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestPart("file") MultipartFile file,
            Authentication auth) {

        try {
            String publicUrl = supabaseStorageService.uploadAvatar(id, file);
            UsuarioResponse actualizado = usuarioService.actualizarAvatar(id, publicUrl);
            return ResponseEntity.ok(actualizado);
        } catch (Exception e) {
            throw new RuntimeException("Error al subir avatar: " + e.getMessage(), e);
        }
    }

    @Operation(summary = "Eliminar avatar", description = "Elimina el avatar del usuario y limpia la URL")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Avatar eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @DeleteMapping("/{id}/avatar")
    public ResponseEntity<UsuarioResponse> deleteAvatar(
            @Parameter(description = "ID del usuario", required = true)
            @PathVariable("id") String id,
            Authentication auth) {
        try {
            UsuarioResponse usuario = usuarioService.findById(id);
            String currentAvatarUrl = usuario.getAvatarUrl();

            if (currentAvatarUrl != null && !currentAvatarUrl.isBlank()) {
                supabaseStorageService.deleteByPublicUrl(currentAvatarUrl);
            }

            UsuarioResponse actualizado = usuarioService.actualizarAvatar(id, null);
            return ResponseEntity.ok(actualizado);
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar avatar: " + e.getMessage(), e);
        }
    }

    @Operation(
            summary = "Registro público de usuario",
            description = "Endpoint público para registrar nuevos usuarios sin autenticación"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "El usuario ya existe")
    })
    @PostMapping("/public/register")
    public ResponseEntity<UsuarioResponse> registerPublic(
            @Parameter(description = "Datos de registro del usuario", required = true)
            @Valid @RequestBody UsuarioPublicRequest request) {
        UsuarioResponse creado = usuarioService.createFromPublicRequest(request);
        return ResponseEntity
                .created(URI.create("/users/" + creado.getId()))
                .body(creado);
    }


    @Operation(summary = "Listar direcciones", description = "Obtiene todas las direcciones del usuario actual")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de direcciones obtenida"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/me/direcciones")
    public List<DireccionResponse> listarDirecciones(Authentication auth) {
        String uid = (String) auth.getPrincipal();
        return direccionService.listarPorUsuario(uid);
    }

    @Operation(summary = "Crear dirección", description = "Crea una nueva dirección para el usuario actual")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Dirección creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping("/me/direcciones")
    public ResponseEntity<DireccionResponse> crearDireccion(
            @Parameter(description = "Datos de la dirección", required = true)
            @Valid @RequestBody DireccionRequest request,
            Authentication auth) {
        String uid = (String) auth.getPrincipal();
        DireccionResponse creado = direccionService.crearParaUsuario(uid, request);
        return ResponseEntity
                .created(URI.create("/users/me/direcciones/" + creado.getId()))
                .body(creado);
    }

    @Operation(summary = "Actualizar dirección", description = "Actualiza una dirección del usuario actual")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dirección actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Dirección no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PutMapping("/me/direcciones/{id}")
    public DireccionResponse actualizarDireccion(
            @Parameter(description = "ID de la dirección", required = true)
            @PathVariable("id") Long id,
            @Parameter(description = "Datos actualizados de la dirección", required = true)
            @Valid @RequestBody DireccionRequest request,
            Authentication auth) {
        String uid = (String) auth.getPrincipal();
        return direccionService.actualizarParaUsuario(uid, id, request);
    }

    @Operation(summary = "Eliminar dirección", description = "Elimina una dirección del usuario actual")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Dirección eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Dirección no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @DeleteMapping("/me/direcciones/{id}")
    public ResponseEntity<Void> eliminarDireccion(
            @Parameter(description = "ID de la dirección", required = true)
            @PathVariable("id") Long id,
            Authentication auth) {
        String uid = (String) auth.getPrincipal();
        direccionService.eliminarParaUsuario(uid, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(hidden = true)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @Schema(description = "Respuesta de error")
    public static class ErrorResponse {
        @Schema(description = "Mensaje de error", example = "Error al procesar la solicitud")
        private String message;

        public ErrorResponse() { }
        public ErrorResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
