package team.projectpulse.user.resetpassword;

import team.projectpulse.system.EmailService;
import team.projectpulse.system.exception.InvalidForgetPasswordTokenException;
import team.projectpulse.system.exception.ObjectNotFoundException;
import team.projectpulse.user.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ResetPasswordService {

    private final ResetPasswordTokenRepository resetPasswordTokenRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;

    public ResetPasswordService(ResetPasswordTokenRepository resetPasswordTokenRepository, EmailService emailService, UserRepository userRepository) {
        this.resetPasswordTokenRepository = resetPasswordTokenRepository;
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    public void sendResetPasswordLink(String email) {
        // Verify that the email is valid
        this.userRepository.findByEmail(email)
                .orElseThrow(() -> new ObjectNotFoundException("email", email));

        ResetPasswordToken token = new ResetPasswordToken(email);
        this.resetPasswordTokenRepository.save(token);
        this.emailService.sendPasswordResetEmail(email, token.getToken());
    }

    public void validateForgetPasswordToken(String email, String token) {
        ResetPasswordToken resetPasswordToken = this.resetPasswordTokenRepository.findById(email)
                .orElse(null);

        if (resetPasswordToken == null) {
            throw new InvalidForgetPasswordTokenException("Invalid reset password token for email: " + email);
        }

        if (!resetPasswordToken.getToken().equals(token)) {
            throw new InvalidForgetPasswordTokenException("Invalid reset password token for email: " + email);
        }

        if (resetPasswordToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            deleteForgetPasswordToken(email);
            throw new InvalidForgetPasswordTokenException("Reset password token has expired for email: " + email);
        }
    }

    public void deleteForgetPasswordToken(String email) {
        this.resetPasswordTokenRepository.deleteByEmail(email);
    }

}
