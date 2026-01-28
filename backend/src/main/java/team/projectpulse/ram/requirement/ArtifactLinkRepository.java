package team.projectpulse.ram.requirement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ArtifactLinkRepository extends JpaRepository<ArtifactLink, Long>, JpaSpecificationExecutor<ArtifactLink> {

    /**
     * Outgoing links (source = current artifact).
     * Fetch only targetArtifact (the "other" artifact).
     */
    @Query("""
                SELECT al FROM ArtifactLink al
                JOIN FETCH al.targetArtifact ta
                Where al.team.teamId = :teamId
                and al.sourceArtifact.id = :requirementArtifactId
            """)
    List<ArtifactLink> findBySourceArtifactId(Long requirementArtifactId, Integer teamId);

    /**
     * Incoming links (target = current artifact).
     * Fetch only sourceArtifact (the "other" artifact).
     */
    @Query("""
                SELECT al FROM ArtifactLink al
                JOIN FETCH al.sourceArtifact sa
                Where al.team.teamId = :teamId
                and al.targetArtifact.id = :requirementArtifactId
            """)
    List<ArtifactLink> findByTargetArtifactId(Long requirementArtifactId, Integer teamId);
}
