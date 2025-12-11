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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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
    void testValidateUserInvitationIncorrectSectionId() {
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
        this.userInvitationService.sendEmailInvitations(courseId, sectionId, emails, role);

        // Then
        verify(this.userInvitationRepository, times(4)).save(any());
        verify(this.emailService, times(4)).sendInvitationEmail(any());
    }

}