package team.projectpulse.ram.usecase;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UseCaseRepository extends JpaRepository<UseCase, Long> {

    // Fetch "scalar" associations, Scalar = Single value = @ManyToOne or @OneToOne
    @Query("""
                select uc from UseCase uc
                left join fetch uc.artifact
                left join fetch uc.primaryActor
                where uc.id = :id
            """)
    Optional<UseCase> findByIdWithScalars(@Param("id") Long id);

    // Fetch "wide" collections separately to avoid Cartesian product
    @Query("""
                select uc from UseCase uc
                left join fetch uc.secondaryActors
                left join fetch uc.preconditions
                left join fetch uc.postconditions
                where uc.id = :id
            """)
    Optional<UseCase> findByIdWithManyToMany(@Param("id") Long id);

    // Fetch "deep" graph separately
    @Query("""
                select uc from UseCase uc
                left join fetch uc.mainSteps ms
                left join fetch ms.extensions e
                left join fetch e.steps
                where uc.id = :id
            """)
    Optional<UseCase> findByIdWithStepGraph(@Param("id") Long id);

    @Query("""
                select uc from UseCase uc
                left join fetch uc.artifact
                where uc.id = :id
            """)
    Optional<UseCase> findByIdWithBasicInfo(@Param("id") Long id);

}
