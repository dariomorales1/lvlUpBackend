package cl.levelup.authservice.service;

import cl.levelup.authservice.model.dto.AuthResponse;
import cl.levelup.authservice.model.dto.LoginRequest;
import cl.levelup.authservice.model.dto.RefreshRequest;
import cl.levelup.authservice.repository.UsuarioRepository;
import cl.levelup.authservice.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;


    @Autowired
    public AuthService(AuthenticationManager authenticationManager,
                       UsuarioRepository usuarioRepository,
                       JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        Usuario user = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                user.getId(),
                user.getEmail(),
                user.getRol()
        );
    }

    public AuthResponse refresh(RefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Refresh token inválido");
        }

        String newAccessToken = jwtService.refreshAccessToken(refreshToken);

        String userId = jwtService.getUserIdFromToken(refreshToken);
        String email = jwtService.getEmailFromToken(refreshToken);
        String rol = jwtService.getRolFromToken(refreshToken);

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

        System.out.println("Logout realizado para usuario: " +
                jwtService.getUserIdFromToken(refreshToken));
    }
}