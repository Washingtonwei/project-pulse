package team.projectpulse.section;

import team.projectpulse.instructor.Instructor;
import team.projectpulse.instructor.InstructorRepository;
import team.projectpulse.rubric.Rubric;
import team.projectpulse.rubric.RubricRepository;
import team.projectpulse.student.Student;
import team.projectpulse.student.StudentRepository;
import team.projectpulse.system.UserUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Answers the course section questions the route rules ask: is the caller the section's course admin
 * ({@code isSectionOwner}); is the caller an instructor assigned to it ({@code isSectionInstructor}); is the
 * caller in it at all, enrolled as a student or assigned as an instructor ({@code canAccessSection}); and, before
 * an assignment route runs, does the named instructor or rubric belong to the section's course.
 *
 * <p>Every check fails closed: an id that names no row, or a link not yet established, is denied rather than
 * throwing. These methods run inside an {@code AuthorizationManager}, upstream of the {@code DispatcherServlet},
 * so an exception thrown here escapes the filter chain as a 500 that also tells the caller which ids exist.
 */
@Service
@Transactional
public class SectionSecurityService {

    private final SectionRepository sectionRepository;
    private final UserUtils userUtils;
    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;
    private final RubricRepository rubricRepository;


    public SectionSecurityService(SectionRepository sectionRepository, UserUtils userUtils, StudentRepository studentRepository, InstructorRepository instructorRepository, RubricRepository rubricRepository) {
        this.sectionRepository = sectionRepository;
        this.userUtils = userUtils;
        this.studentRepository = studentRepository;
        this.instructorRepository = instructorRepository;
        this.rubricRepository = rubricRepository;
    }

    public boolean isSectionOwner(Integer sectionId) {
        Section sectionToBeAccessed = this.sectionRepository.findById(sectionId).orElse(null);
        if (sectionToBeAccessed == null) {
            return false;
        }
        Integer adminId = sectionToBeAccessed.getCourse().getCourseAdmin().getId();
        Integer userIdFromJwt = this.userUtils.getUserId();
        return adminId.equals(userIdFromJwt);
    }

    public boolean isSectionInstructor(Integer sectionId) {
        Section sectionToBeAccessed = this.sectionRepository.findById(sectionId).orElse(null);
        if (sectionToBeAccessed == null) {
            return false;
        }
        Integer userIdFromJwt = this.userUtils.getUserId();
        // Get the list of instructorIds from the section object
        List<Integer> instructorIds = sectionToBeAccessed.getInstructors().stream()
                .map(Instructor::getId)
                .collect(Collectors.toList());
        return instructorIds.contains(userIdFromJwt);
    }

    public boolean canAccessSection(Integer sectionId) {
        Section sectionToBeAccessed = this.sectionRepository.findById(sectionId).orElse(null);
        if (sectionToBeAccessed == null) {
            return false;
        }
        Integer userIdFromJwt = this.userUtils.getUserId();
        boolean hasStudentRole = this.userUtils.hasRole("ROLE_student");
        boolean hasInstructorRole = this.userUtils.hasRole("ROLE_instructor");
        if (hasStudentRole) {
            Student student = this.studentRepository.findById(userIdFromJwt).orElse(null);
            if (student == null || student.getSection() == null) {
                return false;
            }
            // Check if the student's sectionId matches the sectionId from the request URI
            return sectionId.equals(student.getSection().getSectionId());
        } else if (hasInstructorRole) {
            // Get the list of instructorIds from the section object
            List<Integer> instructorIds = sectionToBeAccessed.getInstructors().stream()
                    .map(Instructor::getId)
                    .collect(Collectors.toList());
            return instructorIds.contains(userIdFromJwt);
        } else {
            return false;
        }
    }

    // Check if the instructor is in the same course as the section
    public boolean isSectionAndInstructorInSameCourse(Integer sectionId, Integer instructorId) {
        // Find the section by the sectionId
        Section section = this.sectionRepository.findById(sectionId).orElse(null);
        // Find the instructor by the instructorId
        Instructor instructor = this.instructorRepository.findById(instructorId).orElse(null);
        if (section == null || instructor == null) {
            return false;
        }
        return instructor.getCourses().contains(section.getCourse());
    }

    // Check if the rubric is in the same course as the section
    public boolean isSectionAndRubricInSameCourse(Integer sectionId, Integer rubricId) {
        Section section = this.sectionRepository.findById(sectionId).orElse(null);
        Rubric rubric = this.rubricRepository.findById(rubricId).orElse(null);
        if (section == null || rubric == null) {
            return false;
        }
        return section.getCourse().getCourseId().equals(rubric.getCourse().getCourseId());
    }

}
