package team.projectpulse.team;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.projectpulse.instructor.InstructorRepository;
import team.projectpulse.section.Section;
import team.projectpulse.student.Student;
import team.projectpulse.student.StudentRepository;
import team.projectpulse.system.UserUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * The fail-closed contract: these methods run inside an {@code AuthorizationManager}, ahead of the
 * {@code DispatcherServlet}, so anything they throw escapes the filter chain as a 500 instead of reaching
 * {@code ExceptionHandlerAdvice}. Every unanswerable question has to come back as a denial.
 *
 * <p>The only relationship exercised as absent here is {@code Student.team}, which is genuinely optional. The
 * mandatory ones are {@code @ManyToOne(optional = false)} on the entities, so there is nothing to test.
 */
@ExtendWith(MockitoExtension.class)
class TeamSecurityServiceTest {

    @Mock
    TeamRepository teamRepository;
    @Mock
    StudentRepository studentRepository;
    @Mock
    InstructorRepository instructorRepository;
    @Mock
    UserUtils userUtils;

    @InjectMocks
    TeamSecurityService teamSecurityService;

    Team team;


    @BeforeEach
    void setUp() {
        Section section = new Section();
        section.setSectionId(1);
        this.team = new Team("Team1", "Team 1 description", "https://www.team1.com");
        this.team.setTeamId(1);
        this.team.setSection(section);
    }

    @Test
    @DisplayName("An owner check on a team that does not exist is a denial, not an exception")
    void isTeamOwnerDeniesWhenTheTeamDoesNotExist() {
        given(this.teamRepository.findById(999999)).willReturn(Optional.empty());

        assertThat(this.teamSecurityService.isTeamOwner(999999)).isFalse();
    }

    @Test
    @DisplayName("A membership check on a team that does not exist is a denial, not an exception")
    void canAccessTeamDeniesWhenTheTeamDoesNotExist() {
        given(this.teamRepository.findById(999999)).willReturn(Optional.empty());

        assertThat(this.teamSecurityService.canAccessTeam(999999)).isFalse();
    }

    @Test
    @DisplayName("A student who has not been assigned to a team yet is denied, not a null dereference")
    void canAccessTeamDeniesAStudentWithNoTeam() {
        Student studentWithNoTeam = new Student("j.smith@abc.edu", "John", "Smith", "j.smith@abc.edu", "123456", true, "student");
        studentWithNoTeam.setId(4);

        given(this.teamRepository.findById(1)).willReturn(Optional.of(this.team));
        given(this.userUtils.getUserId()).willReturn(4);
        given(this.userUtils.hasRole("ROLE_student")).willReturn(true);
        given(this.userUtils.hasRole("ROLE_instructor")).willReturn(false);
        given(this.studentRepository.findById(4)).willReturn(Optional.of(studentWithNoTeam));

        assertThat(this.teamSecurityService.canAccessTeam(1)).isFalse();
    }

    @Test
    @DisplayName("A JWT naming a student row that is gone is denied, not an exception")
    void canAccessTeamDeniesWhenTheCallersStudentRowIsMissing() {
        given(this.teamRepository.findById(1)).willReturn(Optional.of(this.team));
        given(this.userUtils.getUserId()).willReturn(4);
        given(this.userUtils.hasRole("ROLE_student")).willReturn(true);
        given(this.userUtils.hasRole("ROLE_instructor")).willReturn(false);
        given(this.studentRepository.findById(4)).willReturn(Optional.empty());

        assertThat(this.teamSecurityService.canAccessTeam(1)).isFalse();
    }

    @Test
    @DisplayName("A same-section check against ids that name no rows is a denial, not an exception")
    void sameSectionChecksDenyWhenEitherSideDoesNotExist() {
        given(this.teamRepository.findById(999999)).willReturn(Optional.empty());
        given(this.studentRepository.findById(999998)).willReturn(Optional.empty());

        assertThat(this.teamSecurityService.isTeamAndStudentInSameSection(999999, 999998)).isFalse();

        given(this.instructorRepository.findById(999997)).willReturn(Optional.empty());

        assertThat(this.teamSecurityService.isTeamAndInstructorInSameSection(999999, 999997)).isFalse();
    }

}
