package stupid.hackthon.register.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import stupid.hackthon.register.dto.AuthResponse;
import stupid.hackthon.register.dto.UpdateProfileRequest;
import stupid.hackthon.register.dto.UsernameAvailabilityResponse;
import stupid.hackthon.register.dto.UsernameLookupResponse;
import stupid.hackthon.register.security.AppUserPrincipal;
import stupid.hackthon.register.service.AuthService;

@Validated
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/check-username")
    public UsernameAvailabilityResponse checkUsername(@RequestParam String username) {
        return new UsernameAvailabilityResponse(username, authService.isUsernameUnique(username));
    }

    @GetMapping("/username-by-email")
    public UsernameLookupResponse usernameByEmail(@RequestParam String email) {
        return new UsernameLookupResponse(email, authService.findUsernameByEmail(email));
    }

    @PutMapping("/me/profile")
    public AuthResponse updateProfile(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return authService.updateProfile(principal.getId(), request);
    }
}
