package team.projectpulse.security.authorizationmanagers;

import org.jspecify.annotations.Nullable;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

/**
 * Reads the URI template variables that the matched route rule already extracted.
 *
 * <p>An {@code AuthorizationManager} must never re-parse the request URI itself. The route rule in
 * {@code SecurityConfiguration} and the manager bound to it would then be two independent parses of the
 * same path, over two different strings (the parsed request path versus the raw {@code getRequestURI()}),
 * free to disagree and with nothing to detect the drift. {@code RequestAuthorizationContext.getVariables()}
 * hands back the very parse that selected the rule.
 *
 * <p>A variable that is missing or not a number yields {@code null}, and every caller must translate that
 * into a denial. A guard that cannot tell which object is being named has no business allowing the request.
 */
final class PathVariables {

    private PathVariables() {
    }

    /**
     * @return the named path variable as an id, or {@code null} if the matched route did not bind that
     * variable or its value is not an integer
     */
    static @Nullable Integer readId(RequestAuthorizationContext context, String variableName) {
        String value = context.getVariables().get(variableName);
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

}
