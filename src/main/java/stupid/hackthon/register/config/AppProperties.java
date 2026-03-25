package stupid.hackthon.register.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Jwt jwt,
        PasswordReset passwordReset
) {

    public record Jwt(
            String secret,
            long expirationMinutes
    ) {
    }

    public record PasswordReset(
            long otpExpirationMinutes,
            long otpVerifiedWindowMinutes,
            String mailFrom
    ) {
    }
}
