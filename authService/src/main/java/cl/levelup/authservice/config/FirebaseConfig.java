package cl.levelup.authservice.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account:}")
    private String serviceAccountPath;

    @PostConstruct
    public void init() {

        System.out.println("🔥 FIREBASE_SERVICE_ACCOUNT (raw) = " + serviceAccountPath);
        System.out.println("🔥 FIREBASE_SERVICE_ACCOUNT (system) = " + System.getProperty("FIREBASE_SERVICE_ACCOUNT"));

        if (serviceAccountPath == null || serviceAccountPath.isBlank()) {
            serviceAccountPath = System.getProperty("FIREBASE_SERVICE_ACCOUNT");
            System.out.println("FIREBASE_SERVICE_ACCOUNT cargado desde System: " + serviceAccountPath);
        }

        try {
            if (FirebaseApp.getApps().isEmpty()) {

                if (serviceAccountPath == null || serviceAccountPath.isBlank()) {
                    System.out.println("FIREBASE_SERVICE_ACCOUNT no definido. Firebase NO se inicializa.");
                    return;
                }

                FileInputStream serviceAccount = new FileInputStream(serviceAccountPath);

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                System.out.println("Firebase inicializado correctamente.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error inicializando Firebase", e);
        }
    }
}
