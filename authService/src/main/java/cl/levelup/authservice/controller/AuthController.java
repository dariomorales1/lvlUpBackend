package cl.levelup.authservice.controller;

import cl.levelup.authservice.model.dto.AuthResponse;
import cl.levelup.authservice.model.dto.LoginRequest;
import cl.levelup.authservice.model.dto.RefreshRequest;
import cl.levelup.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "APIs para autenticación, gestión de tokens y administración")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica un usuario con email y password, retorna access token y refresh token"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login exitoso",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de login inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciales incorrectas"
            )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Parameter(description = "Credenciales de acceso", required = true)
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Refrescar token",
            description = "Obtiene un nuevo access token usando un refresh token válido"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refrescado exitosamente",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Refresh token inválido o expirado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Parameter(description = "Solicitud con refresh token", required = true)
            @RequestBody RefreshRequest request) {
        AuthResponse response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Cerrar sesión",
            description = "Invalida el refresh token para cerrar la sesión del usuario"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Logout exitoso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Refresh token inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Parameter(
                    description = "Refresh token a invalidar (opcional)",
                    required = false,
                    content = @Content(schema = @Schema(implementation = RefreshRequest.class))
            )
            @RequestBody(required = false) RefreshRequest request) {
        if (request != null && request.getRefreshToken() != null) {
            authService.logout(request);
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Eliminar usuario de Firebase",
            description = "Endpoint administrativo para eliminar un usuario de Firebase Authentication"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Usuario eliminado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID de usuario no proporcionado o inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado en Firebase"
            )
    })
    @DeleteMapping("/admin/delete-user")
    public ResponseEntity<Void> deleteUserFromFirebase(
            @Parameter(
                    description = "Mapa con el ID del usuario a eliminar",
                    required = true,
                    example = "{\"userId\": \"user-123\"}"
            )
            @RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        if (userId == null || userId.isEmpty()) {
            throw new RuntimeException("Se requiere el ID del usuario");
        }

        authService.deleteUserFromFirebase(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(hidden = true)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @Schema(description = "Respuesta de error para operaciones fallidas")
    public static class ErrorResponse {

        @Schema(
                description = "Mensaje descriptivo del error",
                example = "Credenciales inválidas"
        )
        private String message;

        public ErrorResponse() { }

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}