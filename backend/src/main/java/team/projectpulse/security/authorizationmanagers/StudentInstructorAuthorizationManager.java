package team.projectpulse.security.authorizationmanagers;

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
public class StudentInstructorAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private static final UriTemplate STUDENT_URI_TEMPLATE = new UriTemplate("/students/{studentId}");
    private final StudentSecurityService studentSecurityService;

    public StudentInstructorAuthorizationManager(StudentSecurityService studentSecurityService) {
        this.studentSecurityService = studentSecurityService;
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext context) {
        // Extract the studentId from the request URI: /students/{studentId}
        Map<String, String> uriVariables = STUDENT_URI_TEMPLATE.match(context.getRequest().getRequestURI());
        Integer studentIdFromRequestUri = Integer.parseInt(uriVariables.get("studentId"));
        return new AuthorizationDecision(this.studentSecurityService.isCurrentUserInstructorOfStudentSection(studentIdFromRequestUri));
    }

}
