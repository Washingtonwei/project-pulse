package team.projectpulse.security.authorizationmanagers;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import team.projectpulse.team.TeamSecurityService;

import java.util.function.Supplier;

/**
 * Guards changing and deleting one team, and creating or deleting its requirement documents, admitting only the team's
 * course admin.
 *
 * <p>Reads {@code teamId} and delegates to {@link TeamSecurityService#isTeamOwner}.
 */
@Component
public class TeamOwnershipAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final TeamSecurityService teamSecurityService;


    public TeamOwnershipAuthorizationManager(TeamSecurityService teamSecurityService) {
        this.teamSecurityService = teamSecurityService;
    }

    @Override
    public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authentication, RequestAuthorizationContext context) {
        Integer teamId = PathVariables.readId(context, "teamId");
        if (teamId == null) {
            return new AuthorizationDecision(false);
        }
        return new AuthorizationDecision(
                this.teamSecurityService.isTeamOwner(teamId));
    }

}
