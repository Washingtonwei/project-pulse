package team.projectpulse.team;

import team.projectpulse.instructor.Instructor;
import team.projectpulse.instructor.InstructorRepository;
import team.projectpulse.student.Student;
import team.projectpulse.student.StudentRepository;
import team.projectpulse.system.UserUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Answers the team questions the route rules ask: is the caller this team's course admin ({@code isTeamOwner}); is
 * the caller on this team, meaning a student assigned to it or an instructor of its course section
 * ({@code canAccessTeam}); and, before an assignment route runs, is the named instructor or student already in the
 * team's course section.
 *
 * <p>Every check fails closed: an id that names no row, or a link not yet established, is denied rather than
 * throwing. These methods run inside an {@code AuthorizationManager}, upstream of the {@code DispatcherServlet},
 * so an exception thrown here escapes the filter chain as a 500 that also tells the caller which ids exist.
 */
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
        Team teamToBeAccessed = this.teamRepository.findById(teamId).orElse(null);
        if (teamToBeAccessed == null) {
            return false;
        }
        Integer adminId = teamToBeAccessed.getSection().getCourse().getCourseAdmin().getId();
        Integer userIdFromJwt = this.userUtils.getUserId();
        return adminId.equals(userIdFromJwt);
    }

    public boolean canAccessTeam(Integer teamId) {
        Team teamToBeAccessed = this.teamRepository.findById(teamId).orElse(null);
        if (teamToBeAccessed == null) {
            return false;
        }
        Integer userIdFromJwt = this.userUtils.getUserId();
        boolean hasStudentRole = this.userUtils.hasRole("ROLE_student");
        boolean hasInstructorRole = this.userUtils.hasRole("ROLE_instructor");
        if (hasStudentRole) {
            Student student = this.studentRepository.findById(userIdFromJwt).orElse(null);
            // A student who has not been assigned to a team yet is a member of none
            if (student == null || student.getTeam() == null) {
                return false;
            }
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
        Team team = this.teamRepository.findById(teamId).orElse(null);
        Instructor instructor = this.instructorRepository.findById(instructorId).orElse(null);
        if (team == null || instructor == null) {
            return false;
        }
        return instructor.getSections().contains(team.getSection());
    }

    // Check if the student is in the same section as the team
    public boolean isTeamAndStudentInSameSection(Integer teamId, Integer studentId) {
        Team team = this.teamRepository.findById(teamId).orElse(null);
        Student student = this.studentRepository.findById(studentId).orElse(null);
        if (team == null || student == null) {
            return false;
        }
        return team.getSection().getSectionId().equals(student.getSection().getSectionId());
    }

}
