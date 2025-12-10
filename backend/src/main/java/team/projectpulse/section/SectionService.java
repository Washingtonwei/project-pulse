package team.projectpulse.section;

import team.projectpulse.instructor.Instructor;
import team.projectpulse.instructor.InstructorRepository;
import team.projectpulse.rubric.Rubric;
import team.projectpulse.rubric.RubricRepository;
import team.projectpulse.system.UserUtils;
import team.projectpulse.system.exception.ObjectNotFoundException;
import team.projectpulse.user.PeerEvaluationUser;
import team.projectpulse.user.UserRepository;
import team.projectpulse.user.userinvitation.UserInvitationService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SectionService {

    private final SectionRepository sectionRepository;
    private final RubricRepository rubricRepository;
    private final InstructorRepository instructorRepository;
    private final UserUtils userUtils;
    private final UserRepository userRepository;
    private final UserInvitationService userInvitationService;


    public SectionService(SectionRepository sectionRepository, RubricRepository rubricRepository, InstructorRepository instructorRepository, UserUtils userUtils, UserRepository userRepository, UserInvitationService userInvitationService) {
        this.sectionRepository = sectionRepository;
        this.rubricRepository = rubricRepository;
        this.instructorRepository = instructorRepository;
        this.userUtils = userUtils;
        this.userRepository = userRepository;
        this.userInvitationService = userInvitationService;
    }

    public Page<Section> findByCriteria(Map<String, String> searchCriteria, Pageable pageable) {
        Integer courseId = this.userUtils.getUserCourseId();

        Specification<Section> spec = Specification.unrestricted(); // Start with an unrestricted specification, matching all objects.

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

    public List<Map<String, Object>> getInstructors(Integer sectionId) {
        Section section = this.sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ObjectNotFoundException("section", sectionId));
        
        List<Map<String, Object>> instructorsList = new ArrayList<>();
        for (Instructor instructor : section.getInstructors()) {
            Map<String, Object> instructorData = new HashMap<>();
            instructorData.put("id", instructor.getId());
            instructorData.put("firstName", instructor.getFirstName());
            instructorData.put("lastName", instructor.getLastName());
            instructorData.put("email", instructor.getEmail());
            instructorsList.add(instructorData);
        }
        
        return instructorsList;
    }

    public void removeInstructor(Integer sectionId, Integer instructorId) {
        Instructor instructor = this.instructorRepository.findById(instructorId)
                .orElseThrow(() -> new ObjectNotFoundException("instructor", instructorId));
        this.sectionRepository.findById(sectionId)
                .map(oldSection -> {
                    // Check if the instructor is in the section
                    if (oldSection.getInstructors().contains(instructor)) {
                        // Prevent removal if this is the last instructor
                        if (oldSection.getInstructors().size() <= 1) {
                            throw new IllegalStateException("Cannot remove the last instructor from the section");
                        }
                        oldSection.removeInstructor(instructor);
                    }
                    return this.sectionRepository.save(oldSection);
                })
                .orElseThrow(() -> new ObjectNotFoundException("section", sectionId));
    }

    public Map<String, Object> inviteOrAddInstructors(Integer courseId, Integer sectionId, List<String> emails) {
        Section section = this.sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ObjectNotFoundException("section", sectionId));

        List<String> addedEmails = new ArrayList<>();
        List<String> invitedEmails = new ArrayList<>();
        List<String> alreadyExistsEmails = new ArrayList<>();

        for (String email : emails) {
            // Check if user with this email already exists
            PeerEvaluationUser existingUser = this.userRepository.findByEmail(email).orElse(null);
            
            if (existingUser != null) {
                // User exists - check if they're an instructor
                if (existingUser instanceof Instructor) {
                    Instructor instructor = (Instructor) existingUser;
                    // Check if already assigned to this section
                    if (section.getInstructors().contains(instructor)) {
                        alreadyExistsEmails.add(email);
                    } else {
                        // Add instructor to section
                        section.addInstructor(instructor);
                        addedEmails.add(email);
                    }
                } else {
                    // User exists but is not an instructor - cannot add
                    throw new IllegalArgumentException("User with email " + email + " exists but is not an instructor");
                }
            } else {
                // User doesn't exist - send invitation
                this.userInvitationService.sendEmailInvitations(courseId, sectionId, List.of(email), "instructor");
                invitedEmails.add(email);
            }
        }

        this.sectionRepository.save(section);

        Map<String, Object> result = new HashMap<>();
        result.put("added", addedEmails);
        result.put("invited", invitedEmails);
        result.put("alreadyExists", alreadyExistsEmails);
        return result;
    }

}
