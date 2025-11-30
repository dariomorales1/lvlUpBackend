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
                System.out.println("No existe archivo .env - usando valores por defecto");
                return;
            }

            Map<String, String> envVars = new HashMap<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();

                    if (line.isEmpty() || line.startsWith("#")) continue;
                    if (!line.contains("=")) continue;

                    String[] parts = line.split("=", 2);
                    String key = parts[0].trim();
                    String value = parts[1].trim();

                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }

                    envVars.put(key, value);
                    System.out.println("ENV cargado: " + key + " = " + value);
                }
            }

            for (Map.Entry<String, String> entry : envVars.entrySet()) {
                System.setProperty(entry.getKey(), entry.getValue());
            }

            System.out.println("Archivo .env cargado exitosamente");

        } catch (Exception e) {
            System.err.println("Error cargando .env: " + e.getMessage());
        }
    }
}