package team.projectpulse.security.authorizationmanagers;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import team.projectpulse.student.StudentSecurityService;

import java.util.function.Supplier;

/**
 * Guards deleting a student, admitting only the course admin of the course that owns the student's course section
 * (BR-student-lifecycle). The student herself and an instructor merely assigned to her course section are denied,
 * unlike {@link UserOwnershipAuthorizationManager}, which guards viewing and editing her.
 *
 * <p>Reads {@code studentId} and delegates to {@link StudentSecurityService#isCurrentUserCourseAdminOfStudent}.
 */
@Component
public class StudentDeletionAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final StudentSecurityService studentSecurityService;


    public StudentDeletionAuthorizationManager(StudentSecurityService studentSecurityService) {
        this.studentSecurityService = studentSecurityService;
    }

    @Override
    public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authentication, RequestAuthorizationContext context) {
        Integer studentId = PathVariables.readId(context, "studentId");
        if (studentId == null) {
            return new AuthorizationDecision(false);
        }
        return new AuthorizationDecision(this.studentSecurityService.isCurrentUserCourseAdminOfStudent(studentId));
    }

}
