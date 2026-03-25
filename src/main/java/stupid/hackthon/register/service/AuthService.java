package stupid.hackthon.register.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import stupid.hackthon.register.domain.AuthProvider;
import stupid.hackthon.register.domain.User;
import stupid.hackthon.register.config.AppProperties;
import stupid.hackthon.register.dto.AuthResponse;

import stupid.hackthon.register.dto.LoginRequest;
import stupid.hackthon.register.dto.MessageResponse;
import stupid.hackthon.register.dto.OtpVerificationResponse;
import stupid.hackthon.register.dto.PasswordResetOtpRequest;
import stupid.hackthon.register.dto.PasswordResetOtpVerifyRequest;
import stupid.hackthon.register.dto.PasswordResetRequest;
import stupid.hackthon.register.dto.RegisterRequest;
import stupid.hackthon.register.dto.UpdateProfileRequest;
import stupid.hackthon.register.exception.BadRequestException;
import stupid.hackthon.register.exception.NotFoundException;
import stupid.hackthon.register.repository.UserRepository;
import stupid.hackthon.register.security.AppUserPrincipal;
import stupid.hackthon.register.security.JwtService;

@Service
public class AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    private final EmailService emailService;
    private final AppProperties appProperties;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            
            EmailService emailService,
            AppProperties appProperties
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
       
        this.emailService = emailService;
        this.appProperties = appProperties;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        ensureEmailAvailable(request.email());
        ensureUsernameAvailable(request.username(), null);

        User user = new User();
        user.setEmail(request.email());
        user.setUsername(request.username());
        user.setName(request.name());
        user.setGender(request.gender());
        user.setBirthday(request.birthday());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setProfileCompleted(true);

        return toAuthResponse(userRepository.save(user));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password())
        );
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            throw new BadRequestException("This account uses Google login");
        }
        return toAuthResponse(user);
    }

    

    @Transactional
    public AuthResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        ensureUsernameAvailable(request.username(), user.getId());

        user.setUsername(request.username());
        user.setName(request.name());
        user.setGender(request.gender());
        user.setBirthday(request.birthday());
        user.setProfileCompleted(true);

        return toAuthResponse(userRepository.save(user));
    }

    @Transactional
    public MessageResponse sendPasswordResetOtp(PasswordResetOtpRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new NotFoundException("No user found for this email"));
        ensureLocalPasswordAccount(user);

        String otp = generateOtp();
        user.setPasswordResetOtpHash(passwordEncoder.encode(otp));
        user.setPasswordResetOtpExpiresAt(Instant.now().plus(appProperties.passwordReset().otpExpirationMinutes(), ChronoUnit.MINUTES));
        user.setPasswordResetVerifiedAt(null);
        userRepository.save(user);

        emailService.sendPasswordResetOtp(user.getEmail(), otp);
        return new MessageResponse("Password reset OTP sent to email");
    }

    @Transactional
    public OtpVerificationResponse verifyPasswordResetOtp(PasswordResetOtpVerifyRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new NotFoundException("No user found for this email"));
        ensureLocalPasswordAccount(user);
        validateOtp(user, request.otp());

        user.setPasswordResetVerifiedAt(Instant.now());
        userRepository.save(user);
        return new OtpVerificationResponse(user.getEmail(), true, "OTP verified successfully");
    }

    @Transactional
    public MessageResponse resetPassword(PasswordResetRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new NotFoundException("No user found for this email"));
        ensureLocalPasswordAccount(user);

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        clearPasswordResetState(user);
        userRepository.save(user);
        return new MessageResponse("Password reset successful");
    }

    public boolean isUsernameUnique(String username) {
        return !userRepository.existsByUsernameIgnoreCase(username.trim().toLowerCase());
    }

    public String findUsernameByEmail(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("No user found for this email"));
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new NotFoundException("Username is not set for this account yet");
        }
        return user.getUsername();
    }

    private void ensureEmailAvailable(String email) {
        if (userRepository.existsByEmailIgnoreCase(email.trim().toLowerCase())) {
            throw new BadRequestException("Email is already registered");
        }
    }

    private void ensureUsernameAvailable(String username, Long ignoredUserId) {
        String normalizedUsername = username.trim().toLowerCase();
        boolean taken = ignoredUserId == null
                ? userRepository.existsByUsernameIgnoreCase(normalizedUsername)
                : userRepository.existsByUsernameIgnoreCaseAndIdNot(normalizedUsername, ignoredUserId);
        if (taken) {
            throw new BadRequestException("Username is already taken");
        }
    }

    private void ensureLocalPasswordAccount(User user) {
        if (user.getAuthProvider() != AuthProvider.LOCAL || user.getPasswordHash() == null) {
            throw new BadRequestException("Password reset is available only for password-based accounts");
        }
    }

    private void validateOtp(User user, String otp) {
        if (user.getPasswordResetOtpHash() == null || user.getPasswordResetOtpExpiresAt() == null) {
            throw new BadRequestException("No active password reset OTP found");
        }
        if (user.getPasswordResetOtpExpiresAt().isBefore(Instant.now())) {
            clearPasswordResetState(user);
            userRepository.save(user);
            throw new BadRequestException("OTP has expired");
        }
        if (!passwordEncoder.matches(otp, user.getPasswordResetOtpHash())) {
            throw new BadRequestException("Invalid OTP");
        }
    }

    private void clearPasswordResetState(User user) {
        user.setPasswordResetOtpHash(null);
        user.setPasswordResetOtpExpiresAt(null);
        user.setPasswordResetVerifiedAt(null);
    }

    private String generateOtp() {
        int otp = 100000 + SECURE_RANDOM.nextInt(900000);
        return Integer.toString(otp);
    }

    private User mergeGoogleIdentity(User user, String subject, String fullName) {
        if (user.getAuthProvider() == AuthProvider.LOCAL && user.getPasswordHash() != null) {
            throw new BadRequestException("This email is already registered with password login");
        }
        if (user.getGoogleSubject() != null && !subject.equals(user.getGoogleSubject())) {
            throw new BadRequestException("Email is already linked to another Google account");
        }
        user.setGoogleSubject(subject);
        if ((user.getName() == null || user.getName().isBlank()) && fullName != null && !fullName.isBlank()) {
            user.setName(fullName);
        }
        user.setAuthProvider(AuthProvider.GOOGLE);
        return user;
    }

    private User createGoogleUser(String email, String subject, String fullName) {
        User user = new User();
        user.setEmail(email);
        user.setName(fullName);
        user.setAuthProvider(AuthProvider.GOOGLE);
        user.setGoogleSubject(subject);
        user.setProfileCompleted(false);
        return user;
    }

    private AuthResponse toAuthResponse(User user) {
        AppUserPrincipal principal = new AppUserPrincipal(user);
        return new AuthResponse(
                jwtService.generateToken(principal),
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getName(),
                user.getGender(),
                user.getBirthday(),
                user.getAuthProvider(),
                user.isProfileCompleted()
        );
    }
}
