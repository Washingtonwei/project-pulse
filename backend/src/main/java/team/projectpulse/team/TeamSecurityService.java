package team.projectpulse.team;

import team.projectpulse.instructor.Instructor;
import team.projectpulse.instructor.InstructorRepository;
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
public class TeamSecurityService {

    private final TeamRepository teamRepository;
    private final UserUtils userUtils;
    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;


    public TeamSecurityService(TeamRepository teamRepository, UserUtils userUtils, StudentRepository studentRepository, InstructorRepository instructorRepository) {
        this.teamRepository = teamRepository;
        this.userUtils = userUtils;
        this.studentRepository = studentRepository;
        this.instructorRepository = instructorRepository;
    }

    public boolean isTeamOwner(Integer teamId) {
        Team teamToBeAccessed = this.teamRepository.findById(teamId)
                .orElseThrow(() -> new ObjectNotFoundException("team", teamId));
        Integer adminId = teamToBeAccessed.getSection().getCourse().getCourseAdmin().getId();
        Integer userIdFromJwt = this.userUtils.getUserId();
        return adminId.equals(userIdFromJwt);
    }

    public boolean canAccessTeam(Integer teamId) {
        Team teamToBeAccessed = this.teamRepository.findById(teamId)
                .orElseThrow(() -> new ObjectNotFoundException("team", teamId));
        Integer userIdFromJwt = this.userUtils.getUserId();
        boolean hasStudentRole = this.userUtils.hasRole("ROLE_student");
        boolean hasInstructorRole = this.userUtils.hasRole("ROLE_instructor");
        if (hasStudentRole) {
            Student student = this.studentRepository.findById(userIdFromJwt)
                    .orElseThrow(() -> new ObjectNotFoundException("student", userIdFromJwt));
            // Check if the student's teamId matches the teamId from the request URI
            return teamId.equals(student.getTeam().getTeamId());
        } else if (hasInstructorRole) {
            // Get the list of instructorIds from the team's section
            List<Integer> instructorIds = teamToBeAccessed.getSection().getInstructors().stream()
                    .map(Instructor::getId)
                    .collect(Collectors.toList());
            return instructorIds.contains(userIdFromJwt);
        } else {
            return false;
        }
    }

    // Check if the instructor is in the same section as the team
    public boolean isTeamAndInstructorInSameSection(Integer teamId, Integer instructorId) {
        Team team = this.teamRepository.findById(teamId)
                .orElseThrow(() -> new ObjectNotFoundException("team", teamId));
        Instructor instructor = this.instructorRepository.findById(instructorId)
                .orElseThrow(() -> new ObjectNotFoundException("instructor", instructorId));
        return instructor.getSections().contains(team.getSection());
    }

    // Check if the student is in the same section as the team
    public boolean isTeamAndStudentInSameSection(Integer teamId, Integer studentId) {
        Team team = this.teamRepository.findById(teamId)
                .orElseThrow(() -> new ObjectNotFoundException("team", teamId));
        Student student = this.studentRepository.findById(studentId)
                .orElseThrow(() -> new ObjectNotFoundException("student", studentId));
        return team.getSection().getSectionId().equals(student.getSection().getSectionId());
    }

}
