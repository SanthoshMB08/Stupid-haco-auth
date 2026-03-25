package stupid.hackthon.register.dto;

import stupid.hackthon.register.domain.Gender;

public record FunLoginResponse(
        boolean success,
        boolean newUser,
        String token,
        Long userId,
        String email,
        Gender gender
) {
}
