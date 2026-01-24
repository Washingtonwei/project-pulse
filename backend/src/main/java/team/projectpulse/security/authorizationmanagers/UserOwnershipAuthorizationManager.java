package team.projectpulse.security.authorizationmanagers;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationResult;
import team.projectpulse.instructor.InstructorSecurityService;
import team.projectpulse.student.StudentSecurityService;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriTemplate;

import java.util.Map;
import java.util.function.Supplier;

@Component
public class UserOwnershipAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private static final UriTemplate STUDENT_USER_URI_TEMPLATE = new UriTemplate("/students/{studentId}");
    private static final UriTemplate INSTRUCTOR_USER_URI_TEMPLATE = new UriTemplate("/instructors/{instructorId}");
    private static final UriTemplate EVALUATOR_USER_URI_TEMPLATE = new UriTemplate("/evaluations/evaluators/{evaluatorId}");

    private final InstructorSecurityService instructorSecurityService;
    private final StudentSecurityService studentSecurityService;

    public UserOwnershipAuthorizationManager(InstructorSecurityService instructorSecurityService, StudentSecurityService studentSecurityService) {
        this.instructorSecurityService = instructorSecurityService;
        this.studentSecurityService = studentSecurityService;
    }


    @Override
    public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authentication, RequestAuthorizationContext context) {
        if (!STUDENT_USER_URI_TEMPLATE.match(context.getRequest().getRequestURI()).isEmpty()) {
            // Extract the studentId from the request URI: /students/{studentId}
            Map<String, String> uriVariables = STUDENT_USER_URI_TEMPLATE.match(context.getRequest().getRequestURI());
            Integer studentIdFromRequestUri = Integer.parseInt(uriVariables.get("studentId"));
            return new AuthorizationDecision(
                    this.studentSecurityService.isCurrentUserInstructorOfStudentSection(studentIdFromRequestUri) ||
                            this.studentSecurityService.isStudentSelf(studentIdFromRequestUri)
            );
        } else if (!INSTRUCTOR_USER_URI_TEMPLATE.match(context.getRequest().getRequestURI()).isEmpty()) {
            // Extract the instructorId from the request URI: /instructors/{instructorId}
            Map<String, String> uriVariables = INSTRUCTOR_USER_URI_TEMPLATE.match(context.getRequest().getRequestURI());
            Integer instructorIdFromRequestUri = Integer.parseInt(uriVariables.get("instructorId"));
            return new AuthorizationDecision(
                    this.instructorSecurityService.isCurrentUserAdminOfInstructorCourse(instructorIdFromRequestUri) ||
                            this.instructorSecurityService.isInstructorSelf(instructorIdFromRequestUri)
            );
        } else if (!EVALUATOR_USER_URI_TEMPLATE.match(context.getRequest().getRequestURI()).isEmpty()) {
            // Extract the evaluatorId from the request URI: /evaluations/evaluators/{evaluatorId}
            Map<String, String> uriVariables = EVALUATOR_USER_URI_TEMPLATE.match(context.getRequest().getRequestURI());
            Integer evaluatorIdFromRequestUri = Integer.parseInt(uriVariables.get("evaluatorId"));
            return new AuthorizationDecision(
                    this.studentSecurityService.isCurrentUserInstructorOfStudentSection(evaluatorIdFromRequestUri) ||
                            this.studentSecurityService.isStudentSelf(evaluatorIdFromRequestUri)
            );
        } else {
            return new AuthorizationDecision(false);
        }
    }

}
