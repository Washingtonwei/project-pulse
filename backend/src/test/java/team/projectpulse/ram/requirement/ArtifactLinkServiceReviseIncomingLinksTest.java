package team.projectpulse.ram.requirement;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import team.projectpulse.AbstractIntegrationTest;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * {@link ArtifactLinkService#reviseIncomingLinks}, the way a use case writes its business rule links (docs/design/doc.md,
 * "Business rules as links"). Team 1's seed: UC-1 (artifact 3) is the target of DERIVES_FROM links from FR-1 and FR-2,
 * and the source of its two actor links and a DERIVES_FROM link to BO-1; RULE-1 is the team's one business rule and
 * has no links.
 */
class ArtifactLinkServiceReviseIncomingLinksTest extends AbstractIntegrationTest {

    @Autowired
    ArtifactLinkService artifactLinkService;

    @Autowired
    RequirementArtifactRepository requirementArtifactRepository;

    @Autowired
    ArtifactLinkRepository artifactLinkRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    void reviseIncomingLinks_ChangesOnlyThatTypeToThatTarget_AndKeepsTheInMemorySetsInStep() {
        RequirementArtifact useCase = this.requirementArtifactRepository.findByIdAndTeamTeamId(3L, 1).orElseThrow();
        RequirementArtifact rule = firstOfType(RequirementArtifactType.BUSINESS_RULE);
        long outgoingBefore = this.artifactLinkRepository.findBySourceArtifactId(3L, 1).size();

        this.artifactLinkService.reviseIncomingLinks(1, useCase, ArtifactLinkType.CONSTRAINS, Set.of(rule));

        // In memory, in the same transaction: both ends already show the new link, and UC-1's DERIVES_FROM links
        // from FR-1 and FR-2 are still there.
        assertThat(useCase.getIncomingArtifactLinks()).extracting(ArtifactLink::getType)
                .containsExactlyInAnyOrder(ArtifactLinkType.DERIVES_FROM, ArtifactLinkType.DERIVES_FROM, ArtifactLinkType.CONSTRAINS);
        assertThat(rule.getOutgoingArtifactLinks()).extracting(link -> link.getType(), link -> link.getTargetArtifact().getId())
                .containsExactly(tuple(ArtifactLinkType.CONSTRAINS, 3L));

        // In the database: the same, with the route's team, and UC-1's own outgoing links untouched.
        this.entityManager.flush();
        this.entityManager.clear();
        assertThat(this.artifactLinkRepository.findByTargetArtifactId(3L, 1))
                .extracting(link -> link.getType(), link -> link.getTeam().getTeamId())
                .containsExactlyInAnyOrder(tuple(ArtifactLinkType.DERIVES_FROM, 1), tuple(ArtifactLinkType.DERIVES_FROM, 1), tuple(ArtifactLinkType.CONSTRAINS, 1));
        assertThat(this.artifactLinkRepository.findBySourceArtifactId(3L, 1)).hasSize((int) outgoingBefore);
    }

    @Test
    void reviseIncomingLinks_EmptySources_RemovesThatTypeOnly() {
        RequirementArtifact useCase = this.requirementArtifactRepository.findByIdAndTeamTeamId(3L, 1).orElseThrow();
        RequirementArtifact rule = firstOfType(RequirementArtifactType.BUSINESS_RULE);
        this.artifactLinkService.reviseIncomingLinks(1, useCase, ArtifactLinkType.CONSTRAINS, Set.of(rule));
        this.entityManager.flush();
        this.entityManager.clear();

        RequirementArtifact reloaded = this.requirementArtifactRepository.findByIdAndTeamTeamId(3L, 1).orElseThrow();
        this.artifactLinkService.reviseIncomingLinks(1, reloaded, ArtifactLinkType.CONSTRAINS, Set.of());

        this.entityManager.flush();
        this.entityManager.clear();
        assertThat(this.artifactLinkRepository.findByTargetArtifactId(3L, 1)).extracting(ArtifactLink::getType)
                .containsExactlyInAnyOrder(ArtifactLinkType.DERIVES_FROM, ArtifactLinkType.DERIVES_FROM);
        assertThat(this.requirementArtifactRepository.findById(rule.getId())).isPresent();
    }

    private RequirementArtifact firstOfType(RequirementArtifactType type) {
        return this.requirementArtifactRepository.findAll().stream()
                .filter(artifact -> artifact.getType() == type && artifact.getTeam().getTeamId().equals(1))
                .findFirst().orElseThrow();
    }
}
