package team.projectpulse.ram.usecase;

/**
 * One precondition or postcondition as a use case save submits it, in its list's order. An {@code id} links an
 * existing condition of the team as it is; no {@code id} asks for a new one. {@code wording} is the condition's text:
 * for a new condition it becomes the artifact's {@code content}, and for an existing one it is ignored and the stored
 * {@code content} stands, since a use case never rewords a condition. {@code artifactKey} is the same: the key the
 * student gave a new condition (UC-DOC-create-use-case, "Artifact keys"), ignored for an existing one, which keeps
 * its stored key. {@link UseCaseService} decides what each entry means (BR-use-case-condition-shared,
 * BR-shared-condition-wording). For example:
 * <pre>
 * (null, "The student is logged into the system.", "PRE-student-logged-in")   a new precondition with its key
 * (14,   "anything",                               "PRE-anything")            precondition 14 as stored; both ignored
 * </pre>
 *
 * @param id          an existing condition's artifact id, or null for a new condition
 * @param wording     the condition's text; ignored for an existing condition
 * @param artifactKey a new condition's artifact key; ignored for an existing condition
 */
public record SubmittedCondition(Long id, String wording, String artifactKey) {
}
