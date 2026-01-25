package team.projectpulse.security.authorizationmanagers;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriTemplate;
import team.projectpulse.team.TeamSecurityService;

import java.util.Map;
import java.util.function.Supplier;

@Component
public class TransferTeamAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    private static final UriTemplate TEAM_URI_TEMPLATE = new UriTemplate("/teams/{teamId}/section");
    private final TeamSecurityService teamSecurityService;


    public TransferTeamAuthorizationManager(TeamSecurityService teamSecurityService) {
        this.teamSecurityService = teamSecurityService;
    }

    @Override
    public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authentication, RequestAuthorizationContext context) {
        // Extract the teamId from the request URI: /teams/{teamId}/section
        Map<String, String> uriVariables = TEAM_URI_TEMPLATE.match(context.getRequest().getRequestURI());
        Integer teamIdFromRequestUri = Integer.parseInt(uriVariables.get("teamId"));
        // Only the team owner (course admin) can transfer teams
        return new AuthorizationDecision(this.teamSecurityService.isTeamOwner(teamIdFromRequestUri));
    }

}
