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
import team.projectpulse.ram.usecase.UseCaseRepository;
import team.projectpulse.team.TeamRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * The order of an artifact's referrers (UC-ART-delete-artifact step 4, 4a): links before actor references, each group
 * in artifact-key order as a student reads it, so UC-2 comes before UC-10.
 */
@ExtendWith(MockitoExtension.class)
class RequirementArtifactReferencesTest {

    @Mock
    RequirementArtifactRepository requirementArtifactRepository;

    @Mock
    TeamRepository teamRepository;

    @Mock
    ArtifactKeySequenceRepository artifactKeySequenceRepository;

    @Mock
    DocumentRepository documentRepository;

    @Mock
    DocumentSectionRepository documentSectionRepository;

    @Mock
    DocumentTemplateRegistry documentTemplateRegistry;

    @Mock
    ArtifactLinkRepository artifactLinkRepository;

    @Mock
    UseCaseRepository useCaseRepository;

    @InjectMocks
    RequirementArtifactService requirementArtifactService;

    @Test
    void findReferences_LinksThenActors_EachInKeyOrderByNumber() {
        RequirementArtifact fr1 = artifact(9L, "FR-1");
        RequirementArtifact uc10 = artifact(30L, "UC-10");
        RequirementArtifact uc2 = artifact(22L, "UC-2");
        given(this.requirementArtifactRepository.findByIdAndTeamTeamId(9L, 1)).willReturn(Optional.of(fr1));
        given(this.artifactLinkRepository.findBySourceArtifactId(9L, 1)).willReturn(List.of(link(fr1, uc10), link(fr1, uc2)));
        given(this.artifactLinkRepository.findByTargetArtifactId(9L, 1)).willReturn(List.of());
        given(this.useCaseRepository.findUseCaseArtifactsNamingPrimaryActor(9L, 1)).willReturn(List.of(uc10));
        given(this.useCaseRepository.findUseCaseArtifactsNamingSecondaryActor(9L, 1)).willReturn(List.of(uc2));

        List<ArtifactReferrerDto> referrers = this.requirementArtifactService.findReferences(1, 9L);

        assertThat(referrers).extracting(ArtifactReferrerDto::artifactKey).containsExactly("UC-2", "UC-10", "UC-2", "UC-10");
        assertThat(referrers).extracting(ArtifactReferrerDto::relation).containsExactly(
                ArtifactReferrerDto.Relation.LINK_FROM, ArtifactReferrerDto.Relation.LINK_FROM,
                ArtifactReferrerDto.Relation.SECONDARY_ACTOR, ArtifactReferrerDto.Relation.PRIMARY_ACTOR);
    }

    @Test
    void findReferences_SelfLink_ListedOnceAsLinkFrom() {
        RequirementArtifact fr1 = artifact(9L, "FR-1");
        ArtifactLink self = link(fr1, fr1);
        given(this.requirementArtifactRepository.findByIdAndTeamTeamId(9L, 1)).willReturn(Optional.of(fr1));
        given(this.artifactLinkRepository.findBySourceArtifactId(9L, 1)).willReturn(List.of(self));
        given(this.artifactLinkRepository.findByTargetArtifactId(9L, 1)).willReturn(List.of(self));
        given(this.useCaseRepository.findUseCaseArtifactsNamingPrimaryActor(9L, 1)).willReturn(List.of());
        given(this.useCaseRepository.findUseCaseArtifactsNamingSecondaryActor(9L, 1)).willReturn(List.of());

        List<ArtifactReferrerDto> referrers = this.requirementArtifactService.findReferences(1, 9L);

        assertThat(referrers).singleElement().satisfies(referrer -> {
            assertThat(referrer.artifactKey()).isEqualTo("FR-1");
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

    private static ArtifactLink link(RequirementArtifact source, RequirementArtifact target) {
        ArtifactLink link = new ArtifactLink();
        link.setSourceArtifact(source);
        link.setTargetArtifact(target);
        link.setType(ArtifactLinkType.DERIVES_FROM);
        return link;
    }

}
