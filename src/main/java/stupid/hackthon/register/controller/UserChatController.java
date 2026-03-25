package stupid.hackthon.register.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stupid.hackthon.register.dto.AddChatIdRequest;
import stupid.hackthon.register.dto.MessageResponse;
import stupid.hackthon.register.dto.UserChatIdsResponse;
import stupid.hackthon.register.security.AppUserPrincipal;
import stupid.hackthon.register.service.UserChatService;

@RestController
@RequestMapping("/api/user-chats")
public class UserChatController {

    private final UserChatService userChatService;

    public UserChatController(UserChatService userChatService) {
        this.userChatService = userChatService;
    }

    @PostMapping("/me")
    public MessageResponse addChatId(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @Valid @RequestBody AddChatIdRequest request
    ) {
        return userChatService.addChatId(principal.getId(), request);
    }

    @GetMapping("/me")
    public UserChatIdsResponse getChatIds(@AuthenticationPrincipal AppUserPrincipal principal) {
        return userChatService.getChatIds(principal.getId());
    }
}
