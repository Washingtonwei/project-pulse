package team.projectpulse.ram.usecase;

/**
 * One row saying "this use case references this condition". Every field except {@code conditionId} describes the
 * <em>use case</em> that holds the reference, not the condition; a condition has no title of its own (its wording is
 * its content). Returned by the {@link UseCaseRepository} queries that find which other use cases share a condition.
 * <p>
 * Example: precondition 4, "The Student is logged into the System." (key PRC-1), is used by use case 3 and by use
 * case 12. Saving use case 3 asks which <em>other</em> use cases reference condition 4, and gets one row:
 * <pre>
 *   ConditionUse(conditionId = 4, useCaseId = 12, artifactKey = "UC-5", title = "The student edits a use case")
 * </pre>
 * that is, condition 4 is also referenced by use case 12, whose artifact key is UC-5 and whose title is "The student
 * edits a use case". The use case editor shows this as "Also used by UC-5: The student edits a use case".
 *
 * @param conditionId the referenced precondition's or postcondition's artifact id
 * @param useCaseId   the id of the use case that references it
 * @param artifactKey that use case's artifact key, such as UC-5
 * @param title       that use case's title
 */
public record ConditionUse(Long conditionId, Long useCaseId, String artifactKey, String title) {
}
