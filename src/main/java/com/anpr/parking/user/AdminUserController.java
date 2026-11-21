package com.anpr.parking.user;

import com.anpr.parking.common.ApiException;
import com.anpr.parking.common.AuditService;
import com.anpr.parking.config.AuthUser;
import com.anpr.parking.user.UserDtos.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuditService audit;

    public AdminUserController(UserRepository users, PasswordEncoder encoder, AuditService audit) {
        this.users = users;
        this.encoder = encoder;
        this.audit = audit;
    }

    @GetMapping
    public List<UserView> list() {
        return users.findAll().stream().map(UserView::of).toList();
    }

    @PatchMapping("/{id}/active")
    public UserView setActive(@PathVariable Long id, @Valid @RequestBody ActiveRequest r,
                              @AuthenticationPrincipal AuthUser admin) {
        if (admin.id().equals(id) && !r.active()) {
            throw ApiException.badRequest("You cannot deactivate your own account");
        }
        User u = users.findById(id).orElseThrow(() -> ApiException.notFound("User not found"));
        u.setActive(r.active());
        users.save(u);
        audit.log(r.active() ? "USER_ACTIVATED" : "USER_DEACTIVATED", admin.id(), "USER", id, "OK", null);
        return UserView.of(u);
    }

    @PostMapping("/{id}/reset-password")
    public void resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest r,
                              @AuthenticationPrincipal AuthUser admin) {
        User u = users.findById(id).orElseThrow(() -> ApiException.notFound("User not found"));
        u.setPasswordHash(encoder.encode(r.newPassword()));
        users.save(u);
        audit.log("USER_PASSWORD_RESET", admin.id(), "USER", id, "OK", null);
    }
}
