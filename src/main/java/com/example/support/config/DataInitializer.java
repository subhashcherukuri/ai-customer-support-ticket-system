package com.example.support.config;

import com.example.support.entity.User;
import com.example.support.enums.Role;
import com.example.support.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        if (!userRepository.existsByEmail("admin@support.com")) {

            User admin = User.builder()
                    .name("System Admin")
                    .email("admin@support.com")
                    .password(passwordEncoder.encode("216257subbu"))
                    .role(Role.ADMIN)
                    .build();

            userRepository.save(admin);

            System.out.println("Default Admin created successfully!");
        }
    }
}