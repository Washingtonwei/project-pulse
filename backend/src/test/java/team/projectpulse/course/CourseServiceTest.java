package team.projectpulse.course;

import team.projectpulse.instructor.Instructor;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    CourseRepository courseRepository;
    @Mock
    UserUtils userUtils;

    @InjectMocks
    CourseService courseService;

    private List<Course> courses;

    @BeforeEach
    void setUp() {
        Course course1 = new Course("COSC 40993 Senior Design", "Senior design project course for Computer Science, Computer Information Technology, and Data Science majors");
        Course course2 = new Course("CITE 30363 Web Tech", "Course project for Web Technology");
        Course course3 = new Course("EE 30323 Capstone", "Capstone project course for EE majors");

        this.courses = List.of(course1, course2, course3);

    }

    @Test
    void findByCriteria() {
        // Given
        Map<String, String> searchCriteria = new HashMap<>();
        PageImpl expectedPage = new PageImpl(this.courses);

        given(this.userUtils.getInstructor()).willReturn(new Instructor());
        given(this.courseRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(expectedPage);
        // When
        Page<Course> result = this.courseService.findByCriteria(searchCriteria, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent().size()).isEqualTo(3);
    }

    @Test
    void saveCourse() {
        // Given
        Course newCourse = new Course("New Course", "New Course Description");
        given(this.courseRepository.save(newCourse)).willReturn(newCourse);

        // When
        Course result = this.courseService.saveCourse(newCourse);

        // Then
        assertThat(result.getCourseName()).isEqualTo("New Course");
        assertThat(result.getCourseDescription()).isEqualTo("New Course Description");
    }

    @Test
    void findCourseById() {
        // Given
        given(this.courseRepository.findById(1)).willReturn(Optional.of(this.courses.get(0)));

        // When
        Course result = this.courseService.findCourseById(1);

        // Then
        assertThat(result.getCourseName()).isEqualTo("COSC 40993 Senior Design");
        assertThat(result.getCourseDescription()).isEqualTo("Senior design project course for Computer Science, Computer Information Technology, and Data Science majors");
    }

    @Test
    void updateCourse() {
        // Given
        Course update = new Course("COSC 40993 Senior Design (updated)", "Senior design project course for Computer Science, Computer Information Technology, and Data Science majors (updated)");
        given(this.courseRepository.findById(1)).willReturn(Optional.of(this.courses.get(0)));
        given(this.courseRepository.save(any())).willReturn(this.courses.get(0));

        // When
        Course result = this.courseService.updateCourse(1, update);

        // Then
        assertThat(result.getCourseName()).isEqualTo(update.getCourseName());
        assertThat(result.getCourseDescription()).isEqualTo(update.getCourseDescription());
    }

    @Test
    void deleteCourse() {
        // Given
        given(this.courseRepository.findById(1)).willReturn(Optional.of(this.courses.get(0)));
        doNothing().when(this.courseRepository).deleteById(1);

        // When
        this.courseService.deleteCourse(1);

        // Then
        verify(this.courseRepository, times(1)).deleteById(1);
    }

}