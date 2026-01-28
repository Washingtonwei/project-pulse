package team.projectpulse.ram.document;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DocumentRepository extends JpaRepository<RequirementDocument, Long>, JpaSpecificationExecutor<RequirementDocument> {

    Optional<RequirementDocument> findByTeamTeamIdAndType(Integer teamId, DocumentType type);

    @Query("""
                select rd from RequirementDocument rd
                left join fetch rd.team
                where rd.id = :id
            """)
    Optional<RequirementDocument> findByIdWithScalars(@Param("id") Long id);

    // Fetch "deep" graph separately, but not the outgoing and incoming links of requirement artifacts
    @Query("""
                select rd from RequirementDocument rd
                left join fetch rd.sections s
                left join fetch s.requirementArtifacts ra
                where rd.id = :id
            """)
    Optional<RequirementDocument> findByIdWithSectionGraph(@Param("id") Long id);

}
