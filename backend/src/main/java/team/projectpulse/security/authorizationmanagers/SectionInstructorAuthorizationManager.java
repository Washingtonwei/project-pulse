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
 * Guards the course-section-wide weekly peer evaluation report, admitting only an instructor assigned to that course
 * section.
 *
 * <p>Reads {@code sectionId} and delegates to {@link SectionSecurityService#isSectionInstructor}.
 */
@Component
public class SectionInstructorAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final SectionSecurityService sectionSecurityService;


    public SectionInstructorAuthorizationManager(SectionSecurityService sectionSecurityService) {
        this.sectionSecurityService = sectionSecurityService;
    }

    @Override
    public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authentication, RequestAuthorizationContext context) {
        Integer sectionId = PathVariables.readId(context, "sectionId");
        if (sectionId == null) {
            return new AuthorizationDecision(false);
        }
        return new AuthorizationDecision(
                this.sectionSecurityService.isSectionInstructor(sectionId));
    }

}
