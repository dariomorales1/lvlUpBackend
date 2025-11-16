package cl.levelup.userservice.config;

import cl.levelup.userservice.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();
        System.out.println("🛣️  Request path: " + path);

        // SOLO health check es público sin auth
        if (path.equals("/actuator/health")) {
            System.out.println("✅ Endpoint público, skipping auth");
            filterChain.doFilter(request, response);
            return;
        }
        // Registro de usuario es público sin auth
        if (path.equals("/users/public/register")) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        System.out.println("🔐 Authorization header: " + header);

        // Para endpoints que requieren auth, el header es obligatorio
        if (header == null || !header.startsWith("Bearer ")) {
            System.out.println("❌ Missing or invalid Authorization header");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or invalid Authorization header");
            return;
        }

        String token = header.substring(7);
        System.out.println("🔐 Token recibido: " + token.substring(0, 20) + "...");

        try {
            System.out.println("🔐 Validando token JWT...");

            // Validar token JWT
            if (!jwtService.validateToken(token)) {
                System.out.println("❌ Token inválido o expirado");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or expired JWT token");
                return;
            }

            // Extraer información del token
            String userId = jwtService.extractUserId(token);
            String email = jwtService.extractEmail(token);
            String rol = jwtService.extractRol(token);

            System.out.println("✅ JWT Validado - User: " + userId + ", Email: " + email + ", Rol: " + rol);

            // Crear autenticación
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userId, null, List.of());

            // Establecer en contexto de seguridad
            SecurityContextHolder.getContext().setAuthentication(auth);
            System.out.println("✅ Autenticación establecida en SecurityContext");

            // Agregar información útil a los headers para debugging
            response.setHeader("X-User-ID", userId);
            response.setHeader("X-User-Rol", rol);

        } catch (Exception e) {
            System.out.println("❌ JWT validation error: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token validation failed: " + e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }
}