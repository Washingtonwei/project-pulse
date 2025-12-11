package team.projectpulse.section;

import team.projectpulse.instructor.Instructor;
import team.projectpulse.instructor.InstructorRepository;
import team.projectpulse.rubric.Rubric;
import team.projectpulse.rubric.RubricRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SectionServiceTest {

    @Mock
    SectionRepository sectionRepository;

    @Mock
    RubricRepository rubricRepository;

    @Mock
    InstructorRepository instructorRepository;

    @Mock
    UserUtils userUtils;

    @InjectMocks
    SectionService sectionService;

    private List<Section> sections;

    private Instructor instructor1;

    @BeforeEach
    void setUp() {
        Rubric rubric = new Rubric("Peer Eval Rubric v1");

        Instructor instructor1 = new Instructor("bingyang", "Bingyang", "Wei", "b.wei@tcu.edu", "123456", true, "admin instructor");
        Instructor instructor2 = new Instructor("bill", "Bill", "Gates", "b.gates@tcu.edu", "123456", true, "instructor");

        this.instructor1 = instructor1;

        // Create a section
        Section section1 = new Section("2022-2023", LocalDate.of(2022, 8, 15), LocalDate.of(2023, 5, 1), true, DayOfWeek.MONDAY, LocalTime.of(23, 59), DayOfWeek.TUESDAY, LocalTime.of(23, 59));
        section1.setSectionId(1);
        section1.setActiveWeeks(List.of("2022-W31", "2022-W32", "2022-W33", "2022-W34", "2022-W35"));
        section1.setRubric(rubric);
        section1.addInstructor(instructor1);
        section1.addInstructor(instructor2);

        Section section2 = new Section("2023-2024", LocalDate.of(2023, 8, 14), LocalDate.of(2024, 4, 29), true, DayOfWeek.MONDAY, LocalTime.of(23, 59), DayOfWeek.TUESDAY, LocalTime.of(23, 59));
        section2.setSectionId(2);
        section2.setActiveWeeks(List.of("2023-W31", "2023-W32", "2023-W33", "2023-W34", "2023-W35", "2023-W36", "2023-W37", "2023-W38", "2023-W39", "2023-W40"));
        section2.setRubric(rubric);
        section2.addInstructor(instructor1);
        section2.addInstructor(instructor2);

        this.sections = List.of(section1, section2);
    }

    @Test
    void testFindByCriteria() {
        // Given
        Map<String, String> searchCriteria = Map.of("sectionName", "2023");
        PageImpl expectedPage = new PageImpl(this.sections);

        given(this.userUtils.getUserCourseId()).willReturn(1);
        given(this.sectionRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(expectedPage);
        // When
        Page<Section> result = this.sectionService.findByCriteria(searchCriteria, PageRequest.of(0, 20));

        // Then
        assertThat(result.getContent().size()).isEqualTo(2);
        assertThat(result.getContent().get(0).getSectionName()).isEqualTo("2022-2023");
        assertThat(result.getContent().get(1).getSectionName()).isEqualTo("2023-2024");
    }

    @Test
    void testFindSectionById() {
        // Given
        given(this.sectionRepository.findById(1)).willReturn(Optional.of(this.sections.get(0)));

        // When
        Section result = this.sectionService.findSectionById(1);

        // Then
        assertThat(result.getSectionName()).isEqualTo("2022-2023");
    }

    @Test
    void testSaveSection() {
        // Given
        Section newSection = new Section("2024-2025", LocalDate.of(2024, 8, 15), LocalDate.of(2025, 5, 1), true, DayOfWeek.MONDAY, LocalTime.of(23, 59), DayOfWeek.TUESDAY, LocalTime.of(23, 59));
        given(this.sectionRepository.save(newSection)).willReturn(newSection);

        // When
        Section result = this.sectionService.saveSection(newSection);

        // Then
        assertThat(result.getSectionName()).isEqualTo("2024-2025");
    }

    @Test
    void testUpdateSection() {
        // Given
        Section update = new Section("2023-2024 (updated)", LocalDate.of(2023, 8, 15), LocalDate.of(2024, 5, 1), true, DayOfWeek.MONDAY, LocalTime.of(23, 59), DayOfWeek.TUESDAY, LocalTime.of(23, 59));
        given(this.sectionRepository.findById(1)).willReturn(Optional.of(this.sections.get(0)));
        given(this.sectionRepository.save(any(Section.class))).willReturn(this.sections.get(0));

        // When
        Section result = this.sectionService.updateSection(1, update);

        // Then
        assertThat(result.getSectionName()).isEqualTo(update.getSectionName());
        assertThat(result.getStartDate()).isEqualTo(update.getStartDate());
        assertThat(result.getEndDate()).isEqualTo(update.getEndDate());
    }

    @Test
    void testDeleteSection() {
        // Given
        given(this.sectionRepository.findById(1)).willReturn(Optional.of(this.sections.get(0)));
        doNothing().when(this.sectionRepository).deleteById(1);

        // When
        this.sectionService.deleteSection(1);

        // Then
        verify(this.sectionRepository, times(1)).deleteById(1);
    }

    @Test
    void testSetUpActiveWeeks() {
        // Given
        List<String> activeWeeks = List.of("2023-W31", "2023-W32", "2023-W33", "2023-W34", "2023-W35", "2023-W36", "2023-W37", "2023-W38", "2023-W39", "2023-W40");
        given(this.sectionRepository.findById(1)).willReturn(Optional.of(this.sections.get(0)));
        given(this.sectionRepository.save(any(Section.class))).willReturn(this.sections.get(0));

        // When
        this.sectionService.setUpActiveWeeks(1, activeWeeks);

        // Then
        assertThat(this.sections.get(0).getActiveWeeks().size()).isEqualTo(10);
    }

    @Test
    void testAssignRubric() {
        // Given
        Rubric rubric = new Rubric("Peer Eval Rubric v2");
        rubric.setRubricId(2);
        given(this.rubricRepository.findById(2)).willReturn(Optional.of(rubric));
        given(this.sectionRepository.findById(1)).willReturn(Optional.of(this.sections.get(0)));
        given(this.sectionRepository.save(any(Section.class))).willReturn(this.sections.get(0));

        // When
        this.sectionService.assignRubric(1, 2);

        // Then
        assertThat(this.sections.get(0).getRubric().getRubricName()).isEqualTo("Peer Eval Rubric v2");
    }

    @Test
    void testAssignInstructor() {
        // Given
        Instructor instructor = new Instructor("elon", "Elon", "Musk", "e.musk@abc.edu", "123456", true, "instructor");
        instructor.setId(3);

        given(this.instructorRepository.findById(3)).willReturn(Optional.of(instructor));
        given(this.sectionRepository.findById(1)).willReturn(Optional.of(this.sections.get(0)));
        given(this.sectionRepository.save(any(Section.class))).willReturn(this.sections.get(0));

        // When
        this.sectionService.assignInstructor(1, 3);

        // Then
        assertThat(this.sections.get(0).getInstructors().size()).isEqualTo(3);
        assertThat(instructor.getSections().size()).isEqualTo(1);
    }

    @Test
    void testRemoveInstructorSuccess() {
        // Given
        given(this.instructorRepository.findById(1)).willReturn(Optional.of(this.instructor1));
        given(this.sectionRepository.findById(1)).willReturn(Optional.of(this.sections.get(0)));
        given(this.sectionRepository.save(any(Section.class))).willReturn(this.sections.get(0));

        // When
        this.sectionService.removeInstructor(1, 1);

        // Then
        assertThat(this.sections.get(0).getInstructors().size()).isEqualTo(1);
        assertThat(this.instructor1.getSections().size()).isEqualTo(1);
        verify(this.sectionRepository, times(1)).save(any(Section.class));
    }

    @Test
    void testRemoveInstructorFailsWhenLastInstructor() {
        // Given - Create a section with only one instructor
        Section sectionWithOneInstructor = new Section("Test Section",
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 12, 31),
                true,
                DayOfWeek.MONDAY,
                LocalTime.of(23, 59),
                DayOfWeek.TUESDAY,
                LocalTime.of(23, 59));
        sectionWithOneInstructor.setSectionId(3);
        sectionWithOneInstructor.addInstructor(this.instructor1);

        given(this.instructorRepository.findById(this.instructor1.getId())).willReturn(Optional.of(this.instructor1));
        given(this.sectionRepository.findById(3)).willReturn(Optional.of(sectionWithOneInstructor));

        // When/Then
        assertThrows(
                IllegalStateException.class,
                () -> this.sectionService.removeInstructor(3, this.instructor1.getId())
        );
    }

//    @Test
//    void testFindDefaultSection() {
//        // Given
//        given(this.sectionRepository.findByIsDefaultSection(true)).willReturn(Optional.of(this.sections.get(0)));
//
//        // When
//        Section result = this.sectionService.findDefaultSection();
//
//        // Then
//        assertThat(result.getSectionName()).isEqualTo("2022-2023");
//        assertThat(result.isDefaultSection()).isTrue();
//    }
//
//    @Test
//    void testSetDefaultSection() {
//        // Given
//        given(this.sectionRepository.findById(2)).willReturn(Optional.of(this.sections.get(1)));
//        given(this.sectionRepository.save(any(Section.class))).willReturn(this.sections.get(1));
//        given(this.sectionRepository.findAll()).willReturn(this.sections);
//
//        // When
//        this.sectionService.setDefaultSection(2);
//
//        // Then
//        assertThat(this.sections.get(0).isDefaultSection()).isFalse();
//        assertThat(this.sections.get(1).isDefaultSection()).isTrue();
//    }

    @Test
    void testGetInstructors() {
        // Given
        given(this.sectionRepository.findById(1)).willReturn(Optional.of(this.sections.get(0)));

        // When
        List<Instructor> result = this.sectionService.getInstructors(1);

        // Then
        assertThat(result).hasSize(2);
    }

}