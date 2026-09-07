package team.projectpulse.security.authorizationmanagers;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import team.projectpulse.instructor.InstructorSecurityService;
import team.projectpulse.student.StudentSecurityService;

import java.util.function.Supplier;

/**
 * Guards the routes that name a single user in the path: the user themselves may pass, and so may the
 * instructor or course admin above them.
 *
 * <p>The routes spell that user three ways, so the manager dispatches on which variable the matched route
 * bound: {@code studentId} and {@code evaluatorId} both name a student (an evaluator is the student writing
 * the peer evaluation), {@code instructorId} names an instructor. A route bound to this manager that binds
 * none of them is denied, since the manager cannot tell whose data is being asked for.
 */
@Component
public class UserOwnershipAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final InstructorSecurityService instructorSecurityService;
    private final StudentSecurityService studentSecurityService;


    public UserOwnershipAuthorizationManager(InstructorSecurityService instructorSecurityService, StudentSecurityService studentSecurityService) {
        this.instructorSecurityService = instructorSecurityService;
        this.studentSecurityService = studentSecurityService;
    }

    @Override
    public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authentication, RequestAuthorizationContext context) {
        Integer studentId = PathVariables.readId(context, "studentId");
        if (studentId == null) {
            studentId = PathVariables.readId(context, "evaluatorId");
        }
        if (studentId != null) {
            return new AuthorizationDecision(
                    this.studentSecurityService.isCurrentUserInstructorOfStudentSection(studentId)
                            || this.studentSecurityService.isStudentSelf(studentId));
        }

        Integer instructorId = PathVariables.readId(context, "instructorId");
        if (instructorId != null) {
            return new AuthorizationDecision(
                    this.instructorSecurityService.isCurrentUserAdminOfInstructorCourse(instructorId)
                            || this.instructorSecurityService.isInstructorSelf(instructorId));
        }

        return new AuthorizationDecision(false);
    }

}
