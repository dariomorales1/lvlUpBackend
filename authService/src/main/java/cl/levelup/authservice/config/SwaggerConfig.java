package cl.levelup.authservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8081}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("LevelUp - Auth Service")
                        .version("1.0.0")
                        .description("""
                        ## 🔐 Microservicio de Autenticación y Autorización
                        
                        ### **Funcionalidades principales:**
                        - ✅ Registro de usuarios
                        - ✅ Login con email/password
                        - ✅ Login con Firebase
                        - ✅ Generación de JWT tokens
                        - ✅ Refresh tokens
                        - ✅ Validación de tokens
                        - ✅ Logout
                        
                        ### **Flujo de autenticación:**
                        1. **Login** → Obtienes accessToken y refreshToken
                        2. **Usar accessToken** en header: `Authorization: Bearer <token>`
                        3. **Token expirado?** → Usar refreshToken para obtener nuevo accessToken
                        """)
                        .contact(new Contact()
                                .name("LevelUp Development Team")
                                .email("dev@levelup.cl")
                                .url("https://levelup.cl"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Servidor de desarrollo local"),
                        new Server()
                                .url("http://levelup.ddns.net:8081")
                                .description("Servidor de producción")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Ingresa el JWT token en el formato: Bearer <token>")));
    }
}