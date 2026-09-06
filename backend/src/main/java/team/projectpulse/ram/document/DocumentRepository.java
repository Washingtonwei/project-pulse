package team.projectpulse.ram.document;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DocumentRepository extends JpaRepository<RequirementDocument, Long>, JpaSpecificationExecutor<RequirementDocument> {

    Optional<RequirementDocument> findByTeamTeamIdAndType(Integer teamId, DocumentType type);

    Optional<RequirementDocument> findByIdAndTeamTeamId(Long id, Integer teamId);

    @Query("""
                select rd from RequirementDocument rd
                left join fetch rd.team t
                where rd.id = :id
                and t.teamId = :teamId
            """)
    Optional<RequirementDocument> findByIdWithScalars(@Param("id") Long id, @Param("teamId") Integer teamId);

    // Fetch "deep" graph separately, but not the outgoing and incoming links of requirement artifacts
    // Hydration only, keyed on id alone by design. Run it only after a team-scoped entry
    // query has already authorized this id in the same transaction, never as the first
    // load of a request.
    @Query("""
                select rd from RequirementDocument rd
                left join fetch rd.sections s
                left join fetch s.requirementArtifacts ra
                where rd.id = :id
            """)
    Optional<RequirementDocument> findByIdWithSectionGraph(@Param("id") Long id);

}
