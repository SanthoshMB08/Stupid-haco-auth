package stupid.hackthon.register.dto;

public record UsernameAvailabilityResponse(
        String username,
        boolean unique
) {
}
