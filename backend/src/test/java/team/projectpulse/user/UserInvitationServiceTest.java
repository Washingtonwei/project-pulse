package team.projectpulse.user;

import team.projectpulse.system.EmailService;
import team.projectpulse.system.exception.InvalidUserInvitationException;
import team.projectpulse.user.userinvitation.UserInvitation;
import team.projectpulse.user.userinvitation.UserInvitationRepository;
import team.projectpulse.user.userinvitation.UserInvitationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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