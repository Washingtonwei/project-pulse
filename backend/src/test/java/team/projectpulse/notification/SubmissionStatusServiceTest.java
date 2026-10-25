package team.projectpulse.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.projectpulse.activity.ActivityService;
import team.projectpulse.evaluation.EvaluationService;
import team.projectpulse.student.Student;
import team.projectpulse.student.StudentRepository;
import team.projectpulse.team.Team;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * BR-submission-owed: who owes a weekly activity report and who owes a peer evaluation.
 */
@ExtendWith(MockitoExtension.class)
class SubmissionStatusServiceTest {

    private static final int SECTION_ID = 2;
    private static final String WEEK = "2023-W32";

    @Mock
    StudentRepository studentRepository;
    @Mock
    ActivityService activityService;
    @Mock
    EvaluationService evaluationService;
    @InjectMocks
    SubmissionStatusService submissionStatusService;

    Student alice; // Team A
    Student bob;   // Team A
    Student carol; // Team A, deactivated
    Student dave;  // Team B
    Student erin;  // on no team

    @BeforeEach
    void setUp() {
        Team teamA = team(1, "Team A");
        Team teamB = team(2, "Team B");
        this.alice = student(10, "Alice", "Adams", teamA, true);
        this.bob = student(11, "Bob", "Brown", teamA, true);
        this.carol = student(12, "Carol", "Clark", teamA, false);
        this.dave = student(13, "Dave", "Davis", teamB, true);
        this.erin = student(14, "Erin", "Evans", null, true);
        given(this.studentRepository.findBySectionSectionId(SECTION_ID))
                .willReturn(List.of(this.dave, this.erin, this.carol, this.bob, this.alice));
    }

    @Test
    void testWeeklyActivityReportOwedByEligibleStudentsWithNoActivity() {
        // Given: Alice reported; Carol and Erin did not, but neither owes anything
        given(this.activityService.findReportingStudentIds(SECTION_ID, WEEK)).willReturn(Set.of(10));

        // When
        List<Student> owing = this.submissionStatusService.findOwing(SECTION_ID, SubmittableItem.WEEKLY_ACTIVITY_REPORT, WEEK);

        // Then: ordered by team, then name; no student on no team, no deactivated student
        assertThat(owing).containsExactly(this.bob, this.dave);
        verify(this.evaluationService, never()).findEvaluateeIdsByEvaluator(anyInt(), anyString());
    }

    @Test
    void testPeerEvaluationOwedUntilEveryActiveTeammateIncludingSelfIsEvaluated() {
        // Given: Alice evaluated Bob and herself, which is every active member of Team A (Carol is deactivated, so
        // not evaluating her does not matter). Bob evaluated only Alice. Dave evaluated nobody.
        given(this.evaluationService.findEvaluateeIdsByEvaluator(SECTION_ID, WEEK))
                .willReturn(Map.of(10, Set.of(10, 11), 11, Set.of(10)));

        // When
        List<Student> owing = this.submissionStatusService.findOwing(SECTION_ID, SubmittableItem.PEER_EVALUATION, WEEK);

        // Then
        assertThat(owing).containsExactly(this.bob, this.dave);
        verify(this.activityService, never()).findReportingStudentIds(anyInt(), anyString());
    }

    @Test
    void testEvaluatingSomeoneOutsideTheTeamDoesNotCount() {
        // Given: Bob evaluated himself and Dave, who is on another team, but not Alice
        given(this.evaluationService.findEvaluateeIdsByEvaluator(SECTION_ID, WEEK))
                .willReturn(Map.of(10, Set.of(10, 11), 11, Set.of(11, 13), 13, Set.of(13)));

        // When
        List<Student> owing = this.submissionStatusService.findOwing(SECTION_ID, SubmittableItem.PEER_EVALUATION, WEEK);

        // Then: Dave is alone on Team B, so evaluating himself covers his team
        assertThat(owing).containsExactly(this.bob);
    }

    @Test
    void testFindOwedItemsListsOnlyTheItemsEachStudentOwes() {
        // Given: Alice reported and evaluated everyone; Bob reported but did not evaluate; Dave did neither
        given(this.activityService.findReportingStudentIds(SECTION_ID, WEEK)).willReturn(Set.of(10, 11));
        given(this.evaluationService.findEvaluateeIdsByEvaluator(SECTION_ID, WEEK)).willReturn(Map.of(10, Set.of(10, 11)));

        // When
        Map<Student, List<SubmittableItem>> owed = this.submissionStatusService.findOwedItems(SECTION_ID,
                List.of(SubmittableItem.WEEKLY_ACTIVITY_REPORT, SubmittableItem.PEER_EVALUATION), WEEK);

        // Then: Alice owes nothing and is absent
        assertThat(owed).containsOnlyKeys(this.bob, this.dave);
        assertThat(owed.get(this.bob)).containsExactly(SubmittableItem.PEER_EVALUATION);
        assertThat(owed.get(this.dave)).containsExactly(SubmittableItem.WEEKLY_ACTIVITY_REPORT, SubmittableItem.PEER_EVALUATION);
    }

    @Test
    void testNobodyOwesWhenEveryoneSubmitted() {
        given(this.activityService.findReportingStudentIds(SECTION_ID, WEEK)).willReturn(Set.of(10, 11, 13));

        assertThat(this.submissionStatusService.findOwing(SECTION_ID, SubmittableItem.WEEKLY_ACTIVITY_REPORT, WEEK)).isEmpty();
    }

    private static Team team(int id, String name) {
        Team team = new Team(name, name + " description", "https://example.com");
        team.setTeamId(id);
        return team;
    }

    private static Student student(int id, String firstName, String lastName, Team team, boolean enabled) {
        Student student = new Student(firstName + "@abc.edu", firstName, lastName, firstName + "@abc.edu", "123456", enabled, "student");
        student.setId(id);
        if (team != null) {
            team.addStudent(student);
        }
        return student;
    }

}
