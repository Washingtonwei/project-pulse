package team.projectpulse.activity;

import team.projectpulse.instructor.Instructor;
import team.projectpulse.section.Section;
import team.projectpulse.student.Student;
import team.projectpulse.system.UserUtils;
import team.projectpulse.team.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    ActivityRepository activityRepository;
    @Mock
    UserUtils userUtils;

    @InjectMocks
    ActivityService activityService;

    List<Activity> week31Activities;
    List<Activity> week32Activities;

    @BeforeEach
    void setUp() {
        // Create instructors
        Instructor instructor1 = new Instructor("bingyang", "Bingyang", "Wei", "b.wei@tcu.edu", "123456", true, "admin instructor");
        Instructor instructor2 = new Instructor("bill", "Bill", "Gates", "b.gates@tcu.edu", "123456", true, "instructor");

        // Create a section
        Section section1 = new Section("2022-2023", LocalDate.of(2022, 8, 15), LocalDate.of(2023, 5, 1));
        section1.setActiveWeeks(List.of("2022-W31", "2022-W32", "2022-W33", "2022-W34", "2022-W35"));
        section1.addInstructor(instructor1);
        section1.addInstructor(instructor2);

        Section section2 = new Section("2023-2024", LocalDate.of(2023, 8, 14), LocalDate.of(2024, 4, 29));
        section2.setActiveWeeks(List.of("2023-W31", "2023-W32", "2023-W33", "2023-W34", "2023-W35", "2023-W36", "2023-W37", "2023-W38", "2023-W39", "2023-W40"));
        section2.addInstructor(instructor1);
        section2.addInstructor(instructor2);

        // Create a team
        Team team1 = new Team("Team1", "Team 1 description", "https://www.team1.com");
        team1.setSection(section2);
        team1.addInstructor(instructor1);

        Team team2 = new Team("Team2", "Team 2 description", "https://www.team2.com");
        team2.setSection(section2);
        team2.addInstructor(instructor1);

        Team team3 = new Team("Team3", "Team 3 description", "https://www.team3.com");
        team3.setSection(section2);
        team3.addInstructor(instructor1);

        // Create students
        Student john = new Student("john", "John", "Smith", "j.smith@abc.edu", "123456", true, "student");
        Student eric = new Student("eric", "Eric", "Wong", "e.wong@abc.edu", "123456", true, "student");
        Student jerry = new Student("jerry", "Jerry", "Moon", "j.moon@abc.edu", "123456", true, "student");

        Student woody = new Student("woody", "Woody", "Allen", "w.allen@abc.edu", "123456", true, "student");
        Student amanda = new Student("amanda", "Amanda", "Wagner", "a.wagner@abc.edu", "123456", true, "student");
        Student cora = new Student("cora", "Cora", "Manning", "c.manning@abc.edu", "123456", true, "student");
        Student agustin = new Student("agustin", "Agustin", "Freeman", "a.freeman@abc.edu", "123456", true, "student");

        Student mavis = new Student("mavis", "Mavis", "Huber", "m.huber@abc.edu", "123456", true, "student");
        Student mary = new Student("mary", "Mary", "Vargas", "m.vargas@abc.edu", "123456", true, "student");
        Student rosendo = new Student("rosendo", "Rosendo", "Maxwell", "r.maxwell@abc.edu", "123456", true, "student");
        Student jan = new Student("jan", "Jan", "Mckinney", "j.mckinney@abc.edu", "123456", true, "student");

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

        section2.addStudent(cora);
        team2.addStudent(cora);

        section2.addStudent(agustin);
        team2.addStudent(agustin);

        section2.addStudent(mavis);
        team3.addStudent(mavis);

        section2.addStudent(mary);
        team3.addStudent(mary);

        section2.addStudent(rosendo);
        team3.addStudent(rosendo);

        section2.addStudent(jan);
        team3.addStudent(jan);

        // Create weekly activities
        Activity activity1 = new Activity(john, "2023-W31", team1, ActivityCategory.DEPLOYMENT, "Develop Login Feature", "Implement login functionality for the application", 12.0, 10.5, ActivityStatus.IN_PROGRESS);
        activity1.setActivityId(1);
        Activity activity2 = new Activity(john, "2023-W32", team1, ActivityCategory.TESTING, "Unit Testing for Login", "Write and execute unit tests for the login", 8.0, 9.0, ActivityStatus.COMPLETED);
        activity2.setActivityId(2);
        Activity activity3 = new Activity(eric, "2023-W31", team1, ActivityCategory.DOCUMENTATION, "Create API Documentation", "Document all API endpoints and their usage", 6.0, 5.5, ActivityStatus.IN_PROGRESS);
        activity3.setActivityId(3);
        Activity activity4 = new Activity(eric, "2023-W32", team1, ActivityCategory.DESIGN, "UI Mockups for Dashboard", "Design user interface mockups for the admin dashboard", 10.0, 10.5, ActivityStatus.IN_PROGRESS);
        activity4.setActivityId(4);
        Activity activity5 = new Activity(jerry, "2023-W31", team1, ActivityCategory.COMMUNICATION, "Weekly Team Meeting", "Attend and contribute to the weekly team progress meeting", 2.0, 1.5, ActivityStatus.COMPLETED);
        activity5.setActivityId(5);
        Activity activity6 = new Activity(woody, "2023-W31", team2, ActivityCategory.BUGFIX, "Fix Navigation Bugs", "Identify and fix bugs in the navigation menu", 4.0, 4.5, ActivityStatus.COMPLETED);
        activity6.setActivityId(6);
        Activity activity7 = new Activity(amanda, "2023-W31", team2, ActivityCategory.DEPLOYMENT, "Deploy Initial Version", "Deploy the initial version of the application to the staging environment", 5.0, 6.0, ActivityStatus.IN_PROGRESS);
        activity7.setActivityId(7);
        Activity activity8 = new Activity(mavis, "2023-W31", team3, ActivityCategory.LEARNING, "Learn Vue Basics", "Complete an online course on the basics of Vue.js", 3.0, 3.0, ActivityStatus.COMPLETED);
        activity8.setActivityId(8);

        this.week31Activities = List.of(activity1, activity3, activity5, activity6, activity7, activity8);
        this.week32Activities = List.of(activity2, activity4);
    }

    @Test
    void testFindByCriteria() {
        // Given
        Map<String, String> searchCriteria = Map.of("week", "2023-W31", "teamId", "1");
        PageImpl expectedPage = new PageImpl(week31Activities);

        given(this.userUtils.getUserSectionId()).willReturn(2);
        given(this.activityRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(expectedPage);
        // When
        Page<Activity> result = this.activityService.findByCriteria(searchCriteria, PageRequest.of(0, 20));

        // Then
        assertThat(result.getContent().size()).isEqualTo(6);
    }

    @Test
    void testFindActivityById() {
        // Given
        given(this.activityRepository.findById(1)).willReturn(Optional.of(this.week31Activities.get(0)));

        // When
        Activity result = this.activityService.findActivityById(1);

        // Then
        assertThat(result.getActivityId()).isEqualTo(1);
        assertThat(result.getCategory()).isEqualTo(ActivityCategory.DEPLOYMENT);
        assertThat(result.getActivity()).isEqualTo("Develop Login Feature");
        assertThat(result.getDescription()).isEqualTo("Implement login functionality for the application");
        assertThat(result.getPlannedHours()).isEqualTo(12.0);
        assertThat(result.getActualHours()).isEqualTo(10.5);
        assertThat(result.getStatus()).isEqualTo(ActivityStatus.IN_PROGRESS);
    }

    @Test
    void testSaveActivity() {
        // Given
        Activity newActivity = new Activity();
        newActivity.setStudent(new Student());
        newActivity.setWeek("2023-W31");
        newActivity.setTeam(new Team());
        newActivity.setCategory(ActivityCategory.DEPLOYMENT);
        newActivity.setActivity("Develop Login Feature");
        newActivity.setDescription("Implement login functionality for the application");
        newActivity.setPlannedHours(12.0);
        newActivity.setActualHours(10.5);
        newActivity.setStatus(ActivityStatus.IN_PROGRESS);

        given(this.activityRepository.save(newActivity)).willReturn(newActivity);

        // When
        Activity result = this.activityService.saveActivity(newActivity);

        // Then
        assertThat(result.getActivityId()).isNull();
        assertThat(result.getCategory()).isEqualTo(ActivityCategory.DEPLOYMENT);
        assertThat(result.getActivity()).isEqualTo("Develop Login Feature");
        assertThat(result.getDescription()).isEqualTo("Implement login functionality for the application");
        assertThat(result.getPlannedHours()).isEqualTo(12.0);
        assertThat(result.getActualHours()).isEqualTo(10.5);
        assertThat(result.getStatus()).isEqualTo(ActivityStatus.IN_PROGRESS);
    }

    @Test
    void testUpdateActivity() {
        // Given
        Activity update = new Activity();
        update.setActivityId(3);
        update.setCategory(ActivityCategory.DOCUMENTATION);
        update.setActivity("Create API Documentation (Updated)");
        update.setDescription("Document all API endpoints and their usage (Updated)");
        update.setPlannedHours(6.0);
        update.setActualHours(5.5);
        update.setStatus(ActivityStatus.COMPLETED);

        given(this.activityRepository.findById(3)).willReturn(Optional.of(this.week31Activities.get(1)));
        given(this.activityRepository.save(any())).willReturn(this.week31Activities.get(1));

        // When
        Activity result = this.activityService.updateActivity(3, update);

        // Then
        assertThat(result.getActivityId()).isEqualTo(3);
        assertThat(result.getCategory()).isEqualTo(update.getCategory());
        assertThat(result.getActivity()).isEqualTo(update.getActivity());
        assertThat(result.getDescription()).isEqualTo(update.getDescription());
        assertThat(result.getPlannedHours()).isEqualTo(update.getPlannedHours());
        assertThat(result.getActualHours()).isEqualTo(update.getActualHours());
        assertThat(result.getStatus()).isEqualTo(update.getStatus());
    }

    @Test
    void testDeleteActivity() {
        // Given
        given(this.activityRepository.findById(1)).willReturn(Optional.of(this.week31Activities.get(0)));
        doNothing().when(this.activityRepository).deleteById(1);

        // When
        this.activityService.deleteActivity(1);

        // Then
        verify(this.activityRepository, times(1)).deleteById(1);
    }

    @Test
    void testAddActivityComment() {
        // Given
        given(this.activityRepository.findById(1)).willReturn(Optional.of(this.week31Activities.get(0)));
        given(this.activityRepository.save(any())).willReturn(this.week31Activities.get(0));

        // When
        Activity result = this.activityService.addActivityComment(1, "Good progress on the login feature");

        // Then
        assertThat(result.getActivityId()).isEqualTo(1);
        assertThat(result.getComments()).isNotEmpty();
        assertThat(result.getComments()).isEqualTo("Good progress on the login feature");
    }

}