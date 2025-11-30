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
            System.out.println("Saltando JWT: " + path);
        } else {
            System.out.println("Aplicando JWT: " + path);
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
        System.out.println("Autorizacion header: " + header);

        if (header == null || !header.startsWith("Bearer ")) {
            System.out.println("Error o header Invalido");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Error Authorization header");
            return;
        }

        String token = header.substring(7);
        System.out.println("Token recibido: " + (token.length() > 20 ? token.substring(0, 20) + "..." : token));

        try {
            System.out.println("Validando token JWT...");

            if (!jwtService.validateToken(token)) {
                System.out.println("Token inválido o expirado");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Ivalido o expiradi JWT token");
                return;
            }

            String userId = jwtService.extractUserId(token);
            String email = jwtService.extractEmail(token);
            String rol = jwtService.extractRol(token);

            System.out.println("JWT Validado - User: " + userId + ", Email: " + email + ", Rol: " + rol);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userId, null, List.of());

            SecurityContextHolder.getContext().setAuthentication(auth);
            System.out.println("Autenticación establecida en SecurityContext");

        } catch (Exception e) {
            System.out.println("JWT Error Validacion: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token validation failed: " + e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

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