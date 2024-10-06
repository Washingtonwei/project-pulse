package team.projectpulse.security.authorizationmanagers;

import team.projectpulse.team.TeamSecurityService;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriTemplate;

import java.util.Map;
import java.util.function.Supplier;

@Component
public class TeamOwnershipAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private static final UriTemplate TEAM_URI_TEMPLATE = new UriTemplate("/teams/{teamId}");
    private final TeamSecurityService teamSecurityService;


    public TeamOwnershipAuthorizationManager(TeamSecurityService teamSecurityService) {
        this.teamSecurityService = teamSecurityService;
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext context) {
        // Extract the teamId from the request URI: /teams/{teamId}
        Map<String, String> uriVariables = TEAM_URI_TEMPLATE.match(context.getRequest().getRequestURI());
        Integer teamIdFromRequestUri = Integer.parseInt(uriVariables.get("teamId"));
        return new AuthorizationDecision(this.teamSecurityService.isTeamOwner(teamIdFromRequestUri));
    }

}
