package team.projectpulse.ram.usecase;

/**
 * One row saying "this use case references this condition". Every field except {@code conditionId} describes the
 * <em>use case</em> that holds the reference, not the condition; a condition has no title of its own (its wording is
 * its content). Returned by the {@link UseCaseRepository} queries that find which other use cases share a condition.
 * <p>
 * Why it exists: one query answers two questions that {@link UseCaseService} asks about shared conditions
 * (BR-use-case-condition-shared, BR-shared-condition-wording).
 * <ul>
 *   <li>On save, may this condition be reworded here? Only if no row comes back for it; otherwise the save is
 *       refused (extension 7a).</li>
 *   <li>On every response, which other use cases reference each condition? The rows are grouped by
 *       {@code conditionId} and become each condition's {@code otherUseCases}, so the editor can show a shared
 *       condition as read-only (extension 6b1).</li>
 * </ul>
 * It is a record, not a {@link UseCase} or an {@code OtherUseCaseDto}: an entity doesn't say which condition it
 * matched, and a DTO is an API type the repository shouldn't produce, with no {@code conditionId} to group by.
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
