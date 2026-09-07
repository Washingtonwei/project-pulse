package team.projectpulse.security.authorizationmanagers;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import team.projectpulse.rubric.RubricSecurityService;

import java.util.function.Supplier;

/**
 * Guards attaching a criterion to a rubric, admitting the rubric's course admin and only when both sit in the same
 * course.
 *
 * <p>Reads {@code rubricId} and {@code criterionId} and delegates to {@link RubricSecurityService}.
 */
@Component
public class AssignCriterionToRubricAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final RubricSecurityService rubricSecurityService;


    public AssignCriterionToRubricAuthorizationManager(RubricSecurityService rubricSecurityService) {
        this.rubricSecurityService = rubricSecurityService;
    }

    @Override
    public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authentication, RequestAuthorizationContext context) {
        Integer rubricId = PathVariables.readId(context, "rubricId");
        Integer criterionId = PathVariables.readId(context, "criterionId");
        if (rubricId == null || criterionId == null) {
            return new AuthorizationDecision(false);
        }
        return new AuthorizationDecision(
                this.rubricSecurityService.isRubricOwner(rubricId)
                        && this.rubricSecurityService.isRubricAndCriterionInSameCourse(rubricId, criterionId));
    }

}
