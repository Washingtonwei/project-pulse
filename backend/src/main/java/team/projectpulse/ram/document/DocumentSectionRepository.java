package team.projectpulse.ram.document;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DocumentSectionRepository extends JpaRepository<DocumentSection, Long> {

    // Team-scoped lookup that also enforces the section belongs to the document in the URL.
    Optional<DocumentSection> findByIdAndDocumentIdAndDocumentTeamTeamId(Long id, Long documentId, Integer teamId);

    // A document's section by its key: how a requirement artifact's home is found once its document is known
    // (BR-artifact-home). It must not fetch requirementArtifacts: the list is read only after the row lock below
    // (RequirementArtifactService.createRequirementArtifact).
    Optional<DocumentSection> findByDocumentIdAndSectionKey(Long documentId, String sectionKey);

    /**
     * Locks the document section's row (SELECT ... FOR UPDATE) until the transaction ends, so changes to its list run
     * one at a time and two appends cannot take the same artifact_index. A one-table query on purpose: a lock taken
     * through a join would also lock the document and team rows. Not team-scoped: call it only with an id a
     * team-scoped lookup has already returned.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ds from DocumentSection ds where ds.id = :id")
    Optional<DocumentSection> findByIdForUpdate(@Param("id") Long id);

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
