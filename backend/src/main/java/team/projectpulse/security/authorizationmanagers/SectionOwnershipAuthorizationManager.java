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
 * Guards changing one course section and managing its weeks, invitations and instructor list, admitting only its course
 * admin.
 *
 * <p>Reads {@code sectionId} and delegates to {@link SectionSecurityService#isSectionOwner}.
 */
@Component
public class SectionOwnershipAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final SectionSecurityService sectionSecurityService;


    public SectionOwnershipAuthorizationManager(SectionSecurityService sectionSecurityService) {
        this.sectionSecurityService = sectionSecurityService;
    }

    @Override
    public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authentication, RequestAuthorizationContext context) {
        Integer sectionId = PathVariables.readId(context, "sectionId");
        if (sectionId == null) {
            return new AuthorizationDecision(false);
        }
        return new AuthorizationDecision(
                this.sectionSecurityService.isSectionOwner(sectionId));
    }

}
