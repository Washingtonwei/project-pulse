package team.projectpulse.ram.requirement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface RequirementArtifactRepository extends JpaRepository<RequirementArtifact, Long>, JpaSpecificationExecutor<RequirementArtifact> {

    // Team-scoped lookups. The scope lives in the query, so an out-of-scope row never loads at all.
    Optional<RequirementArtifact> findByIdAndTeamTeamId(Long id, Integer teamId);

    Optional<RequirementArtifact> findByIdAndTeamTeamIdAndType(Long id, Integer teamId, RequirementArtifactType type);

    // The artifact of the team holding an exact key, for the key checks (BR-artifact-key-unique). The match follows
    // the column's collation, which ignores case; slugs are lowercase, so that changes nothing.
    Optional<RequirementArtifact> findByTeamTeamIdAndArtifactKey(Integer teamId, String artifactKey);

}
