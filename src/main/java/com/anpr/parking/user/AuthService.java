package com.anpr.parking.user;

import com.anpr.parking.common.ApiException;
import com.anpr.parking.config.JwtService;
import com.anpr.parking.user.UserDtos.*;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public AuthResponse register(RegisterRequest r) {
        String email = r.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmail(email)) {
            throw ApiException.conflict("Email is already registered");
        }
        User u = new User();
        u.setFullName(r.fullName().trim());
        u.setEmail(email);
        u.setPhone(r.phone());
        u.setPasswordHash(encoder.encode(r.password()));
        u.setRole(User.DRIVER);
        u = users.save(u);
        return new AuthResponse(jwt.generate(u.getId(), u.getEmail(), u.getRole()), UserView.of(u));
    }

    public AuthResponse login(LoginRequest r) {
        String email = r.email().trim().toLowerCase(Locale.ROOT);
        User u = users.findByEmail(email).orElse(null);
        if (u == null || !u.isActive() || !encoder.matches(r.password(), u.getPasswordHash())) {
            throw ApiException.unauthorized("Invalid email or password");
        }
        return new AuthResponse(jwt.generate(u.getId(), u.getEmail(), u.getRole()), UserView.of(u));
    }
}
