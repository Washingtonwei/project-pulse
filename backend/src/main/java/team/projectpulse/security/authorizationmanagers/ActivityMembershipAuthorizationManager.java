package team.projectpulse.security.authorizationmanagers;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationResult;
import team.projectpulse.activity.ActivitySecurityService;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriTemplate;

import java.util.Map;
import java.util.function.Supplier;

@Component
public class ActivityMembershipAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private static final UriTemplate ACTIVITY_URI_TEMPLATE = new UriTemplate("/activities/{activityId}");
    private final ActivitySecurityService activitySecurityService;


    public ActivityMembershipAuthorizationManager(ActivitySecurityService activitySecurityService) {
        this.activitySecurityService = activitySecurityService;
    }

    @Override
    public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authentication, RequestAuthorizationContext context) {
        // Extract the activityId from the request URI: /activities/{activityId}
        Map<String, String> uriVariables = ACTIVITY_URI_TEMPLATE.match(context.getRequest().getRequestURI());
        Integer activityIdFromRequestUri = Integer.parseInt(uriVariables.get("activityId"));
        return new AuthorizationDecision(this.activitySecurityService.canAccessActivity(activityIdFromRequestUri));
    }

}
