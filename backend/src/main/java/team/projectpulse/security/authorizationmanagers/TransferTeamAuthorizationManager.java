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
 * Guards moving a team to another course section, admitting only the team's course admin.
 *
 * <p>Reads {@code teamId} and delegates to {@link TeamSecurityService#isTeamOwner}.
 */
@Component
public class TransferTeamAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final TeamSecurityService teamSecurityService;


    public TransferTeamAuthorizationManager(TeamSecurityService teamSecurityService) {
        this.teamSecurityService = teamSecurityService;
    }

    @Override
    public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authentication, RequestAuthorizationContext context) {
        Integer teamId = PathVariables.readId(context, "teamId");
        if (teamId == null) {
            return new AuthorizationDecision(false);
        }
        // Only the team owner (course admin) can transfer a team to another course section
        return new AuthorizationDecision(
                this.teamSecurityService.isTeamOwner(teamId));
    }

}
