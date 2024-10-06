package team.projectpulse.section;

import team.projectpulse.instructor.Instructor;
import team.projectpulse.instructor.InstructorRepository;
import team.projectpulse.rubric.Rubric;
import team.projectpulse.rubric.RubricRepository;
import team.projectpulse.student.Student;
import team.projectpulse.student.StudentRepository;
import team.projectpulse.system.UserUtils;
import team.projectpulse.system.exception.ObjectNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
        Section sectionToBeAccessed = this.sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ObjectNotFoundException("section", sectionId));
        Integer adminId = sectionToBeAccessed.getCourse().getCourseAdmin().getId();
        Integer userIdFromJwt = this.userUtils.getUserId();
        return adminId.equals(userIdFromJwt);
    }

    public boolean isSectionInstructor(Integer sectionId) {
        Section sectionToBeAccessed = this.sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ObjectNotFoundException("section", sectionId));
        Integer userIdFromJwt = this.userUtils.getUserId();
        // Get the list of instructorIds from the section object
        List<Integer> instructorIds = sectionToBeAccessed.getInstructors().stream()
                .map(Instructor::getId)
                .collect(Collectors.toList());
        return instructorIds.contains(userIdFromJwt);
    }

    public boolean canAccessSection(Integer sectionId) {
        Section sectionToBeAccessed = this.sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ObjectNotFoundException("section", sectionId));
        Integer userIdFromJwt = this.userUtils.getUserId();
        boolean hasStudentRole = this.userUtils.hasRole("ROLE_student");
        boolean hasInstructorRole = this.userUtils.hasRole("ROLE_instructor");
        if (hasStudentRole) {
            Student student = this.studentRepository.findById(userIdFromJwt)
                    .orElseThrow(() -> new ObjectNotFoundException("student", userIdFromJwt));
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
        Section section = this.sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ObjectNotFoundException("section", sectionId));
        // Find the instructor by the instructorId
        Instructor instructor = this.instructorRepository.findById(instructorId)
                .orElseThrow(() -> new ObjectNotFoundException("instructor", instructorId));
        return instructor.getCourses().contains(section.getCourse());
    }

    // Check if the rubric is in the same course as the section
    public boolean isSectionAndRubricInSameCourse(Integer sectionId, Integer rubricId) {
        Section section = this.sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ObjectNotFoundException("section", sectionId));
        Rubric rubric = this.rubricRepository.findById(rubricId)
                .orElseThrow(() -> new ObjectNotFoundException("rubric", rubricId));
        return section.getCourse().getCourseId().equals(rubric.getCourse().getCourseId());
    }

}
