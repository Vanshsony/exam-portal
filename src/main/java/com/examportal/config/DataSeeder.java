package com.examportal.config;

import com.examportal.entity.User;
import com.examportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .fullName("Administrator")
                .email("admin@examportal.com")
                .role(User.Role.ADMIN)
                .enabled(true)
                .build();
            userRepository.save(admin);
            System.out.println("✅ Default admin created — username: admin, password: admin123");
        }
    }
}
