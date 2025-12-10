package team.projectpulse.system;

import team.projectpulse.user.userinvitation.UserInvitation;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${front-end.url}")
    private String frontendUrl;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendInvitationEmail(UserInvitation userInvitation) {
        try {
            // Send email to the email address inviting the user to register
            MimeMessage message = this.javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(this.fromEmail);
            helper.setTo(userInvitation.getEmail());
            
            String registrationLink;
            String subject;
            String welcomeMessage;
            String ctaText;

            if (userInvitation.getRole().equals("student")) {
                subject = "Invitation to join Project Pulse as a Student";
                welcomeMessage = "You have been invited to join Project Pulse as a <strong>Student</strong>.";
                ctaText = "Register Now";
                registrationLink = this.frontendUrl + "/register?email=" + userInvitation.getEmail() + "&token=" + userInvitation.getToken() + "&courseId=" + userInvitation.getCourseId() + "&sectionId=" + userInvitation.getSectionId() + "&role=" + userInvitation.getRole();
            } else if (userInvitation.getRole().equals("instructor")) {
                subject = "You've been invited to co-manage a section on Project Pulse";
                welcomeMessage = "You have been invited to join Project Pulse as an <strong>Instructor</strong>.";
                ctaText = "Accept Invitation";
                // Include sectionId for instructor invitations if provided
                if (userInvitation.getSectionId() != null) {
                    registrationLink = this.frontendUrl + "/register?email=" + userInvitation.getEmail() + "&token=" + userInvitation.getToken() + "&courseId=" + userInvitation.getCourseId() + "&sectionId=" + userInvitation.getSectionId() + "&role=" + userInvitation.getRole();
                } else {
                    registrationLink = this.frontendUrl + "/register?email=" + userInvitation.getEmail() + "&token=" + userInvitation.getToken() + "&courseId=" + userInvitation.getCourseId() + "&role=" + userInvitation.getRole();
                }
            } else {
                subject = "Invitation to register";
                welcomeMessage = "You have been invited to register at our web application.";
                ctaText = "Register Now";
                registrationLink = this.frontendUrl + "/register?email=" + userInvitation.getEmail() + "&token=" + userInvitation.getToken() + "&courseId=" + userInvitation.getCourseId() + "&role=" + userInvitation.getRole();
            }

            helper.setSubject(subject);

            String htmlContent = "<html>" +
                    "<head>" +
                    "<style>" +
                    "body { font-family: Arial, sans-serif; color: #333; }" +
                    ".container { width: 80%; margin: 0 auto; padding: 20px; background-color: #f9f9f9; border-radius: 10px; box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1); }" +
                    "h1 { color: #0056b3; }" +
                    "p { font-size: 16px; line-height: 1.6; }" +
                    "a { display: inline-block; margin-top: 20px; padding: 10px 20px; background-color: #0056b3; color: #fff; text-decoration: none; border-radius: 5px; }" +
                    "a:hover { background-color: #003d7a; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<div class='container'>" +
                    "<h1>Welcome!</h1>" +
                    "<p>" + welcomeMessage + "</p>" +
                    "<p>Please click the link below to accept and register:</p>" +
                    "<a href='" + registrationLink + "'>" + ctaText + "</a>" +
                    "</div>" +
                    "</body>" +
                    "</html>";
            helper.setText(htmlContent, true); // true indicates HTML content
            this.javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendPasswordResetEmail(String toEmail, String forgetPasswordToken) {
        try {
            MimeMessage message = this.javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(this.fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request");

            // Password reset link
            String resetLink = this.frontendUrl + "/reset-password?email=" + toEmail + "&token=" + forgetPasswordToken;

            // HTML email content for password reset
            String htmlContent = "<html>" +
                    "<head>" +
                    "<style>" +
                    "body { font-family: Arial, sans-serif; color: #333; }" +
                    ".container { width: 80%; margin: 0 auto; padding: 20px; background-color: #f9f9f9; border-radius: 10px; box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1); }" +
                    "h1 { color: #0056b3; }" +
                    "p { font-size: 16px; line-height: 1.6; }" +
                    "a { display: inline-block; margin-top: 20px; padding: 10px 20px; background-color: #0056b3; color: #fff; text-decoration: none; border-radius: 5px; }" +
                    "a:hover { background-color: #003d7a; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<div class='container'>" +
                    "<h1>Password Reset Request</h1>" +
                    "<p>We received a request to reset your password.</p>" +
                    "<p>If you did not make this request, please ignore this email. Otherwise, click the link below to reset your password:</p>" +
                    "<a href='" + resetLink + "'>Reset Password</a>" +
                    "</div>" +
                    "</body>" +
                    "</html>";
            helper.setText(htmlContent, true);
            this.javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    public void sendReminderEmail(String toEmail, String subject, String body) {
        try {
            MimeMessage message = this.javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(this.fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);

            // HTML email content for password reset
            String htmlContent = "<html>" +
                    "<head>" +
                    "<style>" +
                    "body { font-family: Arial, sans-serif; color: #333; }" +
                    ".container { width: 80%; margin: 0 auto; padding: 20px; background-color: #f9f9f9; border-radius: 10px; box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1); }" +
                    "h1 { color: #0056b3; }" +
                    "p { font-size: 16px; line-height: 1.6; }" +
                    "a { display: inline-block; margin-top: 20px; padding: 10px 20px; background-color: #0056b3; color: #fff; text-decoration: none; border-radius: 5px; }" +
                    "a:hover { background-color: #003d7a; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<div class='container'>" +
                    "<h1>" + subject + "</h1>" +
                    "<p>" + body + "</p>" +
                    "</div>" +
                    "</body>" +
                    "</html>";
            helper.setText(htmlContent, true);
            this.javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send reminder email", e);
        }
    }

    public void sendPeerEvaluationConfirmationEmail(String toEmail, String studentName, String sectionName, String week) {
        try {
            MimeMessage message = this.javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(this.fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Peer Evaluation Submission Confirmation");

            String htmlContent = "<html>" +
                    "<head>" +
                    "<style>" +
                    "body { font-family: Arial, sans-serif; color: #333; }" +
                    ".container { width: 80%; margin: 0 auto; padding: 20px; background-color: #f9f9f9; border-radius: 10px; box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1); }" +
                    "h1 { color: #0056b3; }" +
                    "p { font-size: 16px; line-height: 1.6; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<div class='container'>" +
                    "<h1>Peer Evaluation Submission Confirmation</h1>" +
                    "<p>Hello " + studentName + ",</p>" +
                    "<p>This email confirms that you successfully submitted your peer evaluation for section <strong>" + sectionName + "</strong>.</p>" +
                    "<p>Week: <strong>" + week + "</strong><br/>" +
                    "<p>You may keep this message as your record of submission.</p>" +
                    "<p>Regards,<br/>Project Pulse Team</p>" +
                    "</div>" +
                    "</body>" +
                    "</html>";
            helper.setText(htmlContent, true);
            this.javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send peer evaluation confirmation email", e);
        }
    }

}