package team.projectpulse.security.authorizationmanagers;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import team.projectpulse.rubric.CriterionSecurityService;

import java.util.function.Supplier;

/**
 * Guards changing and deleting one rubric criterion, admitting only the owning course's admin.
 *
 * <p>Reads {@code criterionId} and delegates to {@link CriterionSecurityService#isCriterionOwner}.
 */
@Component
public class CriterionOwnershipAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final CriterionSecurityService criterionSecurityService;


    public CriterionOwnershipAuthorizationManager(CriterionSecurityService criterionSecurityService) {
        this.criterionSecurityService = criterionSecurityService;
    }

    @Override
    public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authentication, RequestAuthorizationContext context) {
        Integer criterionId = PathVariables.readId(context, "criterionId");
        if (criterionId == null) {
            return new AuthorizationDecision(false);
        }
        return new AuthorizationDecision(
                this.criterionSecurityService.isCriterionOwner(criterionId));
    }

}
