package team.projectpulse.security.authorizationmanagers;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import team.projectpulse.activity.ActivitySecurityService;

import java.util.function.Supplier;

/**
 * Guards reading and commenting on one weekly activity report, admitting a student or instructor in the course section
 * that report belongs to.
 *
 * <p>Reads {@code activityId} and delegates to {@link ActivitySecurityService#canAccessActivity}.
 */
@Component
public class ActivityMembershipAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final ActivitySecurityService activitySecurityService;


    public ActivityMembershipAuthorizationManager(ActivitySecurityService activitySecurityService) {
        this.activitySecurityService = activitySecurityService;
    }

    @Override
    public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authentication, RequestAuthorizationContext context) {
        Integer activityId = PathVariables.readId(context, "activityId");
        if (activityId == null) {
            return new AuthorizationDecision(false);
        }
        return new AuthorizationDecision(
                this.activitySecurityService.canAccessActivity(activityId));
    }

}
