package team.projectpulse.seed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import team.projectpulse.AbstractIntegrationTest;
import team.projectpulse.ram.document.DocumentRepository;
import team.projectpulse.ram.document.DocumentSection;
import team.projectpulse.ram.document.DocumentType;
import team.projectpulse.ram.document.RequirementDocument;
import team.projectpulse.ram.document.template.DocumentTemplateRegistry;
import team.projectpulse.ram.document.template.DocumentTemplateRegistry.Home;
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.requirement.RequirementArtifactRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The seed is what every integration test starts from, so it must be data the spec allows (OI-73). Every seeded
 * artifact of a type with a list home is an item of exactly that home (BR-artifact-home), and every list loads with no
 * gaps: a gap in {@code artifact_index} would load as a null item.
 */
@DisplayName("Integration tests for the dev seed data")
class SeedDataIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    RequirementArtifactRepository requirementArtifactRepository;

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    DocumentTemplateRegistry documentTemplateRegistry;

    @Test
    void everySeededListTypeArtifact_SitsInItsHomeList() {
        List<RequirementArtifact> listTypes = this.requirementArtifactRepository.findAll().stream()
                .filter(artifact -> artifact.getType().hasListHome())
                .toList();
        assertThat(listTypes).isNotEmpty();

        for (RequirementArtifact artifact : listTypes) {
            DocumentSection section = artifact.getSourceDocumentSection();
            assertThat(section).as(artifact.getArtifactKey() + " has a home").isNotNull();
            Home home = this.documentTemplateRegistry.homeOf(artifact.getType()).orElseThrow();
            assertThat(section.getDocument().getType()).as(artifact.getArtifactKey()).isEqualTo(home.documentType());
            assertThat(section.getSectionKey()).as(artifact.getArtifactKey()).isEqualTo(home.sectionKey());
            assertThat(section.getDocument().getTeam().getTeamId()).as(artifact.getArtifactKey()).isEqualTo(artifact.getTeam().getTeamId());
            assertThat(section.getRequirementArtifacts()).as(artifact.getArtifactKey() + " is in its list").contains(artifact);
        }
    }

    @Test
    void everySeededList_HasNoGaps() {
        for (RequirementDocument document : this.documentRepository.findAll()) {
            for (DocumentSection section : document.getSections()) {
                assertThat(section.getRequirementArtifacts()).as(document.getTitle() + " / " + section.getSectionKey()).doesNotContainNull();
            }
        }
    }

    @Test
    void seededDocuments_KeepTheIdsTheTestsUse() {
        assertDocument(1L, 1, DocumentType.SRS);
        assertDocument(2L, 1, DocumentType.VISION_SCOPE);
        assertDocument(3L, 2, DocumentType.VISION_SCOPE);
        assertDocument(4L, 2, DocumentType.SRS);
    }

    private void assertDocument(Long id, Integer teamId, DocumentType type) {
        RequirementDocument document = this.documentRepository.findById(id).orElseThrow();
        assertThat(document.getTeam().getTeamId()).as("document " + id).isEqualTo(teamId);
        assertThat(document.getType()).as("document " + id).isEqualTo(type);
    }

}
