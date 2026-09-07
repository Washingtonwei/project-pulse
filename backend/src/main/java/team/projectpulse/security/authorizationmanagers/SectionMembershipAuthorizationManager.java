package team.projectpulse.security.authorizationmanagers;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import team.projectpulse.section.SectionSecurityService;

import java.util.function.Supplier;

/**
 * Guards reading one course section, admitting a student enrolled in it or an instructor assigned to it.
 *
 * <p>Reads {@code sectionId} and delegates to {@link SectionSecurityService#canAccessSection}.
 */
@Component
public class SectionMembershipAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final SectionSecurityService sectionSecurityService;


    public SectionMembershipAuthorizationManager(SectionSecurityService sectionSecurityService) {
        this.sectionSecurityService = sectionSecurityService;
    }

    @Override
    public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authentication, RequestAuthorizationContext context) {
        Integer sectionId = PathVariables.readId(context, "sectionId");
        if (sectionId == null) {
            return new AuthorizationDecision(false);
        }
        return new AuthorizationDecision(
                this.sectionSecurityService.canAccessSection(sectionId));
    }

}
