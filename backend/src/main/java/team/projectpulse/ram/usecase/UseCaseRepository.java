package team.projectpulse.ram.usecase;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UseCaseRepository extends JpaRepository<UseCase, Long> {

    Optional<UseCase> findByIdAndArtifactTeamTeamId(Long id, Integer teamId);

    // Fetch "scalar" associations, Scalar = Single value = @ManyToOne or @OneToOne
    @Query("""
                select uc from UseCase uc
                left join fetch uc.artifact a
                left join fetch uc.primaryActor
                left join fetch uc.lock l
                left join fetch l.lockedBy
                where uc.id = :id
                and a.team.teamId = :teamId
            """)
    Optional<UseCase> findByIdWithScalars(@Param("id") Long id, @Param("teamId") Integer teamId);

    // Fetch "wide" collections separately to avoid Cartesian product
    // Hydration only, keyed on id alone by design. Run it only after a team-scoped entry
    // query has already authorized this id in the same transaction, never as the first
    // load of a request.
    @Query("""
                select uc from UseCase uc
                left join fetch uc.secondaryActors
                left join fetch uc.preconditions
                left join fetch uc.postconditions
                where uc.id = :id
            """)
    Optional<UseCase> findByIdWithManyToMany(@Param("id") Long id);

    // Fetch "deep" graph separately
    // Hydration only, keyed on id alone by design. Run it only after a team-scoped entry
    // query has already authorized this id in the same transaction, never as the first
    // load of a request.
    @Query("""
                select uc from UseCase uc
                left join fetch uc.mainSteps ms
                left join fetch ms.extensions e
                left join fetch e.steps
                where uc.id = :id
            """)
    Optional<UseCase> findByIdWithStepGraph(@Param("id") Long id);

    /**
     * The use cases other than {@code useCaseId} that reference any of the preconditions {@code conditionIds}, one row
     * per reference, ordered by use case id. It feeds the response's {@code otherUseCases} ("Also used by",
     * extension 6b1) only; no save depends on it.
     * <p>
     * How the query reads, clause by clause (JPQL works on entities and their fields, not on tables):
     * <ul>
     *   <li>{@code from UseCase uc}: go through every use case, called {@code uc}.</li>
     *   <li>{@code join uc.artifact a}: follow it to its {@code RequirementArtifact}, which holds its key and title.</li>
     *   <li>{@code join uc.preconditions c}: one row per precondition the use case references, called {@code c}.
     *       Hibernate turns this into a hop through the {@code use_case_precondition} join table.</li>
     *   <li>{@code where c.id in :conditionIds}: keep only the preconditions asked about.</li>
     *   <li>{@code and uc.id <> :useCaseId}: drop the use case the response is for, so only the <em>other</em> use
     *       cases remain.</li>
     *   <li>{@code select new ...ConditionUse(...)}: build one {@link ConditionUse} per remaining row instead of
     *       returning whole entities (a constructor expression).</li>
     *   <li>{@code order by uc.id}: a stable order for each condition's {@code otherUseCases}.</li>
     * </ul>
     * Example: use case 3 links preconditions 4, 5 and 6; use case 12 links 4; use case 15 links 4 and 6. The response
     * for use case 3 calls this with {@code conditionIds = [4, 5, 6]} and {@code useCaseId = 3}, and gets
     * <pre>
     *   ConditionUse(4, 12, "UC-5", "The student edits a use case")
     *   ConditionUse(4, 15, "UC-8", "The student deletes a use case")
     *   ConditionUse(6, 15, "UC-8", "The student deletes a use case")
     * </pre>
     * So condition 4 is shared with UC-5 and UC-8, condition 6 with UC-8, and condition 5, having no row, is unshared.
     * <p>
     * No team filter is needed. The ids passed in are the conditions of a use case already loaded through a
     * team-scoped query, and a use case can only link a condition of its own team, because the team-scoped
     * {@code findRequirementArtifactById(teamId, ...)} guards every link. So every row returned belongs to the
     * caller's team.
     */
    @Query("""
                select new team.projectpulse.ram.usecase.ConditionUse(c.id, uc.id, a.artifactKey, a.title)
                from UseCase uc
                join uc.artifact a
                join uc.preconditions c
                where c.id in :conditionIds
                and uc.id <> :useCaseId
                order by uc.id
            """)
    List<ConditionUse> findOtherUseCasesReferencingPreconditions(@Param("conditionIds") Collection<Long> conditionIds, @Param("useCaseId") Long useCaseId);

    /**
     * As {@link #findOtherUseCasesReferencingPreconditions}, for postconditions: the same query through
     * {@code uc.postconditions} and the {@code use_case_postcondition} join table. See that method for how the query
     * reads, an example, and why no team filter is needed.
     */
    @Query("""
                select new team.projectpulse.ram.usecase.ConditionUse(c.id, uc.id, a.artifactKey, a.title)
                from UseCase uc
                join uc.artifact a
                join uc.postconditions c
                where c.id in :conditionIds
                and uc.id <> :useCaseId
                order by uc.id
            """)
    List<ConditionUse> findOtherUseCasesReferencingPostconditions(@Param("conditionIds") Collection<Long> conditionIds, @Param("useCaseId") Long useCaseId);

}
