package team.projectpulse.security.authorizationmanagers;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import team.projectpulse.rubric.RubricSecurityService;

import java.util.function.Supplier;

/**
 * Guards changing and deleting one rubric, admitting only the owning course's admin.
 *
 * <p>Reads {@code rubricId} and delegates to {@link RubricSecurityService#isRubricOwner}.
 */
@Component
public class RubricOwnershipAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final RubricSecurityService rubricSecurityService;


    public RubricOwnershipAuthorizationManager(RubricSecurityService rubricSecurityService) {
        this.rubricSecurityService = rubricSecurityService;
    }

    @Override
    public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authentication, RequestAuthorizationContext context) {
        Integer rubricId = PathVariables.readId(context, "rubricId");
        if (rubricId == null) {
            return new AuthorizationDecision(false);
        }
        return new AuthorizationDecision(
                this.rubricSecurityService.isRubricOwner(rubricId));
    }

}
