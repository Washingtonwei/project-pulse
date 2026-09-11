package team.projectpulse.user;

import team.projectpulse.student.Student;
import team.projectpulse.system.EmailService;
import team.projectpulse.system.exception.InvalidUserInvitationException;
import team.projectpulse.user.userinvitation.UserInvitation;
import team.projectpulse.user.userinvitation.UserInvitationRepository;
import team.projectpulse.user.userinvitation.UserInvitationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserInvitationServiceTest {

    @Mock
    private UserInvitationRepository userInvitationRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserInvitationService userInvitationService;

    @Test
    void testValidateUserInvitationSuccess() {
        // Given
        String providedEmail = "v.gordon@abc.edu";
        String providedRegistrationToken = "token";
        Integer providedCourseId = 1;
        Integer providedSectionId = 2;
        String providedRole = "student";

        UserInvitation userInvitation = new UserInvitation("v.gordon@abc.edu", 1, 2, "token", "student");
        given(this.userInvitationRepository.findById(providedEmail)).willReturn(Optional.of(userInvitation));

        // When and Then
        assertDoesNotThrow(() -> this.userInvitationService.validateUserInvitation(providedEmail, providedRegistrationToken, providedCourseId, providedSectionId, providedRole));
        verify(this.userInvitationRepository).findById(providedEmail);
    }

    @Test
    void testValidateUserInvitationUninvitedUser() {
        // Given
        String providedEmail = "c.hunter@abc.edu"; // Uninvited user
        String providedRegistrationToken = "token";
        Integer providedCourseId = 1;
        Integer providedSectionId = 2;
        String providedRole = "student";

        UserInvitation userInvitation = new UserInvitation("v.gordon@abc.edu", 1, 2, "token", "student");
        given(this.userInvitationRepository.findById(providedEmail)).willReturn(Optional.empty());

        // When
        Throwable thrown = catchThrowable(() -> {
            this.userInvitationService.validateUserInvitation(providedEmail, providedRegistrationToken, providedCourseId, providedSectionId, providedRole);
        });
        assertThat(thrown)
                .isInstanceOf(InvalidUserInvitationException.class)
                .hasMessage(providedEmail + " is not invited to register. Please contact the course admin.");
        verify(this.userInvitationRepository).findById(providedEmail);
    }

    @Test
    void testValidateUserInvitationInvalidRegistrationToken() {
        // Given
        String providedEmail = "v.gordon@abc.edu";
        String providedRegistrationToken = "token";
        Integer providedCourseId = 1;
        Integer providedSectionId = 2;
        String providedRole = "student";

        UserInvitation userInvitation = new UserInvitation("v.gordon@abc.edu", 1, 2, "differentToken", "student");
        given(this.userInvitationRepository.findById(providedEmail)).willReturn(Optional.of(userInvitation));

        // When
        Throwable thrown = catchThrowable(() -> {
            this.userInvitationService.validateUserInvitation(providedEmail, providedRegistrationToken, providedCourseId, providedSectionId, providedRole);
        });
        assertThat(thrown)
                .isInstanceOf(InvalidUserInvitationException.class)
                .hasMessage("Invalid registration token for email: " + providedEmail);
        verify(this.userInvitationRepository).findById(providedEmail);
    }


    @Test
    void testValidateUserInvitationUnmatchingRole() {
        // Given
        String providedEmail = "v.gordon@abc.edu";
        String providedRegistrationToken = "token";
        Integer providedCourseId = 1;
        Integer providedSectionId = 2;
        String providedRole = "instructor";

        UserInvitation userInvitation = new UserInvitation("v.gordon@abc.edu", 1, 2, "token", "student");
        given(this.userInvitationRepository.findById(providedEmail)).willReturn(Optional.of(userInvitation));

        // When
        Throwable thrown = catchThrowable(() -> {
            this.userInvitationService.validateUserInvitation(providedEmail, providedRegistrationToken, providedCourseId, providedSectionId, providedRole);
        });
        assertThat(thrown)
                .isInstanceOf(InvalidUserInvitationException.class)
                .hasMessage("You are not allowed to register as " + providedRole + " for email: " + providedEmail);
        verify(this.userInvitationRepository).findById(providedEmail);
    }

    @Test
    void testValidateUserInvitationIncorrectCourseOrSectionId() {
        // Given
        String providedEmail = "v.gordon@abc.edu";
        String providedRegistrationToken = "token";
        Integer providedCourseId = 1;
        Integer providedSectionId = 3;
        String providedRole = "student";

        UserInvitation userInvitation = new UserInvitation("v.gordon@abc.edu", 1, 2, "token", "student");
        given(this.userInvitationRepository.findById(providedEmail)).willReturn(Optional.of(userInvitation));

        // When
        Throwable thrown = catchThrowable(() -> {
            this.userInvitationService.validateUserInvitation(providedEmail, providedRegistrationToken, providedCourseId, providedSectionId, providedRole);
        });
        assertThat(thrown)
                .isInstanceOf(InvalidUserInvitationException.class)
                .hasMessage("You are not invited to register for this course or section with email: " + providedEmail);
        verify(this.userInvitationRepository).findById(providedEmail);
    }

    @Test
    void testSendEmailInvitations() {
        // Given
        Integer courseId = 1;
        Integer sectionId = 2;
        List<String> emails = List.of("v.gordon@abc.edu", "c.hunter@abc.edu", "m.west@abc.edu", "l.santos@abc.edu");
        String role = "student";
        given(this.userInvitationRepository.save(any())).willReturn(new UserInvitation());
        doNothing().when(emailService).sendInvitationEmail(any());

        // When
        Map<String, List<String>> result = this.userInvitationService.sendEmailInvitations(courseId, sectionId, emails, role);

        // Then
        assertThat(result.get("invited")).containsExactlyElementsOf(emails);
        assertThat(result.get("failed")).isEmpty();
        verify(this.userInvitationRepository, times(4)).save(any());
        verify(this.emailService, times(4)).sendInvitationEmail(any());
    }

    @Test
    void testSendEmailInvitationsIgnoresBlankEntriesAndDuplicates() {
        // Given
        // What a pasted roster actually looks like: a trailing separator leaving an empty entry, and one address
        // spelled two ways. Normalizing makes those two spellings one primary key, so without de-duplication the
        // row is written twice and that student gets two identical emails.
        List<String> emails = Arrays.asList("A.Lee@abc.edu", "  ", "a.lee@abc.edu", null);

        // When
        Map<String, List<String>> result = this.userInvitationService.sendEmailInvitations(1, 2, emails, "student");

        // Then
        assertThat(result.get("invited")).containsExactly("a.lee@abc.edu");
        verify(this.userInvitationRepository, times(1)).save(any());
        verify(this.emailService, times(1)).sendInvitationEmail(any());
    }

    @Test
    void testSendEmailInvitationsSkipsAnAddressThatAlreadyHasAnAccount() {
        // Given
        // Re-inviting a roster to reach the students who have not signed up. The one who has must not be emailed a
        // second registration link, because registration refuses an address that already has an account, and the
        // invitation row that send would leave behind is never deleted.
        List<String> emails = List.of("v.gordon@abc.edu", "  J.Smith@abc.edu  "); // as pasted from a roster
        given(this.userRepository.findByEmailIn(List.of("v.gordon@abc.edu", "j.smith@abc.edu")))
                .willReturn(List.of(new Student("j.smith@abc.edu", "John", "Smith", "j.smith@abc.edu", "123456", true, "student")));

        // When
        Map<String, List<String>> result = this.userInvitationService.sendEmailInvitations(1, 2, emails, "student");

        // Then
        // The address is normalized on the way in, so the lookup, the stored row and the report all agree
        assertThat(result.get("invited")).containsExactly("v.gordon@abc.edu");
        assertThat(result.get("alreadyExists")).containsExactly("j.smith@abc.edu");
        assertThat(result.get("failed")).isEmpty();
        // Nothing was written or sent for the registered address
        verify(this.userInvitationRepository, times(1)).save(any());
        verify(this.emailService, times(1)).sendInvitationEmail(any());
    }

    @Test
    void testFindPendingStudentInvitations() {
        // Given
        // An invitation row exists exactly while it is outstanding, so the rows are the answer: registering
        // deletes the invitation it used, and no invitation is written for an address that already has an account.
        given(this.userInvitationRepository.findBySectionIdAndRole(2, "student")).willReturn(List.of(
                new UserInvitation("v.gordon@abc.edu", 1, 2, "token", "student"),
                new UserInvitation("c.hunter@abc.edu", 1, 2, "token", "student")));

        // When
        List<String> pendingEmails = this.userInvitationService.findPendingStudentInvitations(2);

        // Then
        assertThat(pendingEmails).containsExactly("v.gordon@abc.edu", "c.hunter@abc.edu");
    }

    @Test
    void testSendEmailInvitationsKeepsTheTokenOfAnExistingInvitation() {
        // Given
        // Re-inviting an address that already holds an invitation, which is what a course admin does when a batch
        // looks like it failed. The link already in that inbox has to keep working, so the token is not re-minted.
        String email = "v.gordon@abc.edu";
        given(this.userInvitationRepository.findById(email))
                .willReturn(Optional.of(new UserInvitation(email, 1, 2, "the-token-already-emailed", "student")));

        // When
        this.userInvitationService.sendEmailInvitations(1, 2, List.of(email), "student");

        // Then
        ArgumentCaptor<UserInvitation> saved = ArgumentCaptor.forClass(UserInvitation.class);
        verify(this.userInvitationRepository).save(saved.capture());
        assertThat(saved.getValue().getToken()).isEqualTo("the-token-already-emailed");
        verify(this.emailService).sendInvitationEmail(saved.getValue());
    }

    @Test
    void testSendEmailInvitationsContinuesAfterOneAddressFails() {
        // Given
        // The failure the course admin actually hits: one address the mail server refuses, in the middle of a
        // course section's worth of invitations. Everyone after it must still be invited and emailed.
        Integer courseId = 1;
        Integer sectionId = 2;
        List<String> emails = List.of("v.gordon@abc.edu", "c.hunter@abc.edu", "m.west@abc.edu", "l.santos@abc.edu");
        String role = "student";
        // The broad stub has to come first, and it is not decoration. Under strict stubs a call that matches no
        // stubbing throws PotentialStubbingProblem, and the service catches RuntimeException, so Mockito's own
        // complaint would be swallowed and counted as a failed address. Mockito uses the last matching stub, so
        // the narrow one below still wins for the address it names.
        willDoNothing().given(this.emailService).sendInvitationEmail(any());
        // Keyed to the address rather than to the call number, so the test still means what it says if the
        // implementation ever reorders or deduplicates the list.
        willThrow(new RuntimeException("Failed to send email"))
                .given(this.emailService)
                .sendInvitationEmail(argThat(invitation -> "c.hunter@abc.edu".equals(invitation.getEmail())));

        // When
        Map<String, List<String>> result = this.userInvitationService.sendEmailInvitations(courseId, sectionId, emails, role);

        // Then
        assertThat(result.get("invited")).containsExactly("v.gordon@abc.edu", "m.west@abc.edu", "l.santos@abc.edu");
        assertThat(result.get("failed")).containsExactly("c.hunter@abc.edu");
        // Every address was attempted, and every invitation was written: the one that failed keeps its row, so a
        // retry simply overwrites it with a fresh token, and nothing rolls back the invitations already emailed.
        verify(this.emailService, times(4)).sendInvitationEmail(any());
        verify(this.userInvitationRepository, times(4)).save(any());
    }

}