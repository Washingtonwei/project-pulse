package team.projectpulse.user.userinvitation;

import team.projectpulse.system.EmailService;
import team.projectpulse.system.exception.InvalidUserInvitationException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class UserInvitationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserInvitationService.class);

    private final UserInvitationRepository userInvitationRepository;
    private final EmailService emailService;


    public UserInvitationService(UserInvitationRepository userInvitationRepository, EmailService emailService) {
        this.userInvitationRepository = userInvitationRepository;
        this.emailService = emailService;
    }

    /**
     * Builds an invitation for an address, keeping the token of whatever invitation that address already holds.
     *
     * <p><strong>Why the token is reused.</strong> An invitation row is keyed by email, so a second invitation to
     * the same address overwrites the first. Minting a fresh token there silently invalidates the link already
     * sitting in the recipient's inbox, and re-inviting is exactly what a course admin does when a batch appears
     * to have failed: she sends the same list again and breaks every link that had in fact been delivered.
     * Reusing the token makes a re-invite idempotent, so both the old link and the new one work.
     *
     * <p>The course, course section and role are still taken from the new invitation, so re-inviting an address to
     * a different course section repoints it. The row is deleted once the account registers, so the next
     * invitation to that address mints a fresh token.
     */
    public UserInvitation createUserInvitation(String email, Integer courseId, Integer sectionId, String role) {
        String token = this.userInvitationRepository.findById(email)
                .map(UserInvitation::getToken)
                .orElseGet(() -> UUID.randomUUID().toString());
        return new UserInvitation(email, courseId, sectionId, token, role);
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

    /**
     * Invites each address, keeping one address's delivery failure from costing the others their invitation.
     *
     * <p><strong>Why the failure is caught rather than thrown.</strong> A course admin invites a whole course
     * section at once (UC-STU-invite-students step 10, "the system sends out an email to each email address"),
     * which is one SMTP round trip per address inside this one transactional method. A single undeliverable
     * address used to abort the loop, and because the exception left the method the transaction rolled back:
     * every invitation row already written was discarded, including those of students whose email had already
     * been delivered. Those students then held a registration link whose token no longer existed and were told
     * they were not invited. Catching per address keeps the damage to the address it belongs to.
     *
     * <p>An address whose email failed still keeps its invitation row, which is harmless: the row is keyed by
     * email, so a retry overwrites it with a fresh token. The summary returned here is what tells the course
     * admin which addresses to retry (extension 10a of the use case).
     *
     * <p><strong>Known limit: each email goes out before its own row is committed.</strong> This method is
     * transactional and sends inside the loop, so for a large course section the first recipient holds a
     * live-looking link for as long as the rest of the batch takes. A recipient who clicks during that window is
     * validated by a separate request that cannot see the uncommitted row, and is told she was not invited;
     * clicking again once the batch finishes works. A failure of the commit itself would strand those links for
     * good. Both want the same fix, a transaction per address or a send that runs after the commit, tracked as
     * OI-53.
     *
     * @return the addresses invited, under "invited", and the addresses whose email could not be sent, under "failed"
     */
    public Map<String, List<String>> sendEmailInvitations(Integer courseId, Integer sectionId, List<String> emails, String role) {
        List<String> invitedEmails = new ArrayList<>();
        List<String> failedEmails = new ArrayList<>();

        for (String email : emails) {
            // Create token
            UserInvitation userInvitation = this.createUserInvitation(email, courseId, sectionId, role);
            // Save token
            this.saveUserInvitation(userInvitation);
            // Send email
            try {
                this.emailService.sendInvitationEmail(userInvitation);
                invitedEmails.add(email);
            } catch (RuntimeException e) {
                LOGGER.error("Could not send the invitation email to {}", email, e);
                failedEmails.add(email);
            }
        }

        Map<String, List<String>> result = new HashMap<>();
        result.put("invited", invitedEmails);
        result.put("failed", failedEmails);
        return result;
    }

}
