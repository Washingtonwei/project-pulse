package team.projectpulse.ram.requirement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.projectpulse.ram.document.DocumentRepository;
import team.projectpulse.ram.document.DocumentSectionRepository;
import team.projectpulse.ram.document.template.DocumentTemplateRegistry;
import team.projectpulse.ram.requirement.dto.ArtifactReferrerDto;
import team.projectpulse.team.TeamRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * The order of an artifact's referrers (UC-ART-delete-artifact step 4, 4a): every reference is a link, a use case's
 * actors included, listed in artifact key order (alphabetical), so UC-create-use-case comes before UC-submit-report.
 */
@ExtendWith(MockitoExtension.class)
class RequirementArtifactReferencesTest {

    @Mock
    RequirementArtifactRepository requirementArtifactRepository;

    @Mock
    TeamRepository teamRepository;


    @Mock
    DocumentRepository documentRepository;

    @Mock
    DocumentSectionRepository documentSectionRepository;

    @Mock
    DocumentTemplateRegistry documentTemplateRegistry;

    @Mock
    ArtifactLinkRepository artifactLinkRepository;

    @InjectMocks
    RequirementArtifactService requirementArtifactService;

    @Test
    void findReferences_LinksAndActorLinks_InKeyOrder() {
        RequirementArtifact stk1 = artifact(9L, "STK-student");
        RequirementArtifact ucSubmit = artifact(30L, "UC-submit-report");
        RequirementArtifact ucCreate = artifact(22L, "UC-create-use-case");
        given(this.requirementArtifactRepository.findByIdAndTeamTeamId(9L, 1)).willReturn(Optional.of(stk1));
        given(this.artifactLinkRepository.findBySourceArtifactId(9L, 1)).willReturn(List.of(link(stk1, ucSubmit, ArtifactLinkType.MOTIVATES)));
        given(this.artifactLinkRepository.findByTargetArtifactId(9L, 1)).willReturn(List.of(
                link(ucSubmit, stk1, ArtifactLinkType.PRIMARY_ACTOR), link(ucCreate, stk1, ArtifactLinkType.SECONDARY_ACTOR)));

        List<ArtifactReferrerDto> referrers = this.requirementArtifactService.findReferences(1, 9L);

        // Sorted by key, and a stable sort keeps the source link ahead of the target link for the same key.
        assertThat(referrers).extracting(ArtifactReferrerDto::artifactKey).containsExactly("UC-create-use-case", "UC-submit-report", "UC-submit-report");
        assertThat(referrers).extracting(ArtifactReferrerDto::relation).containsExactly(
                ArtifactReferrerDto.Relation.LINK_TO, ArtifactReferrerDto.Relation.LINK_FROM, ArtifactReferrerDto.Relation.LINK_TO);
        assertThat(referrers).extracting(ArtifactReferrerDto::linkType).containsExactly(
                ArtifactLinkType.SECONDARY_ACTOR, ArtifactLinkType.MOTIVATES, ArtifactLinkType.PRIMARY_ACTOR);
    }

    @Test
    void findReferences_SelfLink_ListedOnceAsLinkFrom() {
        RequirementArtifact fr1 = artifact(9L, "FR-export-csv");
        ArtifactLink self = link(fr1, fr1, ArtifactLinkType.DERIVES_FROM);
        given(this.requirementArtifactRepository.findByIdAndTeamTeamId(9L, 1)).willReturn(Optional.of(fr1));
        given(this.artifactLinkRepository.findBySourceArtifactId(9L, 1)).willReturn(List.of(self));
        given(this.artifactLinkRepository.findByTargetArtifactId(9L, 1)).willReturn(List.of(self));

        List<ArtifactReferrerDto> referrers = this.requirementArtifactService.findReferences(1, 9L);

        assertThat(referrers).singleElement().satisfies(referrer -> {
            assertThat(referrer.artifactKey()).isEqualTo("FR-export-csv");
            assertThat(referrer.relation()).isEqualTo(ArtifactReferrerDto.Relation.LINK_FROM);
        });
    }

    private static RequirementArtifact artifact(Long id, String key) {
        RequirementArtifact artifact = new RequirementArtifact();
        artifact.setId(id);
        artifact.setArtifactKey(key);
        artifact.setTitle(key + " title");
        return artifact;
    }

    private static ArtifactLink link(RequirementArtifact source, RequirementArtifact target, ArtifactLinkType type) {
        ArtifactLink link = new ArtifactLink();
        link.setSourceArtifact(source);
        link.setTargetArtifact(target);
        link.setType(type);
        return link;
    }

}
