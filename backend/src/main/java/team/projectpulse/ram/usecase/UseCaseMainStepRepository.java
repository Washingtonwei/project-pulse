package team.projectpulse.ram.usecase;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UseCaseMainStepRepository extends JpaRepository<UseCaseMainStep, Long> {
    @Query("""
                select distinct ms
                from UseCaseMainStep ms
                left join fetch ms.extensions e
                left join fetch e.steps es
                where ms.useCase.id = :useCaseId
            """)
    List<UseCaseMainStep> fetchMainStepsGraph(@Param("useCaseId") Long useCaseId);
}
