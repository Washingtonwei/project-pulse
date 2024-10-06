package team.projectpulse.security.authorizationmanagers;

import team.projectpulse.course.CourseSecurityService;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriTemplate;

import java.util.Map;
import java.util.function.Supplier;

@Component
public class CourseMembershipAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private static final UriTemplate COURSE_URI_TEMPLATE = new UriTemplate("/courses/{courseId}");
    private final CourseSecurityService courseSecurityService;

    public CourseMembershipAuthorizationManager(CourseSecurityService courseSecurityService) {
        this.courseSecurityService = courseSecurityService;
    }
    
    @Override
    public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext context) {
        // Extract the courseId from the request URI: /courses/{courseId}
        Map<String, String> uriVariables = COURSE_URI_TEMPLATE.match(context.getRequest().getRequestURI());
        Integer courseIdFromRequestUri = Integer.parseInt(uriVariables.get("courseId"));
        return new AuthorizationDecision(this.courseSecurityService.isCourseInstructor(courseIdFromRequestUri));
    }

}
