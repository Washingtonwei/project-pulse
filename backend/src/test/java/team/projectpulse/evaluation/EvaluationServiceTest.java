package team.projectpulse.evaluation;

import team.projectpulse.instructor.Instructor;
import team.projectpulse.rubric.Criterion;
import team.projectpulse.rubric.Rating;
import team.projectpulse.rubric.Rubric;
import team.projectpulse.section.Section;
import team.projectpulse.section.SectionRepository;
import team.projectpulse.student.Student;
import team.projectpulse.student.StudentRepository;
import team.projectpulse.system.exception.PeerEvaluationIllegalArgumentException;
import team.projectpulse.team.Team;
import team.projectpulse.user.MyUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

    @Mock
    PeerEvaluationRepository evaluationRepository;
    @Mock
    StudentRepository studentRepository;
    @Mock
    SectionRepository sectionRepository;
    @Mock
    Clock clock; // Mock the clock bean

    @InjectMocks
    EvaluationService evaluationService;

    List<PeerEvaluation> evaluations;
    List<Student> students;

    Student john; // Team 1
    Student eric; // Team 1
    Student woody; // Team 2
    Student amanda; // Team 2

    List<PeerEvaluation> johnsEvaluations;
    List<PeerEvaluation> ericsW31Evaluations;
    List<PeerEvaluation> ericsW32Evaluations;
    List<PeerEvaluation> jerryEvaulaitons;
    List<PeerEvaluation> woodysEvaluations;
    List<PeerEvaluation> amandasEvaluations;

    @BeforeEach
    void setUp() {
        // Create several peer evaluation criteria
        Criterion criterion1 = new Criterion("Quality of work", "How do you rate the quality of this teammate's work? (1-10)", 10.00);
        criterion1.setCriterionId(1);
        Criterion criterion2 = new Criterion("Productivity", "How productive is this teammate? (1-10)", 10.00);
        criterion2.setCriterionId(2);
        Criterion criterion3 = new Criterion("Initiative", "How proactive is this teammate? (1-10)", 10.00);
        criterion3.setCriterionId(3);
        Criterion criterion4 = new Criterion("Courtesy", "Does this teammate treat others with respect? (1-10)", 10.00);
        criterion4.setCriterionId(4);
        Criterion criterion5 = new Criterion("Open-mindedness", "How well does this teammate handle criticism of their work? (1-10)", 10.00);
        criterion5.setCriterionId(5);
        Criterion criterion6 = new Criterion("Engagement in meetings", "How is this teammate's performance during meetings? (1-10)", 10.00);
        criterion6.setCriterionId(6);
        List<Criterion> criteria = List.of(criterion1, criterion2, criterion3, criterion4, criterion5, criterion6);

        // Create a rubric with the criteria
        Rubric rubric = new Rubric("Peer Eval Rubric v1");
        rubric.setRubricId(1);
        criteria.forEach(rubric::addCriterion);

        // Create instructors
        Instructor instructor1 = new Instructor("bingyang", "Bingyang", "Wei", "b.wei@tcu.edu", "123456", true, "admin instructor");
        instructor1.setId(1);

        // Create a section
        Section section2 = new Section("2023-2024", LocalDate.of(2023, 8, 14), LocalDate.of(2024, 4, 29));
        section2.setSectionId(2);
        section2.setActiveWeeks(List.of("2023-W31", "2023-W32", "2023-W33", "2023-W34", "2023-W35", "2023-W36", "2023-W37", "2023-W38", "2023-W39", "2023-W40"));
        section2.setRubric(rubric);
        section2.addInstructor(instructor1);

        // Create a team
        Team team1 = new Team("Team1", "Team 1 description", "https://www.team1.com");
        team1.setTeamId(1);
        team1.setSection(section2);
        team1.addInstructor(instructor1);

        Team team2 = new Team("Team2", "Team 2 description", "https://www.team2.com");
        team2.setTeamId(2);
        team2.setSection(section2);
        team2.addInstructor(instructor1);

        // Create students
        Student john = new Student("john", "John", "Smith", "j.smith@abc.edu", "123456", true, "student");
        john.setId(3);
        Student eric = new Student("eric", "Eric", "Wong", "e.wong@abc.edu", "123456", true, "student");
        eric.setId(4);
        Student jerry = new Student("jerry", "Jerry", "Moon", "j.moon@abc.edu", "123456", true, "student");
        jerry.setId(5);
        Student woody = new Student("woody", "Woody", "Allen", "w.allen@abc.edu", "123456", true, "student");
        woody.setId(6);
        Student amanda = new Student("amanda", "Amanda", "Wagner", "a.wagner@abc.edu", "123456", true, "student");
        amanda.setId(7);

        section2.addStudent(john);
        team1.addStudent(john);

        section2.addStudent(eric);
        team1.addStudent(eric);

        section2.addStudent(jerry);
        team1.addStudent(jerry);

        section2.addStudent(woody);
        team2.addStudent(woody);

        section2.addStudent(amanda);
        team2.addStudent(amanda);

        this.students = List.of(john, eric, jerry, woody, amanda);
        this.john = john;
        this.eric = eric;
        this.woody = woody;
        this.amanda = amanda;

        // John evaluates Eric
        List<Rating> johnEricRatings = List.of(
                new Rating(criterion1, 8.0),
                new Rating(criterion2, 10.0),
                new Rating(criterion3, 5.0),
                new Rating(criterion4, 8.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 7.0));
        PeerEvaluation peerEvaluation1 = new PeerEvaluation("2023-W31", john, eric, johnEricRatings, "Eric did a great job this week! I think he is on track.", "John wrote this private comment!");

        // John evaluates Jerry
        List<Rating> johnJerryRatings = List.of(
                new Rating(criterion1, 6.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 7.0),
                new Rating(criterion4, 3.0),
                new Rating(criterion5, 7.0),
                new Rating(criterion6, 10.0));
        PeerEvaluation peerEvaluation2 = new PeerEvaluation("2023-W31", john, jerry, johnJerryRatings, "Jerry is OK. Be responsive, please.", "Nothing to say! Commented by John!");

        // John evaluates John
        List<Rating> johnJohnRatings = List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 10.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 3.0),
                new Rating(criterion5, 8.0),
                new Rating(criterion6, 10.0));
        PeerEvaluation peerEvaluation3 = new PeerEvaluation("2023-W31", john, john, johnJohnRatings, "I (john) am doing well.", "Nothing to say! John is pretty good this week compared with last week!");

        // Eric evaluates John
        List<Rating> ericJohnRatings = List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 10.0),
                new Rating(criterion6, 2.0));
        PeerEvaluation peerEvaluation4 = new PeerEvaluation("2023-W31", eric, john, ericJohnRatings, "John's job is well done!", "I am eric.");

        // Eric evaluates Jerry
        List<Rating> ericJerryRatings = List.of(
                new Rating(criterion1, 4.0),
                new Rating(criterion2, 10.0),
                new Rating(criterion3, 7.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 2.0));
        PeerEvaluation peerEvaluation5 = new PeerEvaluation("2023-W31", eric, jerry, ericJerryRatings, "Jerry's job is so so! Commented by Eric.", "I am eric.");

        // Eric evaluates Eric
        List<Rating> ericEricRatings = List.of(
                new Rating(criterion1, 7.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 9.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 10.0));
        PeerEvaluation peerEvaluation6 = new PeerEvaluation("2023-W31", eric, eric, ericEricRatings, "As Eric, I just do my best!", "I am eric.");

        // Jerry evaluates John
        List<Rating> jerryJohnRatings = List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 9.0),
                new Rating(criterion5, 4.0),
                new Rating(criterion6, 10.0));
        PeerEvaluation peerEvaluation7 = new PeerEvaluation("2023-W31", jerry, john, jerryJohnRatings, "John john john, go, go go!", "I am Jerry.");

        // Jerry evaluates Eric
        List<Rating> jerryEricRatings = List.of(
                new Rating(criterion1, 8.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 9.0),
                new Rating(criterion5, 6.0),
                new Rating(criterion6, 7.0));
        PeerEvaluation peerEvaluation8 = new PeerEvaluation("2023-W31", jerry, eric, jerryEricRatings, "eric eric eric, go, go go!", "I am Jerry.");

        // Jerry evaluates Jerry
        List<Rating> jerryJerryRatings = List.of(
                new Rating(criterion1, 9.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 4.0));
        PeerEvaluation peerEvaluation9 = new PeerEvaluation("2023-W31", jerry, jerry, jerryJerryRatings, "Jerry jerry jerry, go, go go!", "I am Jerry.");

        this.johnsEvaluations = List.of(peerEvaluation3, peerEvaluation4, peerEvaluation7);
        this.ericsW31Evaluations = List.of(peerEvaluation1, peerEvaluation6, peerEvaluation8);
        this.jerryEvaulaitons = List.of(peerEvaluation2, peerEvaluation5, peerEvaluation9);

        // Woody evaluates Amanda
        List<Rating> woodyAmandaRatings = List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 10.0),
                new Rating(criterion3, 6.0),
                new Rating(criterion4, 8.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 9.0));
        PeerEvaluation peerEvaluation10 = new PeerEvaluation("2023-W31", woody, amanda, woodyAmandaRatings, "Your ability to debug complex issues quickly has been invaluable to our project's success.", "You consistently deliver high-quality code, and your attention to detail is impressive.");
        this.amandasEvaluations = List.of(peerEvaluation10);
        // Woody evaluates Woody
        List<Rating> woodyWoodyRatings = List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 10.0),
                new Rating(criterion6, 2.0));
        PeerEvaluation peerEvaluation11 = new PeerEvaluation("2023-W31", woody, woody, woodyWoodyRatings, "You handle feedback well and use it to improve your work continuously.", "I am woody.");
        this.woodysEvaluations = List.of(peerEvaluation11);

        this.evaluations = List.of(peerEvaluation1, peerEvaluation2, peerEvaluation3, peerEvaluation4, peerEvaluation5, peerEvaluation6, peerEvaluation7, peerEvaluation8, peerEvaluation9, peerEvaluation10, peerEvaluation11);


        // John evaluates Eric
        List<Rating> johnEricRatingsW32 = List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 10.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 5.0));
        PeerEvaluation peerEvaluation12 = new PeerEvaluation("2023-W32", john, eric, johnEricRatingsW32, "Eric was sick last week.", "John wrote this private comment!");

        // Eric evaluates Eric
        List<Rating> ericEricRatingsW32 = List.of(
                new Rating(criterion1, 5.0),
                new Rating(criterion2, 10.0),
                new Rating(criterion3, 2.0),
                new Rating(criterion4, 5.0),
                new Rating(criterion5, 7.0),
                new Rating(criterion6, 5.0));
        PeerEvaluation peerEvaluation13 = new PeerEvaluation("2023-W32", eric, eric, ericEricRatingsW32, "I was not on campus last week!", "I am eric.");

        // Jerry evaluates Eric
        List<Rating> jerryEricRatingsW32 = List.of(
                new Rating(criterion1, 4.0),
                new Rating(criterion2, 10.0),
                new Rating(criterion3, 5.0),
                new Rating(criterion4, 6.0),
                new Rating(criterion5, 3.0),
                new Rating(criterion6, 9.0));
        PeerEvaluation peerEvaluation14 = new PeerEvaluation("2023-W32", jerry, eric, jerryEricRatingsW32, "eric needs to attend meeting!", "I am Jerry.");

        this.ericsW32Evaluations = List.of(peerEvaluation12, peerEvaluation13, peerEvaluation14);
    }

    @Test
    void testAddPeerEvaluationSuccess() {
        // Given
        LocalDate fixedDate = LocalDate.of(2023, 8, 13); // August 13, 2023 is the Sunday of the week 2023-W32, so the previous week is 2023-W31
        Clock fixedClock = Clock.fixed(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        given(this.clock.instant()).willReturn(fixedClock.instant());
        given(this.clock.getZone()).willReturn(fixedClock.getZone());

        PeerEvaluation newPeerEvaluation = new PeerEvaluation("2023-W31", amanda, woody, List.of(), "public comment", "private comment");
        given(this.evaluationRepository.save(newPeerEvaluation)).willReturn(newPeerEvaluation);

        // When
        PeerEvaluation savedPeerEvaluation = this.evaluationService.addPeerEvaluation(newPeerEvaluation);

        // Then
        assertThat(savedPeerEvaluation).isEqualTo(newPeerEvaluation);
        verify(this.evaluationRepository, times(1)).save(newPeerEvaluation);
    }

    @Test
    void testAddPeerEvaluationNotInActiveWeeks() {
        // Given
        LocalDate fixedDate = LocalDate.of(2023, 8, 8); // 2023-W32
        Clock fixedClock = Clock.fixed(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        given(this.clock.instant()).willReturn(fixedClock.instant());
        given(this.clock.getZone()).willReturn(fixedClock.getZone());

        // Active weeks are "2023-W31", "2023-W32", "2023-W33", "2023-W34", "2023-W35", "2023-W36", "2023-W37", "2023-W38", "2023-W39", "2023-W40"
        // 2023-W19 is not in the active weeks for the section
        PeerEvaluation newPeerEvaluation = new PeerEvaluation("2023-W19", eric, woody, List.of(), "public comment", "private comment");

        // When
        Throwable throwable = catchThrowable(() -> this.evaluationService.addPeerEvaluation(newPeerEvaluation));

        // Then
        assertThat(throwable)
                .isInstanceOf(PeerEvaluationIllegalArgumentException.class)
                .hasMessage("The submission week is not in the active weeks for the section.");
    }

    @Test
    void testAddPeerEvaluationNotForPreviousWeek() {
        // Given
        LocalDate fixedDate = LocalDate.of(2023, 8, 8); // 2023-W32
        Clock fixedClock = Clock.fixed(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        given(this.clock.instant()).willReturn(fixedClock.instant());
        given(this.clock.getZone()).willReturn(fixedClock.getZone());

        PeerEvaluation newPeerEvaluation = new PeerEvaluation("2023-W33", eric, woody, List.of(), "public comment", "private comment");

        // When
        Throwable throwable = catchThrowable(() -> this.evaluationService.addPeerEvaluation(newPeerEvaluation));

        // Then
        assertThat(throwable)
                .isInstanceOf(PeerEvaluationIllegalArgumentException.class)
                .hasMessage("You can only submit evaluations for the previous week.");
    }

    @Test
    void testAddPeerEvaluationEvaluatorEvaluateeNotOnTheSameTeam() {
        // Given
        LocalDate fixedDate = LocalDate.of(2023, 8, 8);
        Clock fixedClock = Clock.fixed(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        given(this.clock.instant()).willReturn(fixedClock.instant());
        given(this.clock.getZone()).willReturn(fixedClock.getZone());

        PeerEvaluation newPeerEvaluation = new PeerEvaluation("2023-W31", eric, woody, List.of(), "public comment", "private comment");

        // When
        Throwable throwable = catchThrowable(() -> this.evaluationService.addPeerEvaluation(newPeerEvaluation));

        // Then
        assertThat(throwable)
                .isInstanceOf(PeerEvaluationIllegalArgumentException.class)
                .hasMessage("The evaluator and evaluatee must be on the same team.");
    }

    @Test
    void testAddPeerEvaluationDuplicatedEvaluation() {
        // Given
        LocalDate fixedDate = LocalDate.of(2023, 8, 8);
        Clock fixedClock = Clock.fixed(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        given(this.clock.instant()).willReturn(fixedClock.instant());
        given(this.clock.getZone()).willReturn(fixedClock.getZone());

        PeerEvaluation newPeerEvaluation = new PeerEvaluation("2023-W31", john, eric, List.of(), "public comment", "private comment");
        given(this.evaluationRepository.findByWeekAndEvaluatorIdAndEvaluateeId("2023-W31", 3, 4)).willReturn(Optional.of(newPeerEvaluation));

        // When
        Throwable throwable = catchThrowable(() -> this.evaluationService.addPeerEvaluation(newPeerEvaluation));

        // Then
        assertThat(throwable)
                .isInstanceOf(PeerEvaluationIllegalArgumentException.class)
                .hasMessage("You have already submitted an evaluation for " + newPeerEvaluation.getEvaluatee().getFirstName() + " in this week.");
    }

    @Test
    void testUpdatePeerEvaluation() {
        // Given
        PeerEvaluation oldPeerEvaluation = new PeerEvaluation("2023-W31", new Student(), new Student(), List.of(), "public comment", "private comment");
        oldPeerEvaluation.setPeerEvaluationId(1);

        PeerEvaluation update = new PeerEvaluation("2023-W31", new Student(), new Student(), List.of(), "new public comment", "new private comment");
        update.setPeerEvaluationId(1);

        given(this.evaluationRepository.findById(1)).willReturn(Optional.of(oldPeerEvaluation));
        given(this.evaluationRepository.save(oldPeerEvaluation)).willReturn(oldPeerEvaluation);

        // When
        PeerEvaluation updatedPeerEvaluation = this.evaluationService.updatePeerEvaluation(1, update);

        // Then
        assertThat(updatedPeerEvaluation.getPeerEvaluationId()).isEqualTo(update.getPeerEvaluationId());
        assertThat(updatedPeerEvaluation.getPublicComment()).isEqualTo(update.getPublicComment());
        assertThat(updatedPeerEvaluation.getPrivateComment()).isEqualTo(update.getPrivateComment());
        verify(this.evaluationRepository, times(1)).findById(1);
        verify(this.evaluationRepository, times(1)).save(oldPeerEvaluation);
    }

    @Test
    void testGetPeerEvaluationAverage() {
        // Given
        given(this.evaluationRepository.findByWeekAndEvaluateeId("2023-W31", 4)).willReturn(this.ericsW31Evaluations);

        // When
        PeerEvaluationAverage peerEvaluationAverage = this.evaluationService.getPeerEvaluationAverage("2023-W31", eric);

        // Then
        assertThat(peerEvaluationAverage.getStudentId()).isEqualTo(4);
        assertThat(peerEvaluationAverage.getWeek()).isEqualTo("2023-W31");
        assertThat(peerEvaluationAverage.getFirstName()).isEqualTo("Eric");
        assertThat(peerEvaluationAverage.getLastName()).isEqualTo("Wong");
        assertThat(peerEvaluationAverage.getEmail()).isEqualTo("e.wong@abc.edu");
        assertThat(peerEvaluationAverage.getTeamName()).isEqualTo("Team1");
        assertThat(peerEvaluationAverage.getPublicComments()).containsAll(List.of("Eric did a great job this week! I think he is on track.", "As Eric, I just do my best!", "eric eric eric, go, go go!"));
        assertThat(peerEvaluationAverage.getPrivateComments()).containsAll(List.of("John wrote this private comment!", "I am eric.", "I am Jerry."));
        assertThat(peerEvaluationAverage.getAverageTotalScore()).isCloseTo(50.0, within(0.01));
        assertThat(peerEvaluationAverage.getRatingAverages().get(0).getAverageScore()).isCloseTo(7.66, within(0.01));
        assertThat(peerEvaluationAverage.getRatingAverages().get(1).getAverageScore()).isCloseTo(9.33, within(0.01));
        assertThat(peerEvaluationAverage.getRatingAverages().get(2).getAverageScore()).isCloseTo(8.33, within(0.01));
        assertThat(peerEvaluationAverage.getRatingAverages().get(3).getAverageScore()).isCloseTo(8.66, within(0.01));
        assertThat(peerEvaluationAverage.getRatingAverages().get(4).getAverageScore()).isCloseTo(8.0, within(0.01));
        assertThat(peerEvaluationAverage.getRatingAverages().get(5).getAverageScore()).isCloseTo(8.0, within(0.01));
    }

    @Test
    void testGenerateWeeklyPeerEvaluationReportForSection() {
        // Given
        given(this.sectionRepository.findById(anyInt())).willReturn(Optional.of(new Section("2023-2024", LocalDate.of(2023, 8, 14), LocalDate.of(2024, 4, 29))));
        given(this.studentRepository.findBySectionSectionId(anyInt())).willReturn(this.students);
        given(this.evaluationRepository.findByWeek(anyString())).willReturn(this.evaluations);
        given(this.evaluationRepository.findByWeekAndEvaluateeId("2023-W31", 3)).willReturn(this.johnsEvaluations);
        given(this.evaluationRepository.findByWeekAndEvaluateeId("2023-W31", 4)).willReturn(this.ericsW31Evaluations);
        given(this.evaluationRepository.findByWeekAndEvaluateeId("2023-W31", 5)).willReturn(this.woodysEvaluations);
        given(this.evaluationRepository.findByWeekAndEvaluateeId("2023-W31", 6)).willReturn(this.amandasEvaluations);

        // When
        WeeklyPeerEvaluationReport report = this.evaluationService.generateWeeklyPeerEvaluationReportForSection(2, "2023-W31");

        // Then
        assertThat(report.getSectionName()).isEqualTo("2023-2024");
        assertThat(report.getPeerEvaluationAverages()).hasSize(5);
        assertThat(report.getPeerEvaluationAverages().get(0).getStudentId()).isEqualTo(3);
        assertThat(report.getPeerEvaluationAverages().get(0).getFirstName()).isEqualTo("John");
        assertThat(report.getPeerEvaluationAverages().get(0).getLastName()).isEqualTo("Smith");
        assertThat(report.getPeerEvaluationAverages().get(0).getEmail()).isEqualTo("j.smith@abc.edu");
        assertThat(report.getPeerEvaluationAverages().get(0).getTeamName()).isEqualTo("Team1");
        assertThat(report.getPeerEvaluationAverages().get(0).getAverageTotalScore()).isCloseTo(51.33, within(0.01));
        assertThat(report.getPeerEvaluationAverages().get(0).getRatingAverages().get(0).getAverageScore()).isCloseTo(10.0, within(0.01));
        assertThat(report.getPeerEvaluationAverages().get(0).getRatingAverages().get(1).getAverageScore()).isCloseTo(9.33, within(0.01));
        assertThat(report.getPeerEvaluationAverages().get(0).getRatingAverages().get(2).getAverageScore()).isCloseTo(10.0, within(0.01));
        assertThat(report.getPeerEvaluationAverages().get(0).getRatingAverages().get(3).getAverageScore()).isCloseTo(7.33, within(0.01));
        assertThat(report.getPeerEvaluationAverages().get(0).getRatingAverages().get(4).getAverageScore()).isCloseTo(7.33, within(0.01));
        assertThat(report.getPeerEvaluationAverages().get(0).getRatingAverages().get(5).getAverageScore()).isCloseTo(7.33, within(0.01));
        assertThat(report.getPeerEvaluationAverages().get(1).getAverageTotalScore()).isCloseTo(50.0, within(0.01));
        assertThat(report.getPeerEvaluationAverages().get(2).getAverageTotalScore()).isCloseTo(51.0, within(0.01));
        assertThat(report.getPeerEvaluationAverages().get(3).getAverageTotalScore()).isCloseTo(52.0, within(0.01));
        assertThat(report.getStudentsMissingPeerEvaluations()).contains("Amanda Wagner");
    }

    @Test
    void testGenerateWeeklyPeerEvaluationSummaryForStudent() {
        // Given
        given(this.studentRepository.findById(4)).willReturn(Optional.of(this.eric));
        given(this.evaluationRepository.findByWeekAndEvaluateeId("2023-W31", 4)).willReturn(this.ericsW31Evaluations);
        // When
        PeerEvaluationAverage peerEvaluationAverage = this.evaluationService.generateWeeklyPeerEvaluationSummaryForStudent(4, "2023-W31");

        // Then
        assertThat(peerEvaluationAverage.getStudentId()).isEqualTo(4);
        assertThat(peerEvaluationAverage.getWeek()).isEqualTo("2023-W31");
        assertThat(peerEvaluationAverage.getFirstName()).isEqualTo("Eric");
        assertThat(peerEvaluationAverage.getLastName()).isEqualTo("Wong");
        assertThat(peerEvaluationAverage.getEmail()).isEqualTo("e.wong@abc.edu");
        assertThat(peerEvaluationAverage.getTeamName()).isEqualTo("Team1");
        assertThat(peerEvaluationAverage.getPublicComments()).isEqualTo(List.of("Eric did a great job this week! I think he is on track.", "As Eric, I just do my best!", "eric eric eric, go, go go!"));
        assertThat(peerEvaluationAverage.getPrivateComments()).isNull();
        assertThat(peerEvaluationAverage.getAverageTotalScore()).isCloseTo(50.0, within(0.01));
    }

    @Test
    void testGenerateEvaluationSummariesForStudent() {
        // Given
        given(this.studentRepository.findById(4)).willReturn(Optional.of(this.eric));
        given(this.evaluationRepository.findByWeekAndEvaluateeId("2023-W31", 4)).willReturn(this.ericsW31Evaluations);
        given(this.evaluationRepository.findByWeekAndEvaluateeId("2023-W32", 4)).willReturn(this.ericsW32Evaluations);

        // Since the behavior of generateEvaluationSummariesForStudent() depends on the current user, we need to set the current user to an instructor
        Instructor instructor = new Instructor();
        instructor.setRoles("instructor");
        MyUserPrincipal myUserPrincipal = new MyUserPrincipal(instructor);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(myUserPrincipal, null, myUserPrincipal.getAuthorities()));
        SecurityContextHolder.setContext(securityContext);

        // When
        List<PeerEvaluationAverage> peerEvaluationAverages = this.evaluationService.generateEvaluationSummariesForStudent(4, "2023-W31", "2023-W33");

        // Then
        assertThat(peerEvaluationAverages).hasSize(3);
        assertThat(peerEvaluationAverages.get(0).getStudentId()).isEqualTo(4);
        assertThat(peerEvaluationAverages.get(0).getWeek()).isEqualTo("2023-W31");
        assertThat(peerEvaluationAverages.get(0).getFirstName()).isEqualTo("Eric");
        assertThat(peerEvaluationAverages.get(0).getLastName()).isEqualTo("Wong");
        assertThat(peerEvaluationAverages.get(0).getEmail()).isEqualTo("e.wong@abc.edu");
        assertThat(peerEvaluationAverages.get(0).getTeamName()).isEqualTo("Team1");
        assertThat(peerEvaluationAverages.get(0).getPublicComments()).isEqualTo(List.of("Eric did a great job this week! I think he is on track.", "As Eric, I just do my best!", "eric eric eric, go, go go!"));
        assertThat(peerEvaluationAverages.get(0).getAverageTotalScore()).isCloseTo(50.0, within(0.01));
        assertThat(peerEvaluationAverages.get(1).getWeek()).isEqualTo("2023-W32");
        assertThat(peerEvaluationAverages.get(1).getAverageTotalScore()).isCloseTo(41.66, within(0.01));
        assertThat(peerEvaluationAverages.get(2).getWeek()).isEqualTo("2023-W33");
        assertThat(peerEvaluationAverages.get(2).getAverageTotalScore()).isCloseTo(0.0, within(0.01));
    }

    @Test
    void testGetWeeklyEvaluationsForStudent() {
        // Given
        given(this.evaluationRepository.findByWeekAndEvaluateeId("2023-W31", 4)).willReturn(this.ericsW31Evaluations);

        // When
        List<PeerEvaluation> peerEvaluations = this.evaluationService.getWeeklyEvaluationsForStudent(4, "2023-W31");

        // Then
        assertThat(peerEvaluations).hasSize(3);
        assertThat(peerEvaluations.get(0).getEvaluatee().getId()).isEqualTo(4);
        assertThat(peerEvaluations.get(0).getEvaluator().getId()).isEqualTo(3);
        assertThat(peerEvaluations.get(0).getWeek()).isEqualTo("2023-W31");
        assertThat(peerEvaluations.get(0).getTotalScore()).isCloseTo(47.0, within(0.01));

        assertThat(peerEvaluations.get(1).getEvaluatee().getId()).isEqualTo(4);
        assertThat(peerEvaluations.get(1).getEvaluator().getId()).isEqualTo(4);
        assertThat(peerEvaluations.get(1).getWeek()).isEqualTo("2023-W31");
        assertThat(peerEvaluations.get(1).getTotalScore()).isCloseTo(54.0, within(0.01));

        assertThat(peerEvaluations.get(2).getEvaluatee().getId()).isEqualTo(4);
        assertThat(peerEvaluations.get(2).getEvaluator().getId()).isEqualTo(5);
        assertThat(peerEvaluations.get(2).getWeek()).isEqualTo("2023-W31");
        assertThat(peerEvaluations.get(2).getTotalScore()).isCloseTo(49.0, within(0.01));
    }

}