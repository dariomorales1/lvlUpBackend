package cl.levelup.authservice.service;

import cl.levelup.authservice.model.dto.AuthResponse;
import cl.levelup.authservice.model.dto.LoginRequest;
import cl.levelup.authservice.model.dto.RefreshRequest;
import cl.levelup.authservice.repository.UsuarioRepository;
import cl.levelup.authservice.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    public AuthResponse login(LoginRequest request) {
        System.out.println("Iniciando proceso de login...");

        if (request.getFirebaseIdToken() == null || request.getFirebaseIdToken().isEmpty()) {
            throw new RuntimeException("Se requiere firebaseIdToken para autenticación");
        }

        System.out.println("FirebaseIdToken recibido, procediendo con verificación...");
        return loginWithFirebase(request.getFirebaseIdToken());
    }

    private AuthResponse loginWithFirebase(String firebaseIdToken) {
        try {
            System.out.println("Verificando token de Firebase...");

            FirebaseToken decodedToken = FirebaseAuth.getInstance()
                    .verifyIdToken(firebaseIdToken);

            String firebaseUid = decodedToken.getUid();
            String email = decodedToken.getEmail();
            String name = decodedToken.getName() != null ? decodedToken.getName() : email.split("@")[0];

            System.out.println("   Token de Firebase verificado:");
            System.out.println("   UID: " + firebaseUid);
            System.out.println("   Email: " + email);
            System.out.println("   Name: " + name);

            Usuario user = usuarioRepository.findById(firebaseUid)
                    .orElseGet(() -> {
                        System.out.println("Creando nuevo usuario en base de datos...");
                        return crearNuevoUsuario(firebaseUid, email, name);
                    });

            System.out.println("Usuario procesado: " + user.getEmail() + " - Rol: " + user.getRol());

            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            System.out.println("   Tokens JWT generados exitosamente");
            System.out.println("   Access Token: " + accessToken.substring(0, 50) + "...");
            System.out.println("   Refresh Token: " + refreshToken.substring(0, 50) + "...");

            return new AuthResponse(
                    accessToken,
                    refreshToken,
                    "Bearer",
                    user.getId(),
                    user.getEmail(),
                    user.getRol()
            );

        } catch (FirebaseAuthException e) {
            System.err.println("Error verificando token Firebase: " + e.getMessage());
            throw new RuntimeException("Token de Firebase inválido: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error inesperado en login: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error en el proceso de autenticación");
        }
    }

    private Usuario crearNuevoUsuario(String firebaseUid, String email, String name) {
        Usuario newUser = new Usuario();
        newUser.setId(firebaseUid);
        newUser.setEmail(email);
        newUser.setNombre(name);
        newUser.setRol(determineUserRole(email));
        newUser.setActivo(true);
        newUser.setCreadoEn(OffsetDateTime.from(LocalDateTime.now()));
        newUser.setActualizadoEn(OffsetDateTime.from(LocalDateTime.now()));

        System.out.println("Guardando nuevo usuario en BD...");
        return usuarioRepository.save(newUser);
    }

    private String determineUserRole(String email) {
        if (email.endsWith("@levelup.ddns.net")) {
            return "ADMIN";
        }
        return "USER";
    }

    public AuthResponse refresh(RefreshRequest request) {
        System.out.println("🔄 Procesando refresh token...");

        String refreshToken = request.getRefreshToken();

        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Refresh token inválido");
        }

        String newAccessToken = jwtService.refreshAccessToken(refreshToken);

        String userId = jwtService.getUserIdFromToken(refreshToken);
        String email = jwtService.getEmailFromToken(refreshToken);
        String rol = jwtService.getRolFromToken(refreshToken);

        System.out.println("Nuevo access token generado para: " + email);

        return new AuthResponse(
                newAccessToken,
                refreshToken,
                "Bearer",
                userId,
                email,
                rol
        );
    }

    public void logout(RefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Refresh token inválido");
        }

        String userId = jwtService.getUserIdFromToken(refreshToken);
        System.out.println("🚪 Logout realizado para usuario: " + userId);

    }

    public void deleteUserFromFirebase(String userId) {
        try {
            System.out.println("🗑️ Eliminando usuario de Firebase: " + userId);

            FirebaseAuth.getInstance().deleteUser(userId);

            System.out.println("✅ Usuario eliminado de Firebase: " + userId);

        } catch (FirebaseAuthException e) {
            System.err.println("❌ Error eliminando usuario de Firebase: " + e.getMessage());

            if (e.getErrorCode().equals("user-not-found")) {
                throw new RuntimeException("Usuario no encontrado en Firebase");
            } else if (e.getErrorCode().equals("insufficient-permissions")) {
                throw new RuntimeException("Permisos insuficientes para eliminar usuario");
            } else {
                throw new RuntimeException("Error eliminando usuario de Firebase: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ Error inesperado eliminando usuario: " + e.getMessage());
            throw new RuntimeException("Error eliminando usuario: " + e.getMessage());
        }
    }
}