package team.projectpulse.security.authorizationmanagers;

import team.projectpulse.rubric.CriterionSecurityService;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriTemplate;

import java.util.Map;
import java.util.function.Supplier;

@Component
public class CriterionMembershipAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private static final UriTemplate CRITERION_URI_TEMPLATE = new UriTemplate("/criteria/{criterionId}");
    private final CriterionSecurityService criterionSecurityService;


    public CriterionMembershipAuthorizationManager(CriterionSecurityService criterionSecurityService) {
        this.criterionSecurityService = criterionSecurityService;
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext context) {
        // Extract the criterionId from the request URI: /criteria/{criterionId}
        Map<String, String> uriVariables = CRITERION_URI_TEMPLATE.match(context.getRequest().getRequestURI());
        Integer criterionIdFromRequestUri = Integer.parseInt(uriVariables.get("criterionId"));
        return new AuthorizationDecision(this.criterionSecurityService.canAccessCriterion(criterionIdFromRequestUri));
    }

}
