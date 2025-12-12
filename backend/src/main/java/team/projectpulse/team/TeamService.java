package team.projectpulse.team;

import team.projectpulse.instructor.Instructor;
import team.projectpulse.instructor.InstructorRepository;
import team.projectpulse.section.Section;
import team.projectpulse.section.SectionRepository;
import team.projectpulse.student.Student;
import team.projectpulse.student.StudentRepository;
import team.projectpulse.system.UserUtils;
import team.projectpulse.system.exception.ObjectNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import team.projectpulse.team.dto.TransferTeamResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;
    private final UserUtils userUtils;
    private final SectionRepository sectionRepository;


    public TeamService(TeamRepository teamRepository, StudentRepository studentRepository, InstructorRepository instructorRepository, UserUtils userUtils, SectionRepository sectionRepository) {
        this.teamRepository = teamRepository;
        this.studentRepository = studentRepository;
        this.instructorRepository = instructorRepository;
        this.userUtils = userUtils;
        this.sectionRepository = sectionRepository;
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

        // AssignStudentToTeamAuthorizationManager would have already checked if the student and team are in the same section

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

    public TransferTeamResponse transferTeamToAnotherSection(Integer teamId, Integer newSectionId) {
        // Find the team
        Team team = this.teamRepository.findById(teamId)
                .orElseThrow(() -> new ObjectNotFoundException("team", teamId));

        // Find the current section
        Section oldSection = team.getSection();

        // Find the new section
        Section newSection = this.sectionRepository.findById(newSectionId)
                .orElseThrow(() -> new ObjectNotFoundException("section", newSectionId));

        // Validate that both sections belong to the same course
        if (!oldSection.getCourse().getCourseId().equals(newSection.getCourse().getCourseId())) {
            throw new IllegalArgumentException("Cannot transfer team to a section in a different course");
        }

        // Transfer the team to the new section
        this.transferTeam(team, oldSection, newSection);

        // Transfer all students in this team to the new section as well
        List<Student> teamStudents = new ArrayList<>(team.getStudents());
        for (Student student : teamStudents) {
            this.transferStudent(student, oldSection, newSection);
        }

        // Assign this team a new instructor from the new section if the current instructor is not teaching the new section
        Instructor oldInstructor = team.getInstructor();
        if (!newSection.getInstructors().contains(oldInstructor)) {
            team.removeInstructor(oldInstructor);
            team.addInstructor(newSection.getInstructors().iterator().next()); // Assign the first instructor of the new section
        }

        return new TransferTeamResponse(
                team.getTeamId(),
                team.getTeamName(),
                oldSection.getSectionId(),
                oldSection.getSectionName(),
                newSection.getSectionId(),
                newSection.getSectionName(),
                teamStudents.size(),
                oldInstructor != null ? oldInstructor.getFirstName() + " " + oldInstructor.getLastName() : "No Instructor",
                team.getInstructor().getFirstName() + " " + team.getInstructor().getLastName()
        );
    }

    public void transferTeam(Team team, Section from, Section to) {
        if (team.getSection() != from) {
            throw new IllegalStateException("Team is not in the expected old section.");
        }

        // inverse side maintenance
        from.getTeams().remove(team);
        to.getTeams().add(team);

        // owning side update (the real FK change)
        team.setSection(to);
    }

    public void transferStudent(Student student, Section from, Section to) {
        if (student.getSection() != from) {
            throw new IllegalStateException("Student is not in the expected old section.");
        }

        // inverse side maintenance
        from.getStudents().remove(student);
        to.getStudents().add(student);

        // owning side update (the real FK change)
        student.setSection(to);
    }

}
