package cl.levelup.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    @PostConstruct
    public void debugEnv() {
        System.out.println("🔥 FIREBASE_SERVICE_ACCOUNT = " + System.getenv("FIREBASE_SERVICE_ACCOUNT"));
    }
}