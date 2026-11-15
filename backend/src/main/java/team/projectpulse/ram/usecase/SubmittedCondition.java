package team.projectpulse.ram.usecase;

/**
 * One precondition or postcondition as a use case save submits it, in its list's order. An {@code id} picks an
 * existing condition of the team; no {@code id} asks for a new one. {@code wording} is the condition's text, stored as
 * the artifact's {@code content}. {@link UseCaseService} decides what each entry means (BR-use-case-condition-shared,
 * BR-shared-condition-wording).
 *
 * @param id      an existing condition's artifact id, or null for a new condition
 * @param wording the condition's text
 */
public record SubmittedCondition(Long id, String wording) {
}
