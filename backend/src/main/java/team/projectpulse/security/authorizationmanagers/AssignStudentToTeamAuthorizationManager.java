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
public class AssignStudentToTeamAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private static final UriTemplate TEAM_URI_TEMPLATE = new UriTemplate("/teams/{teamId}/students/{studentId}");
    private final TeamSecurityService teamSecurityService;


    public AssignStudentToTeamAuthorizationManager(TeamSecurityService teamSecurityService) {
        this.teamSecurityService = teamSecurityService;
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext context) {
        // Extract the teamId and studentId from the request URI: /teams/{teamId}/students/{studentId}
        Map<String, String> uriVariables = TEAM_URI_TEMPLATE.match(context.getRequest().getRequestURI());
        Integer teamIdFromRequestUri = Integer.parseInt(uriVariables.get("teamId"));
        Integer studentIdFromRequestUri = Integer.parseInt(uriVariables.get("studentId"));
        return new AuthorizationDecision(
                this.teamSecurityService.isTeamOwner(teamIdFromRequestUri) &&
                        this.teamSecurityService.isTeamAndStudentInSameSection(teamIdFromRequestUri, studentIdFromRequestUri)
        );
    }

}
