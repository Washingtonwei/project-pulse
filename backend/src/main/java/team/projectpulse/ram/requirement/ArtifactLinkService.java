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
     * (BR-use-case-actor-types, docs/design/doc.md "Actors as links"). For example, with UC-report's secondary actors
     * {STK-tutor, SYS-email} and {@code targets} {STK-tutor, STK-grader}, the STK-tutor link stays, the SYS-email link
     * is deleted, and a link to STK-grader is added.
     * <p>
     * It checks nothing about the artifacts: the caller passes artifacts it has already loaded through a scoped finder
     * of {@code teamId} and checked for type, as {@code UseCaseService.resolveActors} does. The in-memory link sets of
     * the source and of every target are kept in step with the database, so a response built later in the same
     * transaction reads the new links.
     *
     * @param teamId  the team named in the route, the team of every new link
     * @param source  the managed artifact the links start from, already in {@code teamId}
     * @param type    the one link type to revise
     * @param targets the managed artifacts the links must end at, already in {@code teamId}; empty removes them all
     */
    public void reviseOutgoingLinks(Integer teamId, RequirementArtifact source, ArtifactLinkType type, Set<RequirementArtifact> targets) {
        reviseLinks(teamId, source, LinkEnd.SOURCE, type, targets);
    }

    /**
     * The mirror of {@link #reviseOutgoingLinks}: makes {@code target}'s incoming links of {@code type} come from
     * exactly {@code sources}, with the same keep, delete and add rule. Links of any other type, the links that end at
     * any other artifact, and {@code target}'s own outgoing links are never touched. A use case names its business
     * rules this way, as {@code CONSTRAINS} links from each rule to the use case (BR-use-case-business-rules,
     * docs/design/doc.md "Business rules as links"). For example, with UC-report constrained by {RULE-team, RULE-late}
     * and {@code sources} {RULE-late, RULE-due}, the RULE-late link stays, the RULE-team link is deleted, and a link
     * from RULE-due is added. RULE-late's link to FR-notify is never looked at: it ends at FR-notify, not at UC-report,
     * so it is in FR-notify's incoming set and outside this revise. It would stay even if RULE-late were dropped from
     * UC-report.
     * <p>
     * As with {@link #reviseOutgoingLinks}, it checks nothing about the artifacts: the caller passes artifacts already
     * loaded through a scoped finder of {@code teamId} and checked for type, as
     * {@code UseCaseService.resolveBusinessRules} does.
     *
     * @param teamId  the team named in the route, the team of every new link
     * @param target  the managed artifact the links end at, already in {@code teamId}
     * @param type    the one link type to revise
     * @param sources the managed artifacts the links must start from, already in {@code teamId}; empty removes them all
     */
    public void reviseIncomingLinks(Integer teamId, RequirementArtifact target, ArtifactLinkType type, Set<RequirementArtifact> sources) {
        reviseLinks(teamId, target, LinkEnd.TARGET, type, sources);
    }

    /**
     * Which end of its links the artifact being revised, the <em>anchor</em>, sits at. Everything that differs between
     * revising outgoing and incoming links is one of the four methods below, so {@link #reviseLinks} holds the keep,
     * delete and add algorithm once and never asks which direction it is working in.
     * <p>
     * Every link is held in two in-memory sets: its source's outgoing set and its target's incoming set. For the link
     * {@code UC-report -> STK-tutor} (type SECONDARY_ACTOR) and the link {@code RULE-team -> UC-report} (type
     * CONSTRAINS):
     * <pre>
     *   link                     source     in source's set   target     in target's set
     *   UC-report -> STK-tutor   UC-report  outgoing          STK-tutor  incoming
     *   RULE-team -> UC-report   RULE-team  outgoing          UC-report  incoming
     * </pre>
     * UC-report is the anchor in both cases, but at a different end:
     * <ul>
     *   <li>Revising UC-report's actors, {@code reviseOutgoingLinks} uses {@link #SOURCE}: UC-report is the source,
     *       the links are in its outgoing set, and the artifact at the other end is the actor, the target.</li>
     *   <li>Revising UC-report's business rules, {@code reviseIncomingLinks} uses {@link #TARGET}: UC-report is the
     *       target, the links are in its incoming set, and the artifact at the other end is the rule, the source.</li>
     * </ul>
     * The table the four methods give for each end, using the same two links:
     * <pre>
     *   method       SOURCE (anchor UC-report,                 TARGET (anchor UC-report,
     *                link UC-report -> STK-tutor)              link RULE-team -> UC-report)
     *   links        UC-report's outgoing set                  UC-report's incoming set
     *   otherEnd     STK-tutor, the link's target              RULE-team, the link's source
     *   connect      UC-report.addOutgoingLink,                RULE-team.addOutgoingLink,
     *                STK-tutor.addIncomingLink                 UC-report.addIncomingLink
     *   disconnect   UC-report.removeOutgoingLink,             RULE-team.removeOutgoingLink,
     *                STK-tutor.removeIncomingLink              UC-report.removeIncomingLink
     * </pre>
     * {@code connect} and {@code disconnect} always touch both artifacts: the anchor and {@code other}. Updating only
     * the anchor's set would leave the other artifact listing a deleted link, or missing a new one, for the rest of the
     * transaction, and the response is built in that same transaction.
     * <p>
     * An enum with one constant per end, rather than a {@code boolean outgoing} and an {@code if} in each step, keeps
     * the two directions side by side and lets the compiler check that each end gives all four answers. Rejected:
     * copying the algorithm into a second method with the ends swapped, which would leave two copies of it and of its
     * comments to keep in step.
     */
    private enum LinkEnd {
        /**
         * The anchor is the link's source: its outgoing links are revised, and the other end is the link's target.
         * Used by {@code reviseOutgoingLinks}, for example for a use case's PRIMARY_ACTOR and SECONDARY_ACTOR links
         * (UC-report -> STK-tutor).
         */
        SOURCE {
            /** UC-report's outgoing set, which holds UC-report -> STK-tutor along with UC-report's other outgoing links. */
            @Override
            Set<ArtifactLink> links(RequirementArtifact anchor) {
                return anchor.getOutgoingArtifactLinks();
            }

            /** For UC-report -> STK-tutor, STK-tutor: the actor. */
            @Override
            RequirementArtifact otherEnd(ArtifactLink link) {
                return link.getTargetArtifact();
            }

            /**
             * A new link from UC-report to STK-grader: it joins UC-report's outgoing set, which also sets the link's
             * source to UC-report, and STK-grader's incoming set, which sets its target to STK-grader.
             */
            @Override
            void connect(RequirementArtifact anchor, RequirementArtifact other, ArtifactLink link) {
                anchor.addOutgoingLink(link);
                other.addIncomingLink(link);
            }

            /**
             * The dropped link UC-report -> SYS-email: it leaves UC-report's outgoing set and SYS-email's incoming set,
             * and the remove helpers clear the link's source and target. The caller then deletes the row.
             */
            @Override
            void disconnect(RequirementArtifact anchor, RequirementArtifact other, ArtifactLink link) {
                anchor.removeOutgoingLink(link);
                other.removeIncomingLink(link);
            }
        },
        /**
         * The anchor is the link's target: its incoming links are revised, and the other end is the link's source.
         * Used by {@code reviseIncomingLinks}, for example for a use case's CONSTRAINS links from its business rules
         * (RULE-team -> UC-report).
         */
        TARGET {
            /**
             * UC-report's incoming set, which holds RULE-team -> UC-report along with UC-report's other incoming links,
             * such as FR-sign-in -> UC-report (DERIVES_FROM). A rule's link to any other artifact, such as
             * RULE-late -> FR-notify, is not in it.
             */
            @Override
            Set<ArtifactLink> links(RequirementArtifact anchor) {
                return anchor.getIncomingArtifactLinks();
            }

            /** For RULE-team -> UC-report, RULE-team: the business rule. */
            @Override
            RequirementArtifact otherEnd(ArtifactLink link) {
                return link.getSourceArtifact();
            }

            /**
             * A new link from RULE-due to UC-report: it joins RULE-due's outgoing set, which sets the link's source to
             * RULE-due, and UC-report's incoming set, which sets its target to UC-report. The direction of the link
             * never changes; only which of its two artifacts is the anchor.
             */
            @Override
            void connect(RequirementArtifact anchor, RequirementArtifact other, ArtifactLink link) {
                other.addOutgoingLink(link);
                anchor.addIncomingLink(link);
            }

            /**
             * The dropped link RULE-team -> UC-report: it leaves RULE-team's outgoing set and UC-report's incoming set.
             * RULE-team itself stays, with any links it has to other artifacts.
             */
            @Override
            void disconnect(RequirementArtifact anchor, RequirementArtifact other, ArtifactLink link) {
                other.removeOutgoingLink(link);
                anchor.removeIncomingLink(link);
            }
        };

        /**
         * The set of the anchor's links that this end revises: outgoing for {@code SOURCE}, incoming for
         * {@code TARGET}. {@link #reviseLinks} filters it by type and copies it before changing it.
         */
        abstract Set<ArtifactLink> links(RequirementArtifact anchor);

        /** The artifact at {@code link}'s other end from the anchor: the one the caller names in its wanted set. */
        abstract RequirementArtifact otherEnd(ArtifactLink link);

        /**
         * Adds the new {@code link} between {@code anchor} and {@code other} to both artifacts' in-memory sets, in the
         * direction this end implies; the add helpers also set the link's source and target.
         */
        abstract void connect(RequirementArtifact anchor, RequirementArtifact other, ArtifactLink link);

        /** Removes {@code link} from both artifacts' in-memory sets, before the caller deletes its row. */
        abstract void disconnect(RequirementArtifact anchor, RequirementArtifact other, ArtifactLink link);
    }

    /**
     * The one revise algorithm behind {@link #reviseOutgoingLinks} and {@link #reviseIncomingLinks}. {@code anchor} is
     * the artifact being revised, {@code end} says which end of its links it sits at, and {@code others} are the
     * artifacts the links must join it to.
     */
    private void reviseLinks(Integer teamId, RequirementArtifact anchor, LinkEnd end, ArtifactLinkType type, Set<RequirementArtifact> others) {
        // The comments below follow reviseOutgoingLinks's example: anchor UC-report at the SOURCE end, type
        // SECONDARY_ACTOR, current links to STK-tutor and SYS-email, others {STK-tutor, STK-grader}. For the TARGET
        // end, read "links to" as "links from".

        // Step 1: the ids the caller wants linked, here {STK-tutor, STK-grader}. Artifacts are compared by id, not by
        // object: ids are what a link stores, and they stay correct even if the caller loaded an artifact twice.
        Set<Long> wantedIds = others.stream().map(RequirementArtifact::getId).collect(Collectors.toSet());

        // Step 2: the anchor's links of this one type on this end, here
        // [UC-report -> STK-tutor, UC-report -> SYS-email]. UC-report's PRIMARY_ACTOR and DERIVES_FROM links are
        // filtered out, so they are never touched, and so are the links on its other end. The result is copied into a list because step 3 removes links from the same set, and
        // removing from a set while streaming over it would throw ConcurrentModificationException.
        List<ArtifactLink> current = end.links(anchor).stream()
                .filter(link -> link.getType() == type)
                .toList();

        // Step 3: keep or delete each current link.
        // keptIds collects the artifacts that already have a link to keep, here {STK-tutor}, so step 4 does not add a
        // second link to them.
        Set<Long> keptIds = new HashSet<>();
        for (ArtifactLink link : current) {
            RequirementArtifact other = end.otherEnd(link);
            // Keep the link when its other end is still wanted and is not linked yet: UC-report -> STK-tutor stays as
            // it is, with its id, notes and created-by. keptIds.add returns false when the artifact is already kept, so
            // a second link to the same artifact, which the link routes could have created (OI-77), is deleted below.
            if (wantedIds.contains(other.getId()) && keptIds.add(other.getId())) {
                continue;
            }
            // Delete the link: UC-report -> SYS-email is no longer wanted. Take it out of both in-memory sets first, so
            // that in the rest of this transaction neither UC-report nor SYS-email still lists it (the response is
            // built from UC-report's sets). Then delete the row. The artifacts themselves stay; only the link goes.
            end.disconnect(anchor, other, link);
            this.artifactLinkRepository.delete(link);
        }

        // Step 4: add a link to each wanted artifact that has none yet, here STK-grader.
        // The team is loaded only when at least one link is added, so a save that changes no link costs no query.
        Team team = null;
        for (RequirementArtifact other : others) {
            // STK-tutor already has its kept link from step 3.
            if (keptIds.contains(other.getId())) {
                continue;
            }
            if (team == null) {
                team = this.teamRepository.findById(teamId)
                        .orElseThrow(() -> new ObjectNotFoundException("team", teamId));
            }
            // A new link, UC-report -> STK-grader, with the route's team. Notes stay empty: a student adds them, if at
            // all, through the link routes. JPA auditing stamps createdAt and createdBy on save.
            ArtifactLink link = new ArtifactLink();
            link.setTeam(team);
            link.setType(type);
            // Add it to both in-memory sets, as step 3 removes from both, so UC-report and STK-grader list it at once.
            end.connect(anchor, other, link);
            this.artifactLinkRepository.save(link);
        }
        // Result for the example: UC-report's SECONDARY_ACTOR links are now to STK-tutor (the same row as before) and
        // STK-grader (new); the SYS-email link is gone; UC-report's other links are unchanged.
    }

    public List<ArtifactLink> getOutgoingLinksByRequirementArtifactId(Integer teamId, Long requirementArtifactId) {
        return this.artifactLinkRepository.findBySourceArtifactId(requirementArtifactId, teamId);
    }

    public List<ArtifactLink> getIncomingLinksByRequirementArtifactId(Integer teamId, Long requirementArtifactId) {
        return this.artifactLinkRepository.findByTargetArtifactId(requirementArtifactId, teamId);
    }

}
