package team.projectpulse.activity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.projectpulse.instructor.InstructorRepository;
import team.projectpulse.student.StudentRepository;
import team.projectpulse.system.UserUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * The fail-closed contract for activity access: an id that names no row is a denial, never an exception,
 * because these methods run inside an {@code AuthorizationManager} ahead of the {@code DispatcherServlet}.
 *
 * <p>There is deliberately no case here for an activity with no team or no submitter. Both are
 * {@code @ManyToOne(optional = false)}, so such a row cannot exist and the checks read them directly.
 */
@ExtendWith(MockitoExtension.class)
class ActivitySecurityServiceTest {

    @Mock
    ActivityRepository activityRepository;
    @Mock
    StudentRepository studentRepository;
    @Mock
    InstructorRepository instructorRepository;
    @Mock
    UserUtils userUtils;

    @InjectMocks
    ActivitySecurityService activitySecurityService;


    @Test
    @DisplayName("An owner check on an activity that does not exist is a denial, not an exception")
    void isActivityOwnerDeniesWhenTheActivityDoesNotExist() {
        given(this.activityRepository.findById(999999)).willReturn(Optional.empty());

        assertThat(this.activitySecurityService.isActivityOwner(999999)).isFalse();
    }

}
