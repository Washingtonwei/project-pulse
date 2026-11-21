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
     * of any other type, and links from any other source, are never touched. It revises rather than replaces, in the
     * sense {@code UseCase.reviseMainSteps} uses: nothing still wanted is deleted and recreated, unlike a
     * {@code replace...} method, which clears and refills. A use case names its actors this way
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
    public void reviseOutgoingLinks(Integer teamId, RequirementArtifact source, ArtifactLinkType type, Set<RequirementArtifact> targets) {
        // The comments below follow the Javadoc's example: source UC-1, type SECONDARY_ACTOR, current links to STK-2 and
        // SYS-1, targets {STK-2, STK-3}.

        // Step 1: the ids the caller wants linked, here {STK-2, STK-3}. Targets are compared by id, not by object:
        // ids are what a link stores, and they stay correct even if the caller loaded an artifact twice.
        Set<Long> wantedIds = targets.stream().map(RequirementArtifact::getId).collect(Collectors.toSet());

        // Step 2: this source's links of this one type, here [UC-1 -> STK-2, UC-1 -> SYS-1]. UC-1's PRIMARY_ACTOR and
        // DERIVES_FROM links are filtered out, so they are never touched. The result is copied into a list because
        // step 3 removes links from source.getOutgoingArtifactLinks(), and removing from a set while streaming over
        // it would throw ConcurrentModificationException.
        List<ArtifactLink> current = source.getOutgoingArtifactLinks().stream()
                .filter(link -> link.getType() == type)
                .toList();

        // Step 3: keep or delete each current link.
        // keptIds collects the targets that already have a link to keep, here {STK-2}, so step 4 does not add a
        // second link to them.
        Set<Long> keptIds = new HashSet<>();
        for (ArtifactLink link : current) {
            RequirementArtifact target = link.getTargetArtifact();
            // Keep the link when its target is still wanted and is not linked yet: UC-1 -> STK-2 stays as it is, with
            // its id, notes and created-by. keptIds.add returns false when the target is already kept, so a second
            // link to the same target, which the link routes could have created (OI-77), is deleted below instead.
            if (wantedIds.contains(target.getId()) && keptIds.add(target.getId())) {
                continue;
            }
            // Delete the link: UC-1 -> SYS-1 is no longer wanted. Take it out of both in-memory sets first, so that
            // in the rest of this transaction neither UC-1 nor SYS-1 still lists it (the response is built from
            // UC-1's set). Then delete the row. The artifacts themselves stay; only the link goes.
            source.removeOutgoingLink(link);
            target.removeIncomingLink(link);
            this.artifactLinkRepository.delete(link);
        }

        // Step 4: add a link to each wanted target that has none yet, here STK-3.
        // The team is loaded only when at least one link is added, so a save that changes no actor costs no query.
        Team team = null;
        for (RequirementArtifact target : targets) {
            // STK-2 already has its kept link from step 3.
            if (keptIds.contains(target.getId())) {
                continue;
            }
            if (team == null) {
                team = this.teamRepository.findById(teamId)
                        .orElseThrow(() -> new ObjectNotFoundException("team", teamId));
            }
            // A new link, UC-1 -> STK-3, with the route's team. Notes stay empty: a student adds them, if at all,
            // through the link routes. JPA auditing stamps createdAt and createdBy on save.
            ArtifactLink link = new ArtifactLink();
            link.setTeam(team);
            link.setType(type);
            // Add it to both in-memory sets, as step 3 removes from both, so UC-1 and STK-3 list it at once. These
            // helpers also set the link's source and target.
            source.addOutgoingLink(link);
            target.addIncomingLink(link);
            this.artifactLinkRepository.save(link);
        }
        // Result for the example: UC-1's SECONDARY_ACTOR links are now to STK-2 (the same row as before) and STK-3
        // (new); the SYS-1 link is gone; UC-1's other links are unchanged.
    }

    public List<ArtifactLink> getOutgoingLinksByRequirementArtifactId(Integer teamId, Long requirementArtifactId) {
        return this.artifactLinkRepository.findBySourceArtifactId(requirementArtifactId, teamId);
    }

    public List<ArtifactLink> getIncomingLinksByRequirementArtifactId(Integer teamId, Long requirementArtifactId) {
        return this.artifactLinkRepository.findByTargetArtifactId(requirementArtifactId, teamId);
    }

}
