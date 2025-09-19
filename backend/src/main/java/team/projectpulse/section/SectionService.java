package team.projectpulse.section;

import team.projectpulse.instructor.Instructor;
import team.projectpulse.instructor.InstructorRepository;
import team.projectpulse.rubric.Rubric;
import team.projectpulse.rubric.RubricRepository;
import team.projectpulse.system.UserUtils;
import team.projectpulse.system.exception.ObjectNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SectionService {

    private final SectionRepository sectionRepository;
    private final RubricRepository rubricRepository;
    private final InstructorRepository instructorRepository;
    private final UserUtils userUtils;


    public SectionService(SectionRepository sectionRepository, RubricRepository rubricRepository, InstructorRepository instructorRepository, UserUtils userUtils) {
        this.sectionRepository = sectionRepository;
        this.rubricRepository = rubricRepository;
        this.instructorRepository = instructorRepository;
        this.userUtils = userUtils;
    }

    public Page<Section> findByCriteria(Map<String, String> searchCriteria, Pageable pageable) {
        Integer courseId = this.userUtils.getUserCourseId();

        Specification<Section> spec = Specification.where(null);

        if (StringUtils.hasLength(searchCriteria.get("sectionName"))) {
            spec = spec.and(SectionSpecs.containsSectionName(searchCriteria.get("sectionName")));
        }

        if (StringUtils.hasLength(searchCriteria.get("isActive"))) {
            String activeStr = searchCriteria.get("isActive").toLowerCase();
            if ("true".equals(activeStr) || "false".equals(activeStr)) {
                Boolean isActive = Boolean.valueOf(activeStr);
                spec = spec.and(SectionSpecs.hasActiveStatus(isActive));
            }
        }

        spec = spec.and(SectionSpecs.belongsToCourse(courseId)); // Only show sections that belong to the default course of the instructor

        return this.sectionRepository.findAll(spec, pageable);
    }

    public Section findSectionById(Integer sectionId) {
        return this.sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ObjectNotFoundException("section", sectionId));
    }

    /**
     * Find sections with students that are eligible for weekly reminders in a given week.
     * <p>
     * Eligible sections are active sections that have the given week in their activeWeeks list.
     * This is eager loading.
     *
     * @return
     */
    public List<Section> findReminderEligibleSectionsForWeek(String weekKey) {
        return this.sectionRepository.findReminderEligibleSectionsForWeek(weekKey);
    }

    public Section saveSection(Section newSection) {
        return this.sectionRepository.save(newSection);
    }

    public Section updateSection(Integer sectionId, Section update) {
        return this.sectionRepository.findById(sectionId)
                .map(oldSection -> {
                    oldSection.setSectionName(update.getSectionName());
                    oldSection.setStartDate(update.getStartDate());
                    oldSection.setEndDate(update.getEndDate());
                    oldSection.setActive(update.isActive());
                    oldSection.setWarWeeklyDueDay(update.getWarWeeklyDueDay());
                    oldSection.setWarDueTime(update.getWarDueTime());
                    oldSection.setPeerEvaluationWeeklyDueDay(update.getPeerEvaluationWeeklyDueDay());
                    oldSection.setPeerEvaluationDueTime(update.getPeerEvaluationDueTime());
                    return this.sectionRepository.save(oldSection);
                })
                .orElseThrow(() -> new ObjectNotFoundException("section", sectionId));
    }

    public void deleteSection(Integer sectionId) {
        this.sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ObjectNotFoundException("section", sectionId));
        this.sectionRepository.deleteById(sectionId);
    }

    public void setUpActiveWeeks(Integer sectionId, List<String> activeWeeks) {
        this.sectionRepository.findById(sectionId)
                .map(oldSection -> {
                    oldSection.setActiveWeeks(activeWeeks);
                    return this.sectionRepository.save(oldSection);
                })
                .orElseThrow(() -> new ObjectNotFoundException("section", sectionId));
    }

    public void assignRubric(Integer sectionId, Integer rubricId) {
        Rubric rubric = this.rubricRepository.findById(rubricId)
                .orElseThrow(() -> new ObjectNotFoundException("rubric", rubricId));
        this.sectionRepository.findById(sectionId)
                .map(oldSection -> {
                    oldSection.setRubric(rubric);
                    return this.sectionRepository.save(oldSection);
                })
                .orElseThrow(() -> new ObjectNotFoundException("section", sectionId));
    }

    public void assignInstructor(Integer sectionId, Integer instructorId) {
        Instructor instructor = this.instructorRepository.findById(instructorId)
                .orElseThrow(() -> new ObjectNotFoundException("instructor", instructorId));
        this.sectionRepository.findById(sectionId)
                .map(oldSection -> {
                    oldSection.addInstructor(instructor); // No need to check if the instructor is already in the section since it is a set
                    return this.sectionRepository.save(oldSection);
                })
                .orElseThrow(() -> new ObjectNotFoundException("section", sectionId));
    }

    public void removeInstructor(Integer sectionId, Integer instructorId) {
        Instructor instructor = this.instructorRepository.findById(instructorId)
                .orElseThrow(() -> new ObjectNotFoundException("instructor", instructorId));
        this.sectionRepository.findById(sectionId)
                .map(oldSection -> {
                    // Check if the instructor is in the section
                    if (oldSection.getInstructors().contains(instructor)) {
                        oldSection.removeInstructor(instructor);
                    }
                    return this.sectionRepository.save(oldSection);
                })
                .orElseThrow(() -> new ObjectNotFoundException("section", sectionId));
    }

}
