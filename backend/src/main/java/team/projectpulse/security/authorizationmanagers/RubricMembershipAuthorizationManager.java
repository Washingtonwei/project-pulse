package team.projectpulse.security.authorizationmanagers;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationResult;
import team.projectpulse.rubric.RubricSecurityService;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriTemplate;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Both students and instructors can access the rubric resource.
 */
@Component
public class RubricMembershipAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private static final UriTemplate RUBRIC_URI_TEMPLATE = new UriTemplate("/rubrics/{rubricId}");
    private final RubricSecurityService rubricSecurityService;


    public RubricMembershipAuthorizationManager(RubricSecurityService rubricSecurityService) {
        this.rubricSecurityService = rubricSecurityService;
    }

    @Override
    public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authentication, RequestAuthorizationContext context) {
        // Extract the rubricId from the request URI: /rubrics/{rubricId}
        Map<String, String> uriVariables = RUBRIC_URI_TEMPLATE.match(context.getRequest().getRequestURI());
        Integer rubricIdFromRequestUri = Integer.parseInt(uriVariables.get("rubricId"));
        return new AuthorizationDecision(this.rubricSecurityService.canAccessRubric(rubricIdFromRequestUri));
    }

}
