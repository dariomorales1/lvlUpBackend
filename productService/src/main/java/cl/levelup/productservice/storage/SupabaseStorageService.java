package cl.levelup.productservice.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;

@Service
public class SupabaseStorageService {

    private static final Logger log = LoggerFactory.getLogger(SupabaseStorageService.class);

    private final WebClient.Builder webClientBuilder;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-role-key}")
    private String serviceRoleKey;

    @Value("${supabase.storage.bucket}")
    private String bucket;

    @Value("${supabase.storage.product-folder}")
    private String productFolder;

    public SupabaseStorageService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public String uploadProductImage(String categoria, String nombreProducto, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo de imagen esta vacio");
        }

        if (categoria == null || categoria.isBlank()) {
            throw new IllegalArgumentException("La categoria no puede estar vacia");
        }

        if (nombreProducto == null || nombreProducto.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacio");
        }

        String sanitizedCategory = categoria.trim();

        String sanitizedName = nombreProducto.trim();
        String normalizedName = sanitizedName.replaceAll("\\s+", "");

        String fileName = normalizedName + ".jpg";

        String objectPath = String.format("%s/%s/%s",
                productFolder,
                sanitizedCategory,
                fileName
        );

        log.info(
                "Subiendo imagen de producto a Supabase. Categoria='{}', Nombre='{}', fileName='{}', path='{}'",
                sanitizedCategory, sanitizedName, fileName, objectPath
        );

        byte[] fileBytes = file.getBytes();

        WebClient client = webClientBuilder
                .baseUrl(supabaseUrl + "/storage/v1")
                .build();

        client.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/object/{bucket}/{path}")
                        .build(bucket, objectPath))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + serviceRoleKey)
                .header("x-upsert", "true")
                .contentType(MediaType.parseMediaType(
                        file.getContentType() != null ? file.getContentType() : "image/jpeg"
                ))
                .bodyValue(fileBytes)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(error -> {
                    log.error("Error subiendo imagen de producto a Supabase Storage", error);
                    return Mono.error(new RuntimeException("No se pudo subir la imagen del producto a Supabase", error));
                })
                .block();

        String publicUrl = String.format(
                "%s/storage/v1/object/public/%s/%s",
                supabaseUrl,
                bucket,
                objectPath
        );

        log.info("Imagen de producto subida correctamente. URL publica: {}", publicUrl);

        return publicUrl;
    }

    public void deleteByPublicUrl(String publicUrl) {
        if (publicUrl == null || publicUrl.isBlank()) {
            return;
        }

        try {
            URI uri = URI.create(publicUrl);
            String path = uri.getPath();

            String marker = "/storage/v1/object/public/" + bucket + "/";
            int idx = path.indexOf(marker);
            if (idx == -1) {
                log.warn("No se pudo extraer objectPath desde la URL publica: {}", publicUrl);
                return;
            }

            String objectPath = path.substring(idx + marker.length());

            log.info("Eliminando objeto de Supabase Storage. objectPath='{}'", objectPath);

            WebClient client = webClientBuilder
                    .baseUrl(supabaseUrl + "/storage/v1")
                    .build();

            client.delete()
                    .uri(uriBuilder -> uriBuilder
                            .path("/object/{bucket}/{path}")
                            .build(bucket, objectPath))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + serviceRoleKey)
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(error -> {
                        log.error("Error eliminando objeto de Supabase Storage", error);
                        return Mono.error(new RuntimeException("No se pudo eliminar la imagen del producto en Supabase", error));
                    })
                    .block();

            log.info("Objeto eliminado correctamente de Supabase Storage: {}", objectPath);

        } catch (Exception e) {
            log.error("Error procesando URL publica para eliminar en Supabase: {}", publicUrl, e);
        }
    }
}
