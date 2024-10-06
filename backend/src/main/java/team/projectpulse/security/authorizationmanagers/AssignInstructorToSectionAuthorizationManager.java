package team.projectpulse.security.authorizationmanagers;

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
public class AssignInstructorToSectionAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private static final UriTemplate SECTION_URI_TEMPLATE = new UriTemplate("/sections/{sectionId}/instructors/{instructorId}");
    private final SectionSecurityService sectionSecurityService;


    public AssignInstructorToSectionAuthorizationManager(SectionSecurityService sectionSecurityService) {
        this.sectionSecurityService = sectionSecurityService;
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext context) {
        // Extract the sectionId and instructorId from the request URI: /sections/{sectionId}/instructors/{instructorId}
        Map<String, String> uriVariables = SECTION_URI_TEMPLATE.match(context.getRequest().getRequestURI());
        Integer sectionIdFromRequestUri = Integer.parseInt(uriVariables.get("sectionId"));
        Integer instructorIdFromRequestUri = Integer.parseInt(uriVariables.get("instructorId"));
        return new AuthorizationDecision(
                this.sectionSecurityService.isSectionOwner(sectionIdFromRequestUri) &&
                        this.sectionSecurityService.isSectionAndInstructorInSameCourse(sectionIdFromRequestUri, instructorIdFromRequestUri)
        );
    }

}
