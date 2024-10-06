package team.projectpulse.user;

import team.projectpulse.system.exception.ObjectNotFoundException;
import team.projectpulse.user.resetpassword.ResetPasswordService;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ResetPasswordService resetPasswordService;
    private final PasswordEncoder passwordEncoder;


    public UserService(UserRepository userRepository, ResetPasswordService resetPasswordService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.resetPasswordService = resetPasswordService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.userRepository.findByUsername(username) // First, we need to find this user from database.
                .map(peerEvaluationUser -> new MyUserPrincipal(peerEvaluationUser)) // If found, wrap the returned user instance in a MyUserPrincipal instance.
                .orElseThrow(() -> new UsernameNotFoundException("username " + username + " is not found.")); // Otherwise, throw an exception.
    }

    public void resetPassword(Map<String, String> resetPasswordInfo) {
        String email = resetPasswordInfo.get("email");
        String token = resetPasswordInfo.get("token");
        String newPassword = resetPasswordInfo.get("newPassword");

        this.resetPasswordService.validateForgetPasswordToken(email, token); // First, validate the forget password token.

        // Password rule: At least 6 characters, contains at least one letter and one number.
        if (!newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$")) {
            throw new IllegalArgumentException("Password must contain at least one letter and one number, and at least 6 characters.");
        }

        // If the token is valid, update the user's password.
        this.userRepository.findByEmail(email)
                .map(oldUser -> {
                    oldUser.setPassword(this.passwordEncoder.encode(newPassword));
                    return this.userRepository.save(oldUser);
                }).orElseThrow(() -> new ObjectNotFoundException("email", email));

        this.resetPasswordService.deleteForgetPasswordToken(email); // Finally, delete the forget password token.
    }

}
