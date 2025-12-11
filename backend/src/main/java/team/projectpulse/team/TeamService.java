package team.projectpulse.team;

import team.projectpulse.instructor.Instructor;
import team.projectpulse.instructor.InstructorRepository;
import team.projectpulse.section.Section;
import team.projectpulse.section.SectionRepository;
import team.projectpulse.student.Student;
import team.projectpulse.student.StudentRepository;
import team.projectpulse.system.UserUtils;
import team.projectpulse.system.exception.ObjectNotFoundException;
import team.projectpulse.team.dto.TransferTeamResponse;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;
    private final SectionRepository sectionRepository;
    private final UserUtils userUtils;


    public TeamService(TeamRepository teamRepository, StudentRepository studentRepository, InstructorRepository instructorRepository, SectionRepository sectionRepository, UserUtils userUtils) {
        this.teamRepository = teamRepository;
        this.studentRepository = studentRepository;
        this.instructorRepository = instructorRepository;
        this.sectionRepository = sectionRepository;
        this.userUtils = userUtils;
    }

    public Page<Team> findByCriteria(Map<String, String> searchCriteria, Pageable pageable) {
        Integer sectionId = this.userUtils.getUserSectionId();

        Specification<Team> spec = Specification.unrestricted(); // Start with an unrestricted specification, matching all objects.

        if (StringUtils.hasLength(searchCriteria.get("teamName"))) {
            spec = spec.and(TeamSpecs.containsTeamName(searchCriteria.get("teamName")));
        }

        spec = spec.and(TeamSpecs.belongsToSection(sectionId.toString()));

        return this.teamRepository.findAll(spec, pageable);
    }

    public Team findTeamById(Integer teamId) {
        return this.teamRepository.findById(teamId)
                .orElseThrow(() -> new ObjectNotFoundException("team", teamId));
    }

    public Team saveTeam(Team newTeam) {
        return this.teamRepository.save(newTeam);
    }

    public Team updateTeam(Integer teamId, Team update) {
        return this.teamRepository.findById(teamId)
                .map(oldTeam -> {
                    oldTeam.setTeamName(update.getTeamName());
                    oldTeam.setDescription(update.getDescription());
                    oldTeam.setTeamWebsiteUrl(update.getTeamWebsiteUrl());
                    return this.teamRepository.save(oldTeam);
                })
                .orElseThrow(() -> new ObjectNotFoundException("team", teamId));
    }

    public void deleteTeam(Integer teamId) {
        this.teamRepository.findById(teamId)
                .orElseThrow(() -> new ObjectNotFoundException("team", teamId));
        this.teamRepository.deleteById(teamId);
    }

    public void assignStudentToTeam(Integer teamId, Integer studentId) {
        Team team = this.teamRepository.findById(teamId)
                .orElseThrow(() -> new ObjectNotFoundException("team", teamId));

        Student student = this.studentRepository.findById(studentId)
                .orElseThrow(() -> new ObjectNotFoundException("student", studentId));

        // Check if the student is already on the team
        if (!team.getStudents().contains(student)) {
            // Remove the student from the team if the student is already assigned to another team
            if (student.getTeam() != null) {
                student.getTeam().removeStudent(student);
            }
            team.addStudent(student);
        }
    }

    public void removeStudentFromTeam(Integer teamId, Integer studentId) {
        Team team = this.teamRepository.findById(teamId)
                .orElseThrow(() -> new ObjectNotFoundException("team", teamId));

        Student student = this.studentRepository.findById(studentId)
                .orElseThrow(() -> new ObjectNotFoundException("student", studentId));

        // Check if the student is on the team
        if (team.getStudents().contains(student)) {
            team.removeStudent(student);
        }
    }

    public void assignInstructorToTeam(Integer teamId, Integer instructorId) {
        Team team = this.teamRepository.findById(teamId)
                .orElseThrow(() -> new ObjectNotFoundException("team", teamId));

        Instructor instructor = this.instructorRepository.findById(instructorId)
                .orElseThrow(() -> new ObjectNotFoundException("instructor", instructorId));

        // Check if the instructor is already on the team
        if (!instructor.equals(team.getInstructor())) {
            // Remove the instructor from the team if the team is already assigned to another instructor
            if (team.getInstructor() != null) {
                team.removeInstructor(team.getInstructor());
            }
            team.addInstructor(instructor);
        }
    }

    public TransferTeamResponse transferTeam(Integer teamId, Integer newSectionId) {
        // Find the team and new section
        Team team = this.teamRepository.findById(teamId)
                .orElseThrow(() -> new ObjectNotFoundException("team", teamId));

        Section oldSection = team.getSection();
        Section newSection = this.sectionRepository.findById(newSectionId)
                .orElseThrow(() -> new ObjectNotFoundException("section", newSectionId));

        // Validate that both sections belong to the same course
        if (!oldSection.getCourse().getCourseId().equals(newSection.getCourse().getCourseId())) {
            throw new IllegalArgumentException("Cannot transfer team to a section in a different course");
        }

        List<String> warnings = new ArrayList<>();
        
        // Update team's section
        team.setSection(newSection);

        // Get all students in the team and update their sections
        List<Student> teamStudents = this.studentRepository.findByTeamTeamId(teamId);
        for (Student student : teamStudents) {
            student.setSection(newSection);
        }
        // Save updated students
        this.studentRepository.saveAll(teamStudents);

        // Handle instructor reassignment
        if (team.getInstructor() != null) {
            Instructor currentInstructor = team.getInstructor();
            
            // Check if the current instructor is in the new section
            if (!newSection.getInstructors().contains(currentInstructor)) {
                // Remove the current instructor
                team.removeInstructor(currentInstructor);
                
                // Assign an instructor from the new section if available
                if (!newSection.getInstructors().isEmpty()) {
                    Instructor newInstructor = newSection.getInstructors().iterator().next();
                    team.addInstructor(newInstructor);
                    warnings.add("Team instructor changed from " + currentInstructor.getFirstName() + " " + 
                                currentInstructor.getLastName() + " to " + newInstructor.getFirstName() + " " + 
                                newInstructor.getLastName());
                } else {
                    warnings.add("No instructor available in the target section. Team has no assigned instructor.");
                }
            }
        } else if (!newSection.getInstructors().isEmpty()) {
            // If there's no instructor but the new section has instructors, assign one
            Instructor newInstructor = newSection.getInstructors().iterator().next();
            team.addInstructor(newInstructor);
            warnings.add("Team assigned to instructor: " + newInstructor.getFirstName() + " " + 
                        newInstructor.getLastName());
        }

        // Save the team with updated section
        this.teamRepository.save(team);

        return new TransferTeamResponse(
                team.getTeamId(),
                team.getTeamName(),
                oldSection.getSectionId(),
                oldSection.getSectionName(),
                newSection.getSectionId(),
                newSection.getSectionName(),
                teamStudents.size(),
                warnings
        );
    }

}
