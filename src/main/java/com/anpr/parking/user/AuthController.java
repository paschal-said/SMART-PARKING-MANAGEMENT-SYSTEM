package com.anpr.parking.user;

import com.anpr.parking.common.ApiException;
import com.anpr.parking.config.AuthUser;
import com.anpr.parking.user.UserDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    private final UserRepository users;

    public AuthController(AuthService auth, UserRepository users) {
        this.auth = auth;
        this.users = users;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest r) { return auth.register(r); }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest r) { return auth.login(r); }

    @PostMapping("/logout")
    public void logout() {}

    @GetMapping("/me")
    public UserView me(@AuthenticationPrincipal AuthUser me) {
        if (me == null) throw ApiException.unauthorized("Not authenticated");
        return users.findById(me.id()).map(UserView::of)
                .orElseThrow(() -> ApiException.unauthorized("Not authenticated"));
    }
}
