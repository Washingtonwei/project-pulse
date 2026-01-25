package team.projectpulse.security.authorizationmanagers;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationResult;
import team.projectpulse.section.SectionSecurityService;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriTemplate;

import java.util.Map;
import java.util.function.Supplier;

@Component
public class AssignRubricToSectionAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private static final UriTemplate SECTION_URI_TEMPLATE = new UriTemplate("/sections/{sectionId}/rubrics/{rubricId}");
    private final SectionSecurityService sectionSecurityService;


    public AssignRubricToSectionAuthorizationManager(SectionSecurityService sectionSecurityService) {
        this.sectionSecurityService = sectionSecurityService;
    }

    @Override
    public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authentication, RequestAuthorizationContext context) {
        // Extract the sectionId and rubricId from the request URI: /sections/{sectionId}/rubrics/{rubricId}
        Map<String, String> uriVariables = SECTION_URI_TEMPLATE.match(context.getRequest().getRequestURI());
        Integer sectionIdFromRequestUri = Integer.parseInt(uriVariables.get("sectionId"));
        Integer rubricIdFromRequestUri = Integer.parseInt(uriVariables.get("rubricId"));
        return new AuthorizationDecision(
                this.sectionSecurityService.isSectionOwner(sectionIdFromRequestUri) &&
                        this.sectionSecurityService.isSectionAndRubricInSameCourse(sectionIdFromRequestUri, rubricIdFromRequestUri)
        );
    }

}
