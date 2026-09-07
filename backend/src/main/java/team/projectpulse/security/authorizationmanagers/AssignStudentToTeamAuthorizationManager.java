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
 * Guards adding and removing a student on a team, admitting the team's course admin and only when that student is
 * enrolled in the team's course section.
 *
 * <p>Reads {@code teamId} and {@code studentId} and delegates to {@link TeamSecurityService}.
 */
@Component
public class AssignStudentToTeamAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final TeamSecurityService teamSecurityService;


    public AssignStudentToTeamAuthorizationManager(TeamSecurityService teamSecurityService) {
        this.teamSecurityService = teamSecurityService;
    }

    @Override
    public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authentication, RequestAuthorizationContext context) {
        Integer teamId = PathVariables.readId(context, "teamId");
        Integer studentId = PathVariables.readId(context, "studentId");
        if (teamId == null || studentId == null) {
            return new AuthorizationDecision(false);
        }
        return new AuthorizationDecision(
                this.teamSecurityService.isTeamOwner(teamId)
                        && this.teamSecurityService.isTeamAndStudentInSameSection(teamId, studentId));
    }

}
