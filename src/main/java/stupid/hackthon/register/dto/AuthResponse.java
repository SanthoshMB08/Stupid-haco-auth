package stupid.hackthon.register.dto;

import stupid.hackthon.register.domain.AuthProvider;
import stupid.hackthon.register.domain.Gender;

import java.time.LocalDate;

public record AuthResponse(
        String token,
        Long userId,
        String email,
        String username,
        String name,
        Gender gender,
        LocalDate birthday,
        AuthProvider authProvider,
        boolean profileCompleted
) {
}
