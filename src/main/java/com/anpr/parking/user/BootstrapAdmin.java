package com.anpr.parking.user;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BootstrapAdmin implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(BootstrapAdmin.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final String email;
    private final String password;
    private final String name;

    public BootstrapAdmin(UserRepository users, PasswordEncoder encoder,
                          @Value("${app.bootstrap.admin-email:}") String email,
                          @Value("${app.bootstrap.admin-password:}") String password,
                          @Value("${app.bootstrap.admin-name:System Administrator}") String name) {
        this.users = users;
        this.encoder = encoder;
        this.email = email;
        this.password = password;
        this.name = name;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (users.existsByRole(User.ADMIN)) return;
        if (email.isBlank() || password.length() < 8) {
            log.warn("No ADMIN exists. Set ADMIN_EMAIL and ADMIN_PASSWORD (min 8 chars) and restart.");
            return;
        }
        User u = new User();
        u.setFullName(name);
        u.setEmail(email.trim().toLowerCase(Locale.ROOT));
        u.setPasswordHash(encoder.encode(password));
        u.setRole(User.ADMIN);
        users.save(u);
        log.info("Bootstrap admin created: {}", u.getEmail());
    }
}
