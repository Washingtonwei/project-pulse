package team.projectpulse.activity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.projectpulse.instructor.InstructorRepository;
import team.projectpulse.student.Student;
import team.projectpulse.student.StudentRepository;
import team.projectpulse.system.UserUtils;
import team.projectpulse.team.Team;

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

    @Test
    @DisplayName("A student reaches an activity of the team she is on")
    void canAccessActivityAllowsAStudentOnTheSameTeam() {
        Team team = teamWithId(1);
        Student john = studentWithId(10, team);

        given(this.activityRepository.findById(1)).willReturn(Optional.of(activityOf(john, team)));
        given(this.userUtils.getUserId()).willReturn(10);
        given(this.userUtils.hasRole("ROLE_student")).willReturn(true);
        given(this.studentRepository.findById(10)).willReturn(Optional.of(john));

        assertThat(this.activitySecurityService.canAccessActivity(1)).isTrue();
    }

    @Test
    @DisplayName("A student does not reach another team's activity, course section notwithstanding (BR-team-scoped-access)")
    void canAccessActivityDeniesAStudentOnAnotherTeam() {
        Team ownTeam = teamWithId(1);
        Team otherTeam = teamWithId(2);
        Student john = studentWithId(10, ownTeam);
        Student woody = studentWithId(11, otherTeam);

        given(this.activityRepository.findById(6)).willReturn(Optional.of(activityOf(woody, otherTeam)));
        given(this.userUtils.getUserId()).willReturn(10);
        given(this.userUtils.hasRole("ROLE_student")).willReturn(true);
        given(this.studentRepository.findById(10)).willReturn(Optional.of(john));

        assertThat(this.activitySecurityService.canAccessActivity(6)).isFalse();
    }

    @Test
    @DisplayName("A student on no team reaches no activity")
    void canAccessActivityDeniesAStudentWithNoTeam() {
        Team team = teamWithId(1);
        Student john = studentWithId(10, team);
        Student tracy = studentWithId(12, null);

        given(this.activityRepository.findById(1)).willReturn(Optional.of(activityOf(john, team)));
        given(this.userUtils.getUserId()).willReturn(12);
        given(this.userUtils.hasRole("ROLE_student")).willReturn(true);
        given(this.studentRepository.findById(12)).willReturn(Optional.of(tracy));

        assertThat(this.activitySecurityService.canAccessActivity(1)).isFalse();
    }

    @Test
    @DisplayName("An access check on an activity that does not exist is a denial, not an exception")
    void canAccessActivityDeniesWhenTheActivityDoesNotExist() {
        given(this.activityRepository.findById(999999)).willReturn(Optional.empty());

        assertThat(this.activitySecurityService.canAccessActivity(999999)).isFalse();
    }

    private static Team teamWithId(Integer teamId) {
        Team team = new Team("Team" + teamId, "description", "https://example.test");
        team.setTeamId(teamId);
        return team;
    }

    private static Student studentWithId(Integer id, Team team) {
        Student student = new Student("u" + id, "First", "Last", "u" + id + "@abc.edu", "123456", true, "student");
        student.setId(id);
        student.setTeam(team);
        return student;
    }

    private static Activity activityOf(Student student, Team team) {
        return new Activity(student, "2023-W31", team, ActivityCategory.DEVELOPMENT, "An activity",
                "A description", 4.0, 4.0, ActivityStatus.IN_PROGRESS);
    }

}
