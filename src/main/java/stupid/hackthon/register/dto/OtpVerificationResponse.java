package stupid.hackthon.register.dto;

public record OtpVerificationResponse(
        String email,
        boolean verified,
        String message
) {
}
