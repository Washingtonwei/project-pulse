package team.projectpulse.ram.requirement;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import team.projectpulse.AbstractIntegrationTest;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * {@link ArtifactLinkService#replaceOutgoingLinks}, the way a use case writes its actor links (docs/design/doc.md,
 * "Actors as links"). Team 1's seed: UC-1 (artifact 3) links STK-1 (1) as PRIMARY_ACTOR, STK-2 (2) as SECONDARY_ACTOR,
 * and BO-1 with DERIVES_FROM; FR-1 and FR-2 link to UC-1 with DERIVES_FROM.
 */
class ArtifactLinkServiceReplaceOutgoingLinksTest extends AbstractIntegrationTest {

    @Autowired
    ArtifactLinkService artifactLinkService;

    @Autowired
    RequirementArtifactRepository requirementArtifactRepository;

    @Autowired
    ArtifactLinkRepository artifactLinkRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    void replaceOutgoingLinks_ChangesOnlyThatTypeFromThatSource_AndKeepsTheInMemorySetsInStep() {
        RequirementArtifact useCase = this.requirementArtifactRepository.findByIdAndTeamTeamId(3L, 1).orElseThrow();
        RequirementArtifact stk1 = this.requirementArtifactRepository.findByIdAndTeamTeamId(1L, 1).orElseThrow();
        RequirementArtifact stk2 = this.requirementArtifactRepository.findByIdAndTeamTeamId(2L, 1).orElseThrow();
        long otherLinksBefore = this.artifactLinkRepository.findAll().stream()
                .filter(link -> !link.getSourceArtifact().getId().equals(3L)).count();

        // STK-2 is dropped as a secondary actor and STK-1 added.
        this.artifactLinkService.replaceOutgoingLinks(1, useCase, ArtifactLinkType.SECONDARY_ACTOR, Set.of(stk1));

        // In memory, in the same transaction: the source and both targets already show the change.
        assertThat(useCase.getOutgoingArtifactLinks()).extracting(link -> link.getType(), link -> link.getTargetArtifact().getId())
                .containsExactlyInAnyOrder(tuple(ArtifactLinkType.PRIMARY_ACTOR, 1L), tuple(ArtifactLinkType.SECONDARY_ACTOR, 1L),
                        tuple(ArtifactLinkType.DERIVES_FROM, useCase.getOutgoingArtifactLinks().stream()
                                .filter(link -> link.getType() == ArtifactLinkType.DERIVES_FROM).findFirst().orElseThrow().getTargetArtifact().getId()));
        assertThat(stk2.getIncomingArtifactLinks()).isEmpty();
        assertThat(stk1.getIncomingArtifactLinks()).extracting(ArtifactLink::getType)
                .containsExactlyInAnyOrder(ArtifactLinkType.PRIMARY_ACTOR, ArtifactLinkType.SECONDARY_ACTOR);

        // In the database: the same, and no link from any other source changed.
        this.entityManager.flush();
        this.entityManager.clear();
        List<ArtifactLink> stored = this.artifactLinkRepository.findBySourceArtifactId(3L, 1);
        assertThat(stored).extracting(link -> link.getType(), link -> link.getTargetArtifact().getId())
                .contains(tuple(ArtifactLinkType.PRIMARY_ACTOR, 1L), tuple(ArtifactLinkType.SECONDARY_ACTOR, 1L))
                .doesNotContain(tuple(ArtifactLinkType.SECONDARY_ACTOR, 2L))
                .hasSize(3);
        assertThat(this.artifactLinkRepository.findByTargetArtifactId(2L, 1)).isEmpty();
        assertThat(this.artifactLinkRepository.findAll().stream()
                .filter(link -> !link.getSourceArtifact().getId().equals(3L)).count()).isEqualTo(otherLinksBefore);
    }

    @Test
    void replaceOutgoingLinks_EmptyTargets_RemovesThatTypeOnly() {
        RequirementArtifact useCase = this.requirementArtifactRepository.findByIdAndTeamTeamId(3L, 1).orElseThrow();

        this.artifactLinkService.replaceOutgoingLinks(1, useCase, ArtifactLinkType.SECONDARY_ACTOR, Set.of());

        this.entityManager.flush();
        this.entityManager.clear();
        assertThat(this.artifactLinkRepository.findBySourceArtifactId(3L, 1)).extracting(ArtifactLink::getType)
                .containsExactlyInAnyOrder(ArtifactLinkType.PRIMARY_ACTOR, ArtifactLinkType.DERIVES_FROM);
    }

}
