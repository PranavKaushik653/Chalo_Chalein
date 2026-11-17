package com.chalochalein.backend.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@Slf4j
@RequiredArgsConstructor
public class AdminBootStrap implements CommandLineRunner {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    @Value("${chalo.bootstrap.admin-email:}")
    private String email;

    @Value("${chalo.bootstrap.admin-password:}")
    private String password;

    @Override
    public void run(String... args) {
        if(email.isBlank() || password.isBlank()) return;
        if (password.length() < 12) { log.warn("ADMIN_PASSWORD shorter than 12 chars, admin not created"); return; }
        String e = email.trim().toLowerCase(Locale.ROOT);
        if(users.findByEmail(e).isPresent()) return;
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(password));
        u.setFullName("Platform Admin");
        u.getRoles().add(Role.TRAVELLER);
        u.getRoles().add(Role.ADMIN);
        users.save(u);
        log.info("Bootstrap admin created: {}", e);
    }
}
