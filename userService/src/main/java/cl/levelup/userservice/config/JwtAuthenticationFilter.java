// userService/src/main/java/cl/levelup/userservice/config/JwtAuthenticationFilter.java
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
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        boolean shouldNotFilter = isPublicPath(path);

        if (shouldNotFilter) {
            System.out.println("✅ Skipping JWT filter for public path: " + path);
        } else {
            System.out.println("🔐 Applying JWT filter for path: " + path);
        }

        return shouldNotFilter;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

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
        System.out.println("🔐 Token recibido: " + (token.length() > 20 ? token.substring(0, 20) + "..." : token));

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

        } catch (Exception e) {
            System.out.println("❌ JWT validation error: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token validation failed: " + e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Verifica si el path es público (no requiere autenticación)
     */
    private boolean isPublicPath(String path) {
        return path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/webjars") ||
                path.startsWith("/swagger-resources") ||
                path.startsWith("/configuration") ||
                path.equals("/favicon.ico") ||
                path.equals("/error") ||
                path.equals("/") ||
                path.startsWith("/actuator") ||
                path.equals("/users/public/register") ||
                path.contains(".well-known") ||
                path.endsWith(".ico") ||
                path.endsWith(".css") ||
                path.endsWith(".js") ||
                path.endsWith(".png") ||
                path.endsWith(".jpg") ||
                path.endsWith(".gif") ||
                path.endsWith(".html");
    }
}