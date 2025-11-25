// userService/src/main/java/cl/levelup/userservice/config/SwaggerConfig.java
package cl.levelup.userservice.config;

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

    @Value("${server.port:8082}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("LevelUp - User Service")
                        .version("1.0.0")
                        .description("""
                        ## 👥 Microservicio de Gestión de Usuarios
                        
                        ### **Funcionalidades principales:**
                        - ✅ Gestión de perfiles de usuario
                        - ✅ Subida y gestión de avatares (Supabase Storage)
                        - ✅ Gestión de direcciones de usuarios
                        - ✅ Operaciones CRUD completas
                        - ✅ Registro público de usuarios
                        
                        ### **Autenticación requerida:**
                        La mayoría de endpoints requieren JWT token en el header:
                        `Authorization: Bearer <token>`
                        
                        ### **Endpoints públicos:**
                        - `POST /users/public/register` - Registro de nuevos usuarios
                        - `GET /actuator/health` - Health check
                        """))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Servidor de desarrollo local"),
                        new Server()
                                .url("http://levelup.ddns.net:8082")
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