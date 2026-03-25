package stupid.hackthon.register.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetOtpRequest(
        @NotBlank @Email String email
) {
}
