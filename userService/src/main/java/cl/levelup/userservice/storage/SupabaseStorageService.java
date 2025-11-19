package cl.levelup.userservice.storage;

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

    @Value("${supabase.storage.avatar-folder}")
    private String avatarFolder;

    public SupabaseStorageService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    /**
     * Sube un archivo al Storage de Supabase y devuelve la URL publica.
     */
    public String uploadAvatar(String userId, MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo esta vacio");
        }

        // Obtener extension del archivo
        String originalName = file.getOriginalFilename();
        String extension = "jpg";

        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf('.') + 1);
        }

        // Ruta final dentro del bucket: fotoPerfil/{userId}/Foto_Perfil.ext
        String objectPath = String.format("%s/%s/Foto_Perfil.%s",
                avatarFolder, userId, extension
        );

        log.info("Subiendo avatar a Supabase en path: {}", objectPath);

        byte[] fileBytes = file.getBytes();

        // Cliente Web apuntando a /storage/v1
        WebClient client = webClientBuilder
                .baseUrl(supabaseUrl + "/storage/v1")
                .build();

        // POST al endpoint de Storage
        client.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/object/{bucket}/{path}")
                        .build(bucket, objectPath))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + serviceRoleKey)
                .header("x-upsert", "true") // permite sobreescribir
                .contentType(MediaType.parseMediaType(
                        file.getContentType() != null ? file.getContentType() : "image/jpeg"))
                .bodyValue(fileBytes)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(error -> {
                    log.error("Error subiendo archivo a Supabase Storage", error);
                    return Mono.error(new RuntimeException("No se pudo subir el archivo a Supabase", error));
                })
                .block();

        // Construimos la URL publica
        String publicUrl = String.format(
                "%s/storage/v1/object/public/%s/%s",
                supabaseUrl, bucket, objectPath
        );

        log.info("Avatar subido correctamente. URL publica: {}", publicUrl);

        return publicUrl;
    }

    /**
     * Elimina un avatar de Supabase Storage usando su URL publica.
     */
    public void deleteByPublicUrl(String publicUrl) {
        if (publicUrl == null || publicUrl.isBlank()) {
            return;
        }

        try {
            URI uri = URI.create(publicUrl);
            String path = uri.getPath();
            // ej: /storage/v1/object/public/levelup_files/fotoPerfil/{uid}/Foto_Perfil.jpg
            String marker = "/storage/v1/object/public/" + bucket + "/";
            int idx = path.indexOf(marker);
            if (idx == -1) {
                log.warn("No se pudo extraer objectPath desde la URL publica: {}", publicUrl);
                return;
            }

            String objectPath = path.substring(idx + marker.length()); // fotoPerfil/{uid}/Foto_Perfil.jpg

            log.info("Eliminando avatar de Supabase Storage. objectPath='{}'", objectPath);

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
                        log.error("Error eliminando avatar de Supabase Storage", error);
                        return Mono.error(new RuntimeException("No se pudo eliminar el avatar en Supabase", error));
                    })
                    .block();

            log.info("Avatar eliminado correctamente de Supabase Storage: {}", objectPath);

        } catch (Exception e) {
            log.error("Error procesando URL publica para eliminar avatar en Supabase: {}", publicUrl, e);
        }
    }
}
