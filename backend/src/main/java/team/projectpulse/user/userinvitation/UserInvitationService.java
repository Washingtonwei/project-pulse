package team.projectpulse.user.userinvitation;

import team.projectpulse.system.EmailService;
import team.projectpulse.system.exception.InvalidUserInvitationException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserInvitationService {

    private final UserInvitationRepository userInvitationRepository;
    private final EmailService emailService;


    public UserInvitationService(UserInvitationRepository userInvitationRepository, EmailService emailService) {
        this.userInvitationRepository = userInvitationRepository;
        this.emailService = emailService;
    }

    public UserInvitation createUserInvitation(String email, Integer courseId, Integer sectionId, String role) {
        return new UserInvitation(email, courseId, sectionId, UUID.randomUUID().toString(), role);
    }

    public void saveUserInvitation(UserInvitation userInvitation) {
        this.userInvitationRepository.save(userInvitation);
    }

    public void validateUserInvitation(String email, String registrationToken, Integer courseId, Integer sectionId, String role) {
        UserInvitation userInvitation = this.userInvitationRepository.findById(email)
                .orElse(null);

        if (userInvitation == null) {
            throw new InvalidUserInvitationException(email + " is not invited to register. Please contact the course admin.");
        }

        if (!userInvitation.getToken().equals(registrationToken)) {
            throw new InvalidUserInvitationException("Invalid registration token for email: " + email);
        }

        // Ensure the user is trying to register with the correct role
        if (!userInvitation.getRole().equals(role)) {
            throw new InvalidUserInvitationException("You are not allowed to register as " + role + " for email: " + email);
        }

        // Ensure the user is trying to register for the correct course and section
        if (!userInvitation.getCourseId().equals(courseId) || !userInvitation.getSectionId().equals(sectionId)) {
            throw new InvalidUserInvitationException("You are not invited to register for this course or section with email: " + email);
        }
    }

    public void deleteUserInvitation(String email) {
        this.userInvitationRepository.deleteById(email);
    }

    public void sendEmailInvitations(Integer courseId, Integer sectionId, List<String> emails, String role) {
        emails.forEach(email -> {
            // Create token
            UserInvitation userInvitation = this.createUserInvitation(email, courseId, sectionId, role);
            // Save token
            this.saveUserInvitation(userInvitation);
            // Send email
            this.emailService.sendInvitationEmail(userInvitation);
        });
    }

}
