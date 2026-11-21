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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        return this.artifactLinkRepository.findByIdAndTeamTeamId(artifactLinkId, teamId)
                .orElseThrow(() -> new ObjectNotFoundException("artifact link", artifactLinkId));
    }

    public ArtifactLink createArtifactLink(Integer teamId, CreateArtifactLinkRequest createArtifactLinkRequest) {
        Team team = this.teamRepository.findById(teamId)
                .orElseThrow(() -> new ObjectNotFoundException("team", teamId));
        RequirementArtifact sourceArtifact = this.requirementArtifactRepository.findByIdAndTeamTeamId(createArtifactLinkRequest.sourceArtifactId(), teamId)
                .orElseThrow(() -> new ObjectNotFoundException("requirement artifact", createArtifactLinkRequest.sourceArtifactId()));
        RequirementArtifact targetArtifact = this.requirementArtifactRepository.findByIdAndTeamTeamId(createArtifactLinkRequest.targetArtifactId(), teamId)
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
        return this.artifactLinkRepository.findByIdAndTeamTeamId(artifactLinkId, teamId).map(oldArtifactLink -> {
            oldArtifactLink.setType(updateArtifactLinkRequest.type());
            oldArtifactLink.setNotes(updateArtifactLinkRequest.notes());
            return this.artifactLinkRepository.save(oldArtifactLink);
        }).orElseThrow(() -> new ObjectNotFoundException("artifact link", artifactLinkId));
    }

    public void deleteArtifactLink(Integer teamId, Long artifactLinkId) {
        ArtifactLink artifactLink = this.artifactLinkRepository.findByIdAndTeamTeamId(artifactLinkId, teamId)
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

    /**
     * Makes {@code source}'s outgoing links of {@code type} point at exactly {@code targets}, changing as little as
     * possible: a link whose target is still wanted is kept as it is (its id, notes and created-by survive), a link
     * whose target is no longer wanted is deleted, and each new target gets a new link with team {@code teamId}. Links
     * of any other type, and links from any other source, are never touched. A use case names its actors this way
     * (BR-use-case-actor-types, docs/design/doc.md "Actors as links"). For example, with UC-1's secondary actors
     * {STK-2, SYS-1} and {@code targets} {STK-2, STK-3}, the STK-2 link stays, the SYS-1 link is deleted, and a link to
     * STK-3 is added.
     * <p>
     * It checks nothing about the artifacts: the caller passes artifacts it has already loaded through a scoped finder
     * of {@code teamId} and checked for type, as {@code UseCaseService.resolveActors} does. The in-memory link sets of
     * the source and of every target are kept in step with the database, so a response built later in the same
     * transaction reads the new links.
     *
     * @param teamId  the team named in the route, the team of every new link
     * @param source  the managed artifact the links start from, already in {@code teamId}
     * @param type    the one link type to replace
     * @param targets the managed artifacts the links must end at, already in {@code teamId}; empty removes them all
     */
    public void replaceOutgoingLinks(Integer teamId, RequirementArtifact source, ArtifactLinkType type, Set<RequirementArtifact> targets) {
        Set<Long> wantedIds = targets.stream().map(RequirementArtifact::getId).collect(Collectors.toSet());

        // Copy the current links of this type first, since removing a link changes the set being read.
        List<ArtifactLink> current = source.getOutgoingArtifactLinks().stream()
                .filter(link -> link.getType() == type)
                .toList();
        Set<Long> keptIds = new HashSet<>();
        for (ArtifactLink link : current) {
            RequirementArtifact target = link.getTargetArtifact();
            // keptIds.add is false for a second link to the same target, so a duplicate left by the link routes goes.
            if (wantedIds.contains(target.getId()) && keptIds.add(target.getId())) {
                continue;
            }
            source.removeOutgoingLink(link);
            target.removeIncomingLink(link);
            this.artifactLinkRepository.delete(link);
        }

        Team team = null;
        for (RequirementArtifact target : targets) {
            if (keptIds.contains(target.getId())) {
                continue;
            }
            if (team == null) {
                team = this.teamRepository.findById(teamId)
                        .orElseThrow(() -> new ObjectNotFoundException("team", teamId));
            }
            ArtifactLink link = new ArtifactLink();
            link.setTeam(team);
            link.setType(type);
            source.addOutgoingLink(link);
            target.addIncomingLink(link);
            this.artifactLinkRepository.save(link);
        }
    }

    public List<ArtifactLink> getOutgoingLinksByRequirementArtifactId(Integer teamId, Long requirementArtifactId) {
        return this.artifactLinkRepository.findBySourceArtifactId(requirementArtifactId, teamId);
    }

    public List<ArtifactLink> getIncomingLinksByRequirementArtifactId(Integer teamId, Long requirementArtifactId) {
        return this.artifactLinkRepository.findByTargetArtifactId(requirementArtifactId, teamId);
    }

}
