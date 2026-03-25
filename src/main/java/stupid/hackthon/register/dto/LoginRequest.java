package stupid.hackthon.register.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import stupid.hackthon.register.domain.Gender;

public record LoginRequest(
        @NotBlank @Email String email,
        @NotNull Gender gender,
        @NotBlank String password
) {
}
