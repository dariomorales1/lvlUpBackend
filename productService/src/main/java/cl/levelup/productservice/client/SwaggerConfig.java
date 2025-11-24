// productService/src/main/java/cl/levelup/productservice/config/SwaggerConfig.java
package cl.levelup.productservice.config;

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

    @Value("${server.port:8083}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LevelUp - Product Service")
                        .version("1.0.0")
                        .description("""
                        ## 🛍️ Microservicio de Gestión de Productos
                        
                        ### **Funcionalidades principales:**
                        - ✅ Catálogo completo de productos
                        - ✅ Gestión de categorías
                        - ✅ Búsqueda y filtrado de productos
                        - ✅ Gestión de inventario
                        - ✅ Imágenes de productos
                        
                        ### **Notas importantes:**
                        - Este servicio maneja todo el catálogo de productos LevelUp
                        - Integrado con almacenamiento de imágenes
                        - Soporte para múltiples categorías y etiquetas
                        """))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Servidor de desarrollo local"),
                        new Server()
                                .url("http://levelup.ddns.net:8083")
                                .description("Servidor de producción")
                ));
    }
}
