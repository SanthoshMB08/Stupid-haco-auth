package stupid.hackthon.register.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import stupid.hackthon.register.domain.Gender;
import stupid.hackthon.register.validation.Adult;

import java.time.LocalDate;

public record UpdateProfileRequest(
        @NotBlank
        @Size(min = 3, max = 30)
        @Pattern(regexp = "^[a-zA-Z0-9._]+$", message = "username can contain only letters, numbers, dot and underscore")
        String username,
        @NotBlank @Size(max = 100) String name,
        @NotNull Gender gender,
        @NotNull @Adult LocalDate birthday
) {
}
