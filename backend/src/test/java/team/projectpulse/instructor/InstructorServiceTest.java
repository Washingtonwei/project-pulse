package team.projectpulse.instructor;

import team.projectpulse.course.Course;
import team.projectpulse.course.CourseRepository;
import team.projectpulse.section.Section;
import team.projectpulse.section.SectionRepository;
import team.projectpulse.system.UserUtils;
import team.projectpulse.user.MyUserPrincipal;
import team.projectpulse.user.userinvitation.UserInvitationService;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstructorServiceTest {

    @Mock
    InstructorRepository instructorRepository;
    @Mock
    UserInvitationService userInvitationService;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    SectionRepository sectionRepository;
    @Mock
    CourseRepository courseRepository;
    @Mock
    UserUtils userUtils;

    @InjectMocks
    private InstructorService instructorService;

    List<Instructor> instructors;
    Course course;
    Section section;


    @BeforeEach
    void setUp() {
        Instructor instructor1 = new Instructor("bingyang", "Bingyang", "Wei", "b.wei@tcu.edu", "123456", true, "admin instructor");
        Instructor instructor2 = new Instructor("bill", "Bill", "Gates", "b.gates@tcu.edu", "123456", true, "instructor");

        this.course = new Course();
        this.course.setCourseId(1);
        this.course.setCourseName("Course 1");
        this.course.setCourseAdmin(instructor1);
        this.course.addInstructor(instructor2);
        instructor1.setDefaultCourse(this.course);
        instructor2.setDefaultCourse(this.course);

        this.section = new Section();
        this.section.setSectionId(1);
        this.section.setSectionName("Section 1");
        course.addSection(this.section);
        this.section.addInstructor(instructor1);
        this.section.addInstructor(instructor2);
        instructor1.setDefaultSection(this.section);
        instructor2.setDefaultSection(this.section);

        this.instructors = List.of(instructor1, instructor2);
    }

    @Test
    void testFindByCriteria() {
        // Given
        Map<String, String> searchCriteria = Map.of("email", "b.wei@tcu.edu");
        PageImpl expectedPage = new PageImpl(List.of(this.instructors.get(0)));

        given(this.userUtils.getInstructor()).willReturn(this.instructors.get(0));
        given(this.instructorRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(expectedPage);
        // When
        Page<Instructor> result = this.instructorService.findByCriteria(searchCriteria, PageRequest.of(0, 20));

        // Then
        assertThat(result.getContent().size()).isEqualTo(1);
    }

    @Test
    void testFindInstructorById() {
        // Given
        given(this.instructorRepository.findById(1)).willReturn(Optional.of(this.instructors.get(0)));

        // When
        Instructor result = this.instructorService.findInstructorById(1);

        // Then
        assertThat(result.getFirstName()).isEqualTo("Bingyang");
    }

    @Test
    void testSaveInstructor() {
        // Given
        Instructor instructor = new Instructor("elon", "Elon", "Musk", "e.musk@tcu.edu", "Abc123456", true, "instructor");
        given(this.passwordEncoder.encode(anyString())).willReturn("Encoded Password");
        given(this.instructorRepository.save(instructor)).willReturn(instructor);
        doNothing().when(this.userInvitationService).validateUserInvitation(anyString(), anyString(), any(), any(), anyString());
        given(this.courseRepository.findById(1)).willReturn(Optional.of(this.course));

        // When
        Instructor result = this.instructorService.saveInstructor(instructor, 1, "token", "instructor");

        // Then
        assertThat(result.getFirstName()).isEqualTo("Elon");
        assertThat(result.getRoles()).isEqualTo("instructor");
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.getCourses()).size().isEqualTo(1);
    }

    @Test
    void testInstructorUpdatesOwnInfo() {
        // Given
        Instructor update = new Instructor("bill", "Bill (updated)", "Gates", "b.gates@tcu.edu", "", false, "admin instructor");
        Instructor bill = this.instructors.get(1);
        given(this.instructorRepository.findById(2)).willReturn(Optional.of(bill));
        given(this.instructorRepository.save(any(Instructor.class))).willReturn(bill);

        Instructor instructor = new Instructor();
        instructor.setRoles("instructor");
        MyUserPrincipal myUserPrincipal = new MyUserPrincipal(instructor);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(myUserPrincipal, null, myUserPrincipal.getAuthorities()));
        SecurityContextHolder.setContext(securityContext);

        // When
        Instructor result = this.instructorService.updateInstructor(2, update);

        // Then
        assertThat(result.getFirstName()).isEqualTo(update.getFirstName());
        assertThat(result.getRoles()).isNotEqualTo(update.getRoles());
        assertThat(result.isEnabled()).isNotEqualTo(update.isEnabled());
    }

    @Test
    void testAdminUpdatesInstructor() {
        // Given
        Instructor update = new Instructor("bill", "Bill (updated)", "Gates", "b.gates@tcu.edu", "", false, "admin instructor");
        Instructor bill = this.instructors.get(1);
        given(this.instructorRepository.findById(2)).willReturn(Optional.of(bill));
        given(this.instructorRepository.save(any(Instructor.class))).willReturn(bill);

        Instructor instructor = new Instructor();
        instructor.setRoles("admin");
        MyUserPrincipal myUserPrincipal = new MyUserPrincipal(instructor);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(myUserPrincipal, null, myUserPrincipal.getAuthorities()));
        SecurityContextHolder.setContext(securityContext);

        // When
        Instructor result = this.instructorService.updateInstructor(2, update);

        // Then
        assertThat(result.getFirstName()).isEqualTo(update.getFirstName());
        assertThat(result.getRoles()).isEqualTo(update.getRoles());
        assertThat(result.isEnabled()).isEqualTo(update.isEnabled());
    }

    @Test
    void testDeleteInstructor() {
        // Given
        Instructor instructor = this.instructors.get(0);
        given(this.instructorRepository.findById(1)).willReturn(Optional.of(instructor));
        doNothing().when(this.instructorRepository).deleteById(1);

        // When
        this.instructorService.deleteInstructor(1);

        // Then
        verify(this.instructorRepository, times(1)).deleteById(1);
    }

    @Test
    void testSetDefaultSection() {
        // Given
        given(this.userUtils.getInstructor()).willReturn(this.instructors.get(0));
        given(this.sectionRepository.findById(1)).willReturn(Optional.of(this.section));

        // When
        this.instructorService.setDefaultSection(1);

        // Then
        assertThat(this.instructors.get(0).getDefaultSection().getSectionId()).isEqualTo(1);
    }

    @Test
    void testSetDefaultCourse() {
        // Given
        given(this.userUtils.getInstructor()).willReturn(this.instructors.get(0));
        given(this.courseRepository.findById(1)).willReturn(Optional.of(this.course));

        // When
        this.instructorService.setDefaultCourse(1);

        // Then
        assertThat(this.instructors.get(0).getDefaultCourse().getCourseId()).isEqualTo(1);
    }

}