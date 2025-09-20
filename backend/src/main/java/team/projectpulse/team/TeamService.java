package team.projectpulse.team;

import team.projectpulse.instructor.Instructor;
import team.projectpulse.instructor.InstructorRepository;
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

import java.util.Map;

@Service
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;
    private final UserUtils userUtils;


    public TeamService(TeamRepository teamRepository, StudentRepository studentRepository, InstructorRepository instructorRepository, UserUtils userUtils) {
        this.teamRepository = teamRepository;
        this.studentRepository = studentRepository;
        this.instructorRepository = instructorRepository;
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

}
