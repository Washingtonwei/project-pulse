package team.projectpulse.user.userinvitation;

import team.projectpulse.system.EmailService;
import team.projectpulse.system.exception.InvalidUserInvitationException;
import team.projectpulse.user.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserInvitationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserInvitationService.class);

    private final UserInvitationRepository userInvitationRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;


    public UserInvitationService(UserInvitationRepository userInvitationRepository, EmailService emailService, UserRepository userRepository) {
        this.userInvitationRepository = userInvitationRepository;
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    /**
     * The addresses invited to a course section as students that have not registered yet.
     *
     * <p>An invitation row exists exactly while it is outstanding: registering deletes the invitation it used, and
     * {@link #sendEmailInvitations} creates none for an address that already has an account. The rows are
     * therefore the answer, and nothing here needs to re-check them against the user table.
     *
     * <p>Only the addresses are returned. An invitation also carries the registration token, which is the
     * credential its email delivers, and that must not leave the server in a listing.
     */
    public List<String> findPendingStudentInvitations(Integer sectionId) {
        return this.userInvitationRepository.findBySectionIdAndRole(sectionId, "student").stream()
                .map(UserInvitation::getEmail)
                .toList();
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
     * <p>The course, course section and role come from the new invitation rather than from the row it replaces, so
     * re-inviting an address to a different course section would repoint it. AS-one-section-per-student says that
     * does not arise. Registration deletes the row, and {@link #sendEmailInvitations} skips an address that has an
     * account, so nothing is ever re-tokened once its owner has registered.
     *
     * <p>The address arrives normalized from {@link #sendEmailInvitations}. Called with anything else, the lookup
     * matches whatever the column collation matches, which is the fragility that normalizing at the edge avoids.
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
     * <p><strong>Why a failure is caught rather than thrown.</strong> A course admin invites a whole course
     * section at once (UC-STU-invite-students step 10), which is one SMTP round trip per address inside this one
     * transactional method. A single undeliverable address used to abort the loop, and the escaping exception
     * rolled the transaction back, discarding the invitation rows of everyone already emailed: those students held
     * a registration link whose token no longer existed and were told they were not invited. Catching per address
     * keeps a failure to the address it belongs to, and the summary names the ones to retry (extension 10a). A
     * failed address keeps its row, which is harmless, since a retry reuses that row's token.
     *
     * <p><strong>An address that already has an account is skipped</strong> (extension 10b): the registration page
     * refuses such an address, so the link would dead-end. Note that the refusal is the SPA's, which calls the
     * check-email endpoint before showing the form. The server has no duplicate check and no unique index on the
     * column, so do not read this skip as the thing preventing duplicate accounts.
     *
     * <p><strong>Known limit: each email goes out before its own row is committed.</strong> During a long batch the
     * first recipient holds a live-looking link while a separate request validating it cannot yet see the row, and
     * is told she was not invited; clicking again once the batch finishes works. A failure of the commit itself
     * would strand those links for good. Both want a transaction per address, or a send that runs after the
     * commit, tracked as OI-53.
     *
     * @return the addresses invited, under "invited"; those whose email could not be sent, under "failed"; and
     * those skipped as already having an account, under "alreadyExists"
     */
    public Map<String, List<String>> sendEmailInvitations(Integer courseId, Integer sectionId, List<String> emails, String role) {
        List<String> invitedEmails = new ArrayList<>();
        List<String> failedEmails = new ArrayList<>();
        List<String> skippedEmails = new ArrayList<>(); // Reported to the caller under "alreadyExists"

        // Normalized once, here at the edge, rather than compared case-insensitively at every point that touches
        // an address. An email address is case-insensitive to its owner, but in this system it is a person's
        // identity: the primary key of an invitation, the username the student logs in with, and the value the
        // registration link carries. Lowercasing on the way in makes those agree by construction, instead of
        // leaning on the column's `utf8mb4_0900_ai_ci` collation to paper over a difference in spelling. Trimming
        // goes with it, because these arrive pasted from a class roster.
        // Null and blank entries are dropped rather than normalized: the body is an unvalidated List<String>, so
        // a null in it would throw here, outside the per-address isolation below, and take the whole batch down.
        // Duplicates are dropped because normalizing makes them collide: two spellings of one address are now one
        // primary key, so without this the row is written twice and that student gets two identical emails.
        List<String> normalizedEmails = emails.stream()
                .filter(email -> email != null && !email.isBlank())
                .map(email -> email.trim().toLowerCase(Locale.ROOT))
                .distinct()
                .toList();

        // One query for the whole batch rather than one per address, and none at all for an empty batch, since an
        // empty IN clause is not worth asking the database about. Stored addresses are still lowercased here: rows
        // written before this normalization existed can carry any casing, and the collation is what matches them.
        Set<String> emailsWithAnAccount = normalizedEmails.isEmpty() ? Set.of() : this.userRepository.findByEmailIn(normalizedEmails).stream()
                .map(user -> user.getEmail().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        for (String email : normalizedEmails) {
            if (emailsWithAnAccount.contains(email)) {
                skippedEmails.add(email);
                continue;
            }

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
        result.put("alreadyExists", skippedEmails);
        return result;
    }

}
