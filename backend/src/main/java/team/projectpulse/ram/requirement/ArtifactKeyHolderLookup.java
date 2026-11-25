package team.projectpulse.ram.requirement;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import team.projectpulse.ram.requirement.dto.ArtifactKeyHolderDto;

import java.util.Optional;

/**
 * Finds which artifact of a team holds a key, in a transaction of its own. {@link RequirementArtifactService} calls it
 * only after a write hit the unique key index ({@link ArtifactKeys#KEY_INDEX}): by then the request's own transaction
 * is marked for rollback and its persistence context can no longer be trusted, but the refusal must still name the
 * holder (UC-ART-create-artifact 8c, UC-ART-edit-artifact 10b). For example: John and Eric both submit
 * {@code FR-export-csv}; both checks find it free, Eric's insert commits first, and John's insert fails on the index.
 * This lookup, on a fresh connection, sees Eric's committed row and returns it, so John reads "FR-export-csv is already
 * used by FR-export-csv: Data export." It runs read-only and never writes.
 */
@Component
public class ArtifactKeyHolderLookup {

    private final RequirementArtifactRepository requirementArtifactRepository;

    public ArtifactKeyHolderLookup(RequirementArtifactRepository requirementArtifactRepository) {
        this.requirementArtifactRepository = requirementArtifactRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Optional<ArtifactKeyHolderDto> holderOf(Integer teamId, String artifactKey) {
        return this.requirementArtifactRepository.findByTeamTeamIdAndArtifactKey(teamId, artifactKey).map(ArtifactKeyHolderDto::of);
    }

}
