package team.projectpulse.ram.usecase;

/**
 * One precondition or postcondition as a use case save submits it, in its list's order. An {@code id} links an
 * existing condition of the team as it is; no {@code id} asks for a new one. {@code wording} is the condition's text:
 * for a new condition it becomes the artifact's {@code content}, and for an existing one it is ignored and the stored
 * {@code content} stands, since a use case never rewords a condition. {@link UseCaseService} decides what each entry
 * means (BR-use-case-condition-shared, BR-shared-condition-wording).
 *
 * @param id      an existing condition's artifact id, or null for a new condition
 * @param wording the condition's text; ignored for an existing condition
 */
public record SubmittedCondition(Long id, String wording) {
}
