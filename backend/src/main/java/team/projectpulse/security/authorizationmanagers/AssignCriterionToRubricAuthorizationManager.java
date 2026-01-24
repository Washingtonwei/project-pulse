package team.projectpulse.security.authorizationmanagers;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriTemplate;
import team.projectpulse.rubric.RubricSecurityService;

import java.util.Map;
import java.util.function.Supplier;

@Component
public class AssignCriterionToRubricAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private static final UriTemplate RUBRIC_URI_TEMPLATE = new UriTemplate("/rubrics/{rubricId}/criteria/{criterionId}");
    private final RubricSecurityService rubricSecurityService;


    public AssignCriterionToRubricAuthorizationManager(RubricSecurityService rubricSecurityService) {
        this.rubricSecurityService = rubricSecurityService;
    }

    @Override
    public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authentication, RequestAuthorizationContext context) {
        // Extract the rubricId and criterionId from the request URI: /rubrics/{rubricId}/criteria/{criterionId}
        Map<String, String> uriVariables = RUBRIC_URI_TEMPLATE.match(context.getRequest().getRequestURI());
        Integer rubricIdFromRequestUri = Integer.parseInt(uriVariables.get("rubricId"));
        Integer criterionIdFromRequestUri = Integer.parseInt(uriVariables.get("criterionId"));

        return new AuthorizationDecision(
                this.rubricSecurityService.isRubricOwner(rubricIdFromRequestUri) &&
                        this.rubricSecurityService.isRubricAndCriterionInSameCourse(rubricIdFromRequestUri, criterionIdFromRequestUri));
    }

}
