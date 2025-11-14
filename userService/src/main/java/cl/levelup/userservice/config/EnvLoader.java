package cl.levelup.userservice.config;

import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class EnvLoader {

    @PostConstruct
    public void loadEnv() {
        try {
            File envFile = new File(".env");
            if (!envFile.exists()) {
                System.out.println("⚠ No existe archivo .env");
                return;
            }

            Map<String, String> envVars = new HashMap<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.contains("=")) continue;
                    if (line.trim().startsWith("#")) continue;

                    String[] parts = line.split("=", 2);
                    envVars.put(parts[0].trim(), parts[1].trim());
                }
            }

            for (Map.Entry<String, String> entry : envVars.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                System.setProperty(key, value);
                System.out.println("🔧 ENV " + key + " cargado desde .env");
            }

            System.out.println("FIREBASE_SERVICE_ACCOUNT = " + System.getProperty("FIREBASE_SERVICE_ACCOUNT"));

        } catch (Exception e) {
            System.out.println("Error cargando .env: " + e.getMessage());
        }
    }
}
