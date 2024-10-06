package team.projectpulse.user.resetpassword;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class ResetPasswordToken {

    @Id
    private String email;
    private String token;
    private LocalDateTime expiryDate;


    public ResetPasswordToken() {
    }

    public ResetPasswordToken(String email) {
        this.email = email;
        this.token = UUID.randomUUID().toString();
        this.expiryDate = LocalDateTime.now().plusMinutes(5); // Token valid for 5 minutes
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

}