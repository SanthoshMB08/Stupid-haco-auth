package stupid.hackthon.register.dto;

import java.util.List;

public record UserChatIdsResponse(
        Long userId,
        List<String> chatIds
) {
}
