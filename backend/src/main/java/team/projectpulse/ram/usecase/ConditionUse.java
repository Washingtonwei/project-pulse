package team.projectpulse.ram.usecase;

/**
 * One row saying "this use case references this condition". Every field except {@code conditionId} describes the
 * <em>use case</em> that holds the reference, not the condition; a condition has no title of its own (its wording is
 * its content). Returned by the {@link UseCaseRepository} queries that find which other use cases share a condition.
 * <p>
 * Why it exists: on every GET, POST and PUT response, {@link UseCaseService#otherUseCasesReferencing} asks which other
 * use cases reference each of a use case's conditions (BR-use-case-condition-shared). The rows are grouped by
 * {@code conditionId} and become each condition's {@code otherUseCases}, which the editor lists as "Also used by"
 * (extension 6b1). The save itself never asks: a use case never rewords a condition, shared or not
 * (BR-shared-condition-wording), so whether one is shared changes nothing about what a save may do.
 * <p>
 * It is a record, not a {@link UseCase} or an {@code OtherUseCaseDto}: an entity doesn't say which condition it
 * matched, and a DTO is an API type the repository shouldn't produce, with no {@code conditionId} to group by.
 * <p>
 * Example: precondition 4, "The Student is logged into the System." (key PRE-student-logged-in), is used by use case 3 and by use
 * case 12. The response for use case 3 asks which <em>other</em> use cases reference condition 4, and gets one row:
 * <pre>
 *   ConditionUse(conditionId = 4, useCaseId = 12, artifactKey = "UC-edit-use-case", title = "The student edits a use case")
 * </pre>
 * that is, condition 4 is also referenced by use case 12, whose artifact key is UC-edit-use-case and whose title is "The student
 * edits a use case". The use case editor shows this as "Also used by UC-edit-use-case: The student edits a use case".
 *
 * @param conditionId the referenced precondition's or postcondition's artifact id
 * @param useCaseId   the id of the use case that references it
 * @param artifactKey that use case's artifact key, such as UC-edit-use-case
 * @param title       that use case's title
 */
public record ConditionUse(Long conditionId, Long useCaseId, String artifactKey, String title) {
}
