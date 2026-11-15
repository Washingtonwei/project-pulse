package team.projectpulse.ram.usecase;

/**
 * One use case's reference to a condition: the condition's id, and the referencing use case's id, artifact key and
 * title. Returned by the {@link UseCaseRepository} queries that tell whether a condition is shared.
 */
public record ConditionUse(Long conditionId, Long useCaseId, String artifactKey, String title) {
}
