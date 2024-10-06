package team.projectpulse.security.authorizationmanagers;

import team.projectpulse.evaluation.EvaluationSecurityService;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriTemplate;

import java.util.Map;
import java.util.function.Supplier;

@Component
public class EvaluationOwnershipAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private static final UriTemplate EVALUATION_URI_TEMPLATE = new UriTemplate("/evaluations/{evaluationId}");
    private final EvaluationSecurityService evaluationSecurityService;


    public EvaluationOwnershipAuthorizationManager(EvaluationSecurityService evaluationSecurityService) {
        this.evaluationSecurityService = evaluationSecurityService;
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext context) {
        // Extract the evaluationId from the request URI: /evaluations/{evaluationId}
        Map<String, String> uriVariables = EVALUATION_URI_TEMPLATE.match(context.getRequest().getRequestURI());
        Integer evaluationIdFromRequestUri = Integer.parseInt(uriVariables.get("evaluationId"));
        return new AuthorizationDecision(this.evaluationSecurityService.isEvaluationOwner(evaluationIdFromRequestUri));
    }

}
