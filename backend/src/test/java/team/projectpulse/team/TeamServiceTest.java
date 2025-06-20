package team.projectpulse.team;

import team.projectpulse.instructor.Instructor;
import team.projectpulse.instructor.InstructorRepository;
import team.projectpulse.section.Section;
import team.projectpulse.student.Student;
import team.projectpulse.student.StudentRepository;
import team.projectpulse.system.UserUtils;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    TeamRepository teamRepository;
    @Mock
    StudentRepository studentRepository;
    @Mock
    InstructorRepository instructorRepository;
    @Mock
    UserUtils userUtils;

    @InjectMocks
    TeamService teamService;

    List<Team> teams;
    List<Student> students;
    List<Instructor> instructors;

    @BeforeEach
    void setUp() {
        // Create instructors
        Instructor instructor1 = new Instructor("bingyang", "Bingyang", "Wei", "b.wei@tcu.edu", "123456", true, "admin instructor");
        Instructor instructor2 = new Instructor("bill", "Bill", "Gates", "b.gates@tcu.edu", "123456", true, "instructor");
        this.instructors = List.of(instructor1, instructor2);

        // Create a section
        Section section1 = new Section("2022-2023", LocalDate.of(2022, 8, 15), LocalDate.of(2023, 5, 1), true, DayOfWeek.MONDAY, LocalTime.of(23, 59), DayOfWeek.TUESDAY, LocalTime.of(23, 59));
        section1.setActiveWeeks(List.of("2022-W31", "2022-W32", "2022-W33", "2022-W34", "2022-W35"));
        section1.addInstructor(instructor1);
        section1.addInstructor(instructor2);

        Section section2 = new Section("2023-2024", LocalDate.of(2023, 8, 14), LocalDate.of(2024, 4, 29), true, DayOfWeek.MONDAY, LocalTime.of(23, 59), DayOfWeek.TUESDAY, LocalTime.of(23, 59));
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

        this.teams = List.of(team1, team2, team3);

        // Create students
        Student john = new Student("john", "John", "Smith", "j.smith@abc.edu", "123456", true, "student");
        Student eric = new Student("eric", "Eric", "Hudson", "e.hudson@abc.edu", "123456", true, "student");
        Student jerry = new Student("jerry", "Jerry", "Moon", "j.moon@abc.edu", "123456", true, "student");
        Student woody = new Student("woody", "Woody", "Allen", "w.allen@abc.edu", "123456", true, "student");

        this.students = List.of(john, eric, jerry, woody);

        section2.addStudent(john);
        team1.addStudent(john);

        section2.addStudent(eric);
        team1.addStudent(eric);
    }

    @Test
    void testFindByCriteria() {
        // Given
        Map<String, String> searchCriteria = Map.of("teamName", "Team1");
        PageImpl expectedPage = new PageImpl(List.of(this.teams.get(0)));

        given(this.userUtils.getUserSectionId()).willReturn(2);
        given(this.teamRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(expectedPage);
        // When
        Page<Team> result = this.teamService.findByCriteria(searchCriteria, PageRequest.of(0, 20));

        // Then
        assertThat(result.getContent().size()).isEqualTo(1);
    }

    @Test
    void testFindTeamById() {
        // Given
        given(this.teamRepository.findById(1)).willReturn(Optional.ofNullable(this.teams.get(0)));

        // When
        Team result = this.teamService.findTeamById(1);

        // Then
        assertThat(result.getTeamName()).isEqualTo("Team1");
    }

    @Test
    void testSaveTeam() {
        // Given
        Team newTeam = new Team("Team4", "Team 4 description", "https://www.team4.com");
        newTeam.setSection(this.teams.get(0).getSection());

        given(this.teamRepository.save(newTeam)).willReturn(newTeam);

        // When
        Team result = this.teamService.saveTeam(newTeam);

        // Then
        assertThat(result.getTeamName()).isEqualTo("Team4");
    }

    @Test
    void testUpdateTeam() {
        // Given
        Team update = new Team("Team1 (updated)", "Team 1 description (updated)", "https://www.team1.com (updated)");
        update.setSection(this.teams.get(0).getSection());

        given(this.teamRepository.findById(1)).willReturn(Optional.ofNullable(this.teams.get(0)));
        given(this.teamRepository.save(this.teams.get(0))).willReturn(this.teams.get(0));

        // When
        Team result = this.teamService.updateTeam(1, update);

        // Then
        assertThat(result.getTeamName()).isEqualTo(update.getTeamName());
        assertThat(result.getDescription()).isEqualTo(update.getDescription());
        assertThat(result.getTeamWebsiteUrl()).isEqualTo(update.getTeamWebsiteUrl());
    }

    @Test
    void testDeleteTeam() {
        // Given
        given(this.teamRepository.findById(1)).willReturn(Optional.ofNullable(this.teams.get(0)));
        doNothing().when(this.teamRepository).deleteById(1);

        // When
        this.teamService.deleteTeam(1);

        // Then
        verify(this.teamRepository, times(1)).deleteById(1);
    }

    @Test
    void testAssignStudentToTeam() {
        // Given
        Student jerry = this.students.get(2);
        Team team1 = this.teams.get(0);
        given(this.teamRepository.findById(1)).willReturn(Optional.ofNullable(team1));
        given(this.studentRepository.findById(3)).willReturn(Optional.ofNullable(jerry)); // Jerry

        // When
        this.teamService.assignStudentToTeam(1, 3);

        // Then
        assertThat(team1.getStudents().size()).isEqualTo(3);
        assertThat(team1.getStudents().get(2).getFirstName()).isEqualTo("Jerry");
        assertThat(jerry.getTeam()).isEqualTo(team1);

    }

    @Test
    void testRemoveStudentFromTeam() {
        // Given
        Student john = this.students.get(0);
        Team team1 = this.teams.get(0);
        given(this.teamRepository.findById(1)).willReturn(Optional.of(team1));
        given(this.studentRepository.findById(1)).willReturn(Optional.of(john)); // John

        // When
        this.teamService.removeStudentFromTeam(1, 1);

        // Then
        assertThat(team1.getStudents().size()).isEqualTo(1);
        assertThat(john.getTeam()).isNull();
    }

    @Test
    void testAssignInstructorToTeam() {
        // Given
        Team team1 = this.teams.get(0);
        Instructor bill = this.instructors.get(1);
        given(this.teamRepository.findById(1)).willReturn(Optional.ofNullable(team1));
        given(this.instructorRepository.findById(2)).willReturn(Optional.ofNullable(bill));

        // When
        this.teamService.assignInstructorToTeam(1, 2);

        // Then
        assertThat(team1.getInstructor()).isEqualTo(bill);
        assertThat(bill.getTeams().size()).isEqualTo(1);
    }

}