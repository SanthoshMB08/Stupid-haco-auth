package stupid.hackthon.register.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddChatIdRequest(
        @NotBlank
        @Size(max = 200)
        String chatId
) {
}
