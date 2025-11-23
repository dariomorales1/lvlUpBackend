package cl.levelup.productservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class UsuarioClient {

    private final RestTemplate restTemplate;

    @Value("${userservice.url}")
    private String userServiceBaseUrl;

    public UsuarioClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public Optional<UsuarioResumen> obtenerUsuarioPorId(String id) {
        try {
            String url = userServiceBaseUrl + "/users/" + id;
            ResponseEntity<UsuarioResumen> resp =
                    restTemplate.getForEntity(url, UsuarioResumen.class);
            return Optional.ofNullable(resp.getBody());
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (Exception e) {

            e.printStackTrace();
            return Optional.empty();
        }
    }
}
