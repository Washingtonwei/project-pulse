package team.projectpulse.ram.requirement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import team.projectpulse.ram.requirement.dto.CreateArtifactLinkRequest;
import team.projectpulse.ram.requirement.dto.UpdateArtifactLinkRequest;
import team.projectpulse.system.exception.ObjectNotFoundException;
import team.projectpulse.team.Team;
import team.projectpulse.team.TeamRepository;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ArtifactLinkService {

    private final RequirementArtifactRepository requirementArtifactRepository;
    private final TeamRepository teamRepository;
    private final ArtifactLinkRepository artifactLinkRepository;

    public ArtifactLinkService(RequirementArtifactRepository requirementArtifactRepository, TeamRepository teamRepository, ArtifactLinkRepository artifactLinkRepository) {
        this.requirementArtifactRepository = requirementArtifactRepository;
        this.teamRepository = teamRepository;
        this.artifactLinkRepository = artifactLinkRepository;
    }

    public Page<ArtifactLink> findByCriteria(Integer teamId, Map<String, String> searchCriteria, Pageable pageable) {
        Specification<ArtifactLink> specs = Specification.unrestricted(); // Start with an unrestricted specification

        if (StringUtils.hasLength(searchCriteria.get("type"))) {
            specs = specs.and(ArtifactLinkSpecs.hasType(searchCriteria.get("type")));
        }

        specs = specs.and(ArtifactLinkSpecs.hasTeamId(teamId));

        return this.artifactLinkRepository.findAll(specs, pageable);
    }

    public ArtifactLink getArtifactLinkById(Integer teamId, Long artifactLinkId) {
        return this.artifactLinkRepository.findById(artifactLinkId)
                .orElseThrow(() -> new ObjectNotFoundException("artifact link", artifactLinkId));
    }

    public ArtifactLink createArtifactLink(Integer teamId, CreateArtifactLinkRequest createArtifactLinkRequest) {
        Team team = this.teamRepository.findById(teamId)
                .orElseThrow(() -> new ObjectNotFoundException("team", teamId));
        RequirementArtifact sourceArtifact = this.requirementArtifactRepository.findById(createArtifactLinkRequest.sourceArtifactId())
                .orElseThrow(() -> new ObjectNotFoundException("requirement artifact", createArtifactLinkRequest.sourceArtifactId()));
        RequirementArtifact targetArtifact = this.requirementArtifactRepository.findById(createArtifactLinkRequest.targetArtifactId())
                .orElseThrow(() -> new ObjectNotFoundException("requirement artifact", createArtifactLinkRequest.targetArtifactId()));

        ArtifactLink artifactLink = new ArtifactLink();
        artifactLink.setTeam(team);
        artifactLink.setType(createArtifactLinkRequest.type());
        artifactLink.setNotes(createArtifactLinkRequest.notes());

        // Set bi-directional relationships
        sourceArtifact.addOutgoingLink(artifactLink);
        targetArtifact.addIncomingLink(artifactLink);

        return this.artifactLinkRepository.save(artifactLink);
    }

    public ArtifactLink updateArtifactLink(Integer teamId, Long artifactLinkId, UpdateArtifactLinkRequest updateArtifactLinkRequest) {
        return this.artifactLinkRepository.findById(artifactLinkId).map(oldArtifactLink -> {
            oldArtifactLink.setType(updateArtifactLinkRequest.type());
            oldArtifactLink.setNotes(updateArtifactLinkRequest.notes());
            return this.artifactLinkRepository.save(oldArtifactLink);
        }).orElseThrow(() -> new ObjectNotFoundException("artifact link", artifactLinkId));
    }

    public void deleteArtifactLink(Integer teamId, Long artifactLinkId) {
        ArtifactLink artifactLink = this.artifactLinkRepository.findById(artifactLinkId)
                .orElseThrow(() -> new ObjectNotFoundException("artifact link", artifactLinkId));

        // Remove bi-directional relationships
        RequirementArtifact sourceArtifact = artifactLink.getSourceArtifact();
        RequirementArtifact targetArtifact = artifactLink.getTargetArtifact();
        if (sourceArtifact != null) {
            sourceArtifact.removeOutgoingLink(artifactLink);
        }
        if (targetArtifact != null) {
            targetArtifact.removeIncomingLink(artifactLink);
        }

        this.artifactLinkRepository.delete(artifactLink);
    }

    public List<ArtifactLink> getOutgoingLinksByRequirementArtifactId(Integer teamId, Long requirementArtifactId) {
        return this.artifactLinkRepository.findBySourceArtifactId(requirementArtifactId, teamId);
    }

    public List<ArtifactLink> getIncomingLinksByRequirementArtifactId(Integer teamId, Long requirementArtifactId) {
        return this.artifactLinkRepository.findByTargetArtifactId(requirementArtifactId, teamId);
    }

}
