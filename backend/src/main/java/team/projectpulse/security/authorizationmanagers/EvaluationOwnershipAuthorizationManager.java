package team.projectpulse.security.authorizationmanagers;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import team.projectpulse.evaluation.EvaluationSecurityService;

import java.util.function.Supplier;

/**
 * Guards editing one peer evaluation, admitting only the student who wrote it.
 *
 * <p>Reads {@code evaluationId} and delegates to {@link EvaluationSecurityService#isEvaluationOwner}.
 */
@Component
public class EvaluationOwnershipAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final EvaluationSecurityService evaluationSecurityService;


    public EvaluationOwnershipAuthorizationManager(EvaluationSecurityService evaluationSecurityService) {
        this.evaluationSecurityService = evaluationSecurityService;
    }

    @Override
    public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authentication, RequestAuthorizationContext context) {
        Integer evaluationId = PathVariables.readId(context, "evaluationId");
        if (evaluationId == null) {
            return new AuthorizationDecision(false);
        }
        return new AuthorizationDecision(
                this.evaluationSecurityService.isEvaluationOwner(evaluationId));
    }

}
