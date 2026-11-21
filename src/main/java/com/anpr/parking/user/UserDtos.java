package com.anpr.parking.user;

import jakarta.validation.constraints.*;
import java.time.Instant;

public final class UserDtos {
    private UserDtos() {}

    public record RegisterRequest(
            @NotBlank @Size(max = 120) String fullName,
            @NotBlank @Email @Size(max = 160) String email,
            @Size(max = 30) String phone,
            @NotBlank @Size(min = 8, max = 72, message = "Password must be 8-72 characters") String password) {}

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}

    public record ActiveRequest(@NotNull Boolean active) {}

    public record ResetPasswordRequest(
            @NotBlank @Size(min = 8, max = 72, message = "Password must be 8-72 characters") String newPassword) {}

    public record UserView(Long id, String fullName, String email, String phone,
                           String role, boolean active, Instant createdAt) {
        public static UserView of(User u) {
            return new UserView(u.getId(), u.getFullName(), u.getEmail(), u.getPhone(),
                    u.getRole(), u.isActive(), u.getCreatedAt());
        }
    }

    public record AuthResponse(String token, UserView user) {}
}
