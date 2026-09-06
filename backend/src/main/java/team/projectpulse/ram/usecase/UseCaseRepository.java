package team.projectpulse.ram.usecase;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

}
