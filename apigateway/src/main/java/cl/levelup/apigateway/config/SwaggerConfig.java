package cl.levelup.apigateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LevelUp Platform - API Gateway")
                        .version("1.0.0")
                        .description("""
                        ## 🚀 Punto de entrada único para la plataforma LevelUp
                        
                        ### **Microservicios disponibles:**
                        - **Auth Service** (`/auth/**`) - Autenticación y autorización
                        - **User Service** (`/users/**`) - Gestión de usuarios
                        - **Product Service** (`/products/**`) - Catálogo de productos
                        - **Cart Service** (`/carts/**`) - Carrito de compras
                        - **Order Service** (`/orders/**`) - Procesamiento de pedidos
                        
                        ### **Autenticación:**
                        La mayoría de endpoints requieren JWT token en el header:
                        `Authorization: Bearer <token>`
                        """)
                        .contact(new Contact()
                                .name("LevelUp DevTeam")
                                .email("dev@levelup.ddns.net")
                                .url("http://levelup.ddns.net"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Servidor de desarrollo local"),
                        new Server()
                                .url("https://levelup.ddns.net:8080")
                                .description("Servidor de producción")
                ));
    }
}
