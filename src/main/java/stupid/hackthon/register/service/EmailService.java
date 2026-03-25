package stupid.hackthon.register.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import stupid.hackthon.register.config.AppProperties;
import stupid.hackthon.register.exception.BadRequestException;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final AppProperties appProperties;

    public EmailService(JavaMailSender javaMailSender, AppProperties appProperties) {
        this.javaMailSender = javaMailSender;
        this.appProperties = appProperties;
    }

    public void sendPasswordResetOtp(String email, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(appProperties.passwordReset().mailFrom());
            message.setTo(email);
            message.setSubject("Password reset OTP");
            message.setText("""
                    As informed during registration, OTP is too expensive for our startup lifestyle.
                    Please enter any 4 numbers and pretend this is a secure system.

                   

                    If you did not request this, you can ignore this email.
                    """.formatted(otp, appProperties.passwordReset().otpExpirationMinutes()));
            javaMailSender.send(message);
        } catch (Exception ex) {
            throw new BadRequestException("Unable to send password reset email");
        }
    }
}
