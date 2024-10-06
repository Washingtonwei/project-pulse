package team.projectpulse.security.authorizationmanagers;

import team.projectpulse.rubric.RubricSecurityService;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriTemplate;

import java.util.Map;
import java.util.function.Supplier;

@Component
public class RubricOwnershipAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private static final UriTemplate RUBRIC_URI_TEMPLATE = new UriTemplate("/rubrics/{rubricId}");
    private final RubricSecurityService rubricSecurityService;


    public RubricOwnershipAuthorizationManager(RubricSecurityService rubricSecurityService) {
        this.rubricSecurityService = rubricSecurityService;
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext context) {
        // Extract the rubricId from the request URI: /rubrics/{rubricId}
        Map<String, String> uriVariables = RUBRIC_URI_TEMPLATE.match(context.getRequest().getRequestURI());
        Integer rubricIdFromRequestUri = Integer.parseInt(uriVariables.get("rubricId"));
        return new AuthorizationDecision(this.rubricSecurityService.isRubricOwner(rubricIdFromRequestUri));
    }

}
