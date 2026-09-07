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
 * Guards assigning a rubric to a course section, admitting the section's course admin and only when the rubric belongs
 * to the same course.
 *
 * <p>Reads {@code sectionId} and {@code rubricId} and delegates to {@link SectionSecurityService}.
 */
@Component
public class AssignRubricToSectionAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final SectionSecurityService sectionSecurityService;


    public AssignRubricToSectionAuthorizationManager(SectionSecurityService sectionSecurityService) {
        this.sectionSecurityService = sectionSecurityService;
    }

    @Override
    public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authentication, RequestAuthorizationContext context) {
        Integer sectionId = PathVariables.readId(context, "sectionId");
        Integer rubricId = PathVariables.readId(context, "rubricId");
        if (sectionId == null || rubricId == null) {
            return new AuthorizationDecision(false);
        }
        return new AuthorizationDecision(
                this.sectionSecurityService.isSectionOwner(sectionId)
                        && this.sectionSecurityService.isSectionAndRubricInSameCourse(sectionId, rubricId));
    }

}
