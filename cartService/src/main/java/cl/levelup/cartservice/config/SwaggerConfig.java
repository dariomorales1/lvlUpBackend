// cartService/src/main/java/cl/levelup/cartservice/config/SwaggerConfig.java
package cl.levelup.cartservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8084}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LevelUp - Cart Service")
                        .version("1.0.0")
                        .description("""
                        ## 🛒 Microservicio de Carritos de Compras
                        
                        ### **Funcionalidades principales:**
                        - ✅ Gestión de carritos para usuarios autenticados
                        - ✅ Gestión de carritos para usuarios anónimos (guest)
                        - ✅ Migración de carritos guest a usuarios autenticados
                        - ✅ Operaciones CRUD completas para items del carrito
                        - ✅ Cálculo automático de totales
                        
                        ### **Autenticación requerida:**
                        Endpoints de usuarios autenticados requieren JWT token:
                        `Authorization: Bearer <token>`
                        
                        ### **Flujo típico:**
                        1. Usuario anónimo agrega items → Carrito guest
                        2. Usuario se registra/login → Migrar carrito
                        3. Usuario autenticado gestiona carrito permanente
                        """))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Servidor de desarrollo local"),
                        new Server()
                                .url("http://levelup.ddns.net:8084")
                                .description("Servidor de producción")
                ));

    }
}
