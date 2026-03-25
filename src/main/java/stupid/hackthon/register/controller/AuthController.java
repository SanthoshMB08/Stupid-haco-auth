package stupid.hackthon.register.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stupid.hackthon.register.dto.AuthResponse;
import stupid.hackthon.register.dto.FunLoginResponse;
import stupid.hackthon.register.dto.LoginRequest;
import stupid.hackthon.register.dto.MessageResponse;
import stupid.hackthon.register.dto.OtpVerificationResponse;
import stupid.hackthon.register.dto.PasswordResetOtpRequest;
import stupid.hackthon.register.dto.PasswordResetOtpVerifyRequest;
import stupid.hackthon.register.dto.PasswordResetRequest;
import stupid.hackthon.register.dto.RegisterRequest;
import stupid.hackthon.register.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public FunLoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }


    @PostMapping("/password-reset/request-otp")
    public MessageResponse requestPasswordResetOtp(@Valid @RequestBody PasswordResetOtpRequest request) {
        return authService.sendPasswordResetOtp(request);
    }

    @PostMapping("/password-reset/verify-otp")
    public OtpVerificationResponse verifyPasswordResetOtp(@Valid @RequestBody PasswordResetOtpVerifyRequest request) {
        return authService.verifyPasswordResetOtp(request);
    }

    @PostMapping("/password-reset/reset")
    public MessageResponse resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        return authService.resetPassword(request);
    }
}
