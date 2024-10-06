package team.projectpulse.instructor;

import team.projectpulse.course.Course;
import team.projectpulse.course.CourseRepository;
import team.projectpulse.section.Section;
import team.projectpulse.section.SectionRepository;
import team.projectpulse.system.UserUtils;
import team.projectpulse.system.exception.ObjectNotFoundException;
import team.projectpulse.user.userinvitation.UserInvitationService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
@Transactional
public class InstructorService {

    private final InstructorRepository instructorRepository;
    private final UserInvitationService userInvitationService;
    private final PasswordEncoder passwordEncoder;
    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final UserUtils userUtils;


    public InstructorService(InstructorRepository instructorRepository, UserInvitationService userInvitationService, PasswordEncoder passwordEncoder, SectionRepository sectionRepository, CourseRepository courseRepository, UserUtils userUtils) {
        this.instructorRepository = instructorRepository;
        this.userInvitationService = userInvitationService;
        this.passwordEncoder = passwordEncoder;
        this.sectionRepository = sectionRepository;
        this.courseRepository = courseRepository;
        this.userUtils = userUtils;
    }

    public Page<Instructor> findByCriteria(Map<String, String> searchCriteria, Pageable pageable) {
        Instructor instructor = this.userUtils.getInstructor();

        Course course = instructor.getDefaultCourse();

        Specification<Instructor> spec = Specification.where(null);

        if (StringUtils.hasLength(searchCriteria.get("firstName"))) {
            spec = spec.and(InstructorSpecs.hasFirstName(searchCriteria.get("firstName")));
        }

        if (StringUtils.hasLength(searchCriteria.get("lastName"))) {
            spec = spec.and(InstructorSpecs.hasLastName(searchCriteria.get("lastName")));
        }

        if (StringUtils.hasLength(searchCriteria.get("email"))) {
            spec = spec.and(InstructorSpecs.hasEmail(searchCriteria.get("email")));
        }

        spec = spec.and(InstructorSpecs.hasCourse(course)); // Only show instructors that belong to the default course of the instructor

        return this.instructorRepository.findAll(spec, pageable);
    }

    public Instructor findInstructorById(Integer instructorId) {
        return this.instructorRepository.findById(instructorId)
                .orElseThrow(() -> new ObjectNotFoundException("instructor", instructorId));
    }

    public Instructor saveInstructor(Instructor newInstructor, Integer courseId, String registrationToken, String role) {
        // Validate user invitation
        this.userInvitationService.validateUserInvitation(newInstructor.getEmail(), registrationToken, courseId, null, role);

        // Password rule: At least 6 characters, contains at least one letter and one number.
        if (!newInstructor.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$")) {
            throw new IllegalArgumentException("Password must contain at least one letter and one number, and at least 6 characters.");
        }

        Course course = this.courseRepository.findById(courseId)
                .orElseThrow(() -> new ObjectNotFoundException("course", courseId));
        course.addInstructor(newInstructor);
        newInstructor.setEnabled(true);
        newInstructor.setRoles("instructor");
        newInstructor.setPassword(this.passwordEncoder.encode(newInstructor.getPassword()));
        Instructor savedInstructor = this.instructorRepository.save(newInstructor);
        // Delete the user invitation after the instructor is saved.
        this.userInvitationService.deleteUserInvitation(newInstructor.getEmail());
        return savedInstructor;
    }

    public Instructor updateInstructor(Integer instructorId, Instructor update) {
        Instructor oldInstructor = this.instructorRepository.findById(instructorId)
                .orElseThrow(() -> new ObjectNotFoundException("instructor", instructorId));

        // If the user is not an admin, then the user can only update her username, first rubricName, last rubricName, and email
        oldInstructor.setUsername(update.getUsername());
        oldInstructor.setFirstName(update.getFirstName());
        oldInstructor.setLastName(update.getLastName());
        oldInstructor.setEmail(update.getEmail());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // If the user is an admin, then the user can update all the fields
        if (authentication.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_admin"))) {
            oldInstructor.setEnabled(update.isEnabled());
            oldInstructor.setRoles(update.getRoles());
        }

        return this.instructorRepository.save(oldInstructor);
    }

    public void deleteInstructor(Integer instructorId) {
        this.instructorRepository.findById(instructorId)
                .orElseThrow(() -> new ObjectNotFoundException("instructor", instructorId));
        this.instructorRepository.deleteById(instructorId);
    }

    public void setDefaultSection(Integer sectionId) {
        Instructor instructor = this.userUtils.getInstructor();
        Section section = this.sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ObjectNotFoundException("section", sectionId));
        // Make sure the section belongs to the default course of the instructor
        if (!instructor.getDefaultCourse().getCourseId().equals(section.getCourse().getCourseId())) {
            throw new IllegalArgumentException("The section does not belong to the default course of the instructor. Change the default course first.");
        }
        instructor.setDefaultSection(section);
    }

    public void setDefaultCourse(Integer courseId) {
        Instructor instructor = this.userUtils.getInstructor();
        Course course = this.courseRepository.findById(courseId)
                .orElseThrow(() -> new ObjectNotFoundException("course", courseId));
        instructor.setDefaultCourse(course);
        instructor.setDefaultSection(null); // Reset the default section
    }

}
