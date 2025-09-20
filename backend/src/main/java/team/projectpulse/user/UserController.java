package team.projectpulse.user;

import org.springframework.security.core.userdetails.UserDetails;
import team.projectpulse.system.Result;
import team.projectpulse.system.StatusCode;
import team.projectpulse.user.resetpassword.ResetPasswordService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("${api.endpoint.base-url}/users")
public class UserController {

    private final ResetPasswordService resetPasswordService;
    private final UserService userService;


    public UserController(ResetPasswordService resetPasswordService, UserService userService) {
        this.resetPasswordService = resetPasswordService;
        this.userService = userService;
    }

    // Email the user with a link to reset the password
    @PostMapping("/forget-password/{email}")
    public Result sendResetPasswordLink(@PathVariable String email) {
        this.resetPasswordService.sendResetPasswordLink(email);
        return new Result(true, StatusCode.SUCCESS, "Password reset email sent successfully");
    }

    // Reset the password for the user
    @PatchMapping("/reset-password")
    public Result resetPassword(@RequestBody Map<String, String> resetPasswordInfo) {
        this.userService.resetPassword(resetPasswordInfo);
        return new Result(true, StatusCode.SUCCESS, "Password reset successfully");
    }

    // Check if the email is already registered
    @GetMapping("/exists/{email}")
    public Result checkEmailExists(@PathVariable String email) {
        boolean exists = this.userService.checkEmailExists(email);
        return new Result(true, StatusCode.SUCCESS, "", exists);
    }

}
