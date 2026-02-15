package team.projectpulse.ram.document;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DocumentSectionRepository extends JpaRepository<DocumentSection, Long> {

    /**
     * Full graph fetch for DocumentSection detail endpoint.
     * Validates team + document ownership and loads all DTO-required relations.
     */
    @Query("""
            select distinct ds
            from DocumentSection ds
              join ds.document d
              join d.team t
              left join fetch ds.requirementArtifacts ra
              left join fetch ds.lock l
              left join fetch ds.createdBy cb
              left join fetch ds.updatedBy ub
            where ds.id = :documentSectionId
              and d.id  = :documentId
              and t.teamId  = :teamId
            """)
    Optional<DocumentSection> findByIdWithFullGraph(
            @Param("teamId") Integer teamId,
            @Param("documentId") Long documentId,
            @Param("documentSectionId") Long documentSectionId
    );

}
