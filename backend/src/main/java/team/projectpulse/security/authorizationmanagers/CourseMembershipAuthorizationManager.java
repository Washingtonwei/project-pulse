package team.projectpulse.security.authorizationmanagers;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import team.projectpulse.course.CourseSecurityService;

import java.util.function.Supplier;

/**
 * Guards reading one course, admitting an instructor who teaches it.
 *
 * <p>Reads {@code courseId} and delegates to {@link CourseSecurityService#isCourseInstructor}.
 */
@Component
public class CourseMembershipAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final CourseSecurityService courseSecurityService;


    public CourseMembershipAuthorizationManager(CourseSecurityService courseSecurityService) {
        this.courseSecurityService = courseSecurityService;
    }

    @Override
    public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authentication, RequestAuthorizationContext context) {
        Integer courseId = PathVariables.readId(context, "courseId");
        if (courseId == null) {
            return new AuthorizationDecision(false);
        }
        return new AuthorizationDecision(
                this.courseSecurityService.isCourseInstructor(courseId));
    }

}
