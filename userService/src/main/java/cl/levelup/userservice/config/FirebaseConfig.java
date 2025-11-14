package cl.levelup.userservice.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initFirebase() {
        try {
            // Si ya está inicializado, no hacemos nada
            if (!FirebaseApp.getApps().isEmpty()) {
                System.out.println("Firebase ya estaba inicializado.");
                return;
            }

            String credentialsPath = System.getProperty("FIREBASE_SERVICE_ACCOUNT");
            System.out.println("DEBUG FIREBASE_SERVICE_ACCOUNT=" + credentialsPath);

            if (credentialsPath == null || credentialsPath.isBlank()) {
                System.out.println("WARN: FIREBASE_SERVICE_ACCOUNT no está definido. Firebase NO se inicializa.");
                return;
            }

            FileInputStream serviceAccount = new FileInputStream(credentialsPath);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            System.out.println("INFO: FirebaseApp inicializado correctamente.");

        } catch (IOException e) {
            System.out.println("WARN: No se pudo inicializar Firebase: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("WARN: Error inesperado inicializando Firebase: " + e.getMessage());
        }
    }
}
