package com.codewithpcodes.salima;

import com.codewithpcodes.salima.auth.AuthenticationService;
import com.codewithpcodes.salima.auth.CreateAdminRequest;
import com.codewithpcodes.salima.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class SalimaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalimaApplication.class, args);
    }

    @Bean
    CommandLineRunner initAdmin(
            AuthenticationService service,
            UserRepository userRepository
    ) {
        return args -> {
            String defaultEmail = "admin@salima.com";

            if (!userRepository.existsByEmail(defaultEmail)) {
                var admin = new CreateAdminRequest(
                        "admin",
                        "pcodes",
                        "admin@salima.com",
                        "password"
                );
                System.out.println("Admin token: " + service.createAdmin(admin).getAccessToken());
            } else {
                System.out.println("Default ADMIN exists already. Skipping creation...");
            }
        };
    }
}
