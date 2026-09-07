package team.projectpulse.security.authorizationmanagers;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import team.projectpulse.team.TeamSecurityService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * The path-variable contract every {@code AuthorizationManager} in this package shares, exercised through one
 * of them: the id comes from the variables the matched route rule bound, and a variable the manager cannot
 * resolve is a denial rather than an exception.
 */
@ExtendWith(MockitoExtension.class)
class TeamMembershipAuthorizationManagerTest {

    @Mock
    TeamSecurityService teamSecurityService;

    @InjectMocks
    TeamMembershipAuthorizationManager teamMembershipAuthorizationManager;


    private RequestAuthorizationContext contextWith(Map<String, String> variables) {
        return new RequestAuthorizationContext(mock(HttpServletRequest.class), variables);
    }

    @Test
    @DisplayName("Asks about the id the matched route bound, not one parsed out of the request URI")
    void delegatesTheBoundTeamId() {
        given(this.teamSecurityService.canAccessTeam(7)).willReturn(true);

        AuthorizationResult result = this.teamMembershipAuthorizationManager.authorize(() -> null, contextWith(Map.of("teamId", "7")));

        assertThat(result).isNotNull();
        assertThat(result.isGranted()).isTrue();
    }

    @Test
    @DisplayName("Denies when the matched route bound no teamId at all")
    void deniesWhenTheVariableIsAbsent() {
        AuthorizationResult result = this.teamMembershipAuthorizationManager.authorize(() -> null, contextWith(Map.of()));

        assertThat(result).isNotNull();
        assertThat(result.isGranted()).isFalse();
        // The guard cannot tell which team is being named, so it must not ask a question it would misread
        verifyNoInteractions(this.teamSecurityService);
    }

    @Test
    @DisplayName("Denies when the bound teamId is not a number")
    void deniesWhenTheVariableIsNotANumber() {
        AuthorizationResult result = this.teamMembershipAuthorizationManager.authorize(() -> null, contextWith(Map.of("teamId", "not-a-number")));

        assertThat(result).isNotNull();
        assertThat(result.isGranted()).isFalse();
        verifyNoInteractions(this.teamSecurityService);
    }

}
