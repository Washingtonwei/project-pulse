package team.projectpulse.ram.requirement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.projectpulse.ram.document.DocumentRepository;
import team.projectpulse.ram.document.DocumentSection;
import team.projectpulse.ram.document.DocumentSectionRepository;
import team.projectpulse.ram.document.DocumentType;
import team.projectpulse.ram.document.RequirementDocument;
import team.projectpulse.ram.document.template.DocumentTemplateRegistry;
import team.projectpulse.ram.document.template.DocumentTemplateRegistry.Home;
import team.projectpulse.team.Team;
import team.projectpulse.team.TeamRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * The one way to create a requirement artifact (OI-73): a type with no list home is only keyed and inserted, and a list
 * type is placed in its home's list under the row lock. Whichever service calls it, a list type cannot end up with no
 * home.
 */
@ExtendWith(MockitoExtension.class)
class RequirementArtifactServiceTest {

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

    @InjectMocks
    RequirementArtifactService requirementArtifactService;

    Team team;

    @BeforeEach
    void setUp() {
        this.team = new Team("Team1", "Team 1 description", "https://www.team1.com");
        given(this.teamRepository.findById(1)).willReturn(Optional.of(this.team));
        given(this.artifactKeySequenceRepository.findForUpdate(any(), any())).willReturn(Optional.empty());
        given(this.artifactKeySequenceRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(this.requirementArtifactRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createRequirementArtifact_NoListHome_KeyedAndInsertedWithoutTouchingAnyDocument() {
        RequirementArtifact term = new RequirementArtifact(null, RequirementArtifactType.GLOSSARY_TERM, "Artifact", "A traceable requirement item.", "");
        term.setSourceDocumentSection(new DocumentSection()); // a stray section reference is cleared, not kept

        RequirementArtifact saved = this.requirementArtifactService.createRequirementArtifact(1, term);

        assertThat(saved.getTeam()).isSameAs(this.team);
        assertThat(saved.getArtifactKey()).isEqualTo("GLO-1");
        assertThat(saved.getSourceDocumentSection()).isNull();
        verifyNoInteractions(this.documentRepository, this.documentSectionRepository, this.documentTemplateRegistry);
    }

    @Test
    void createRequirementArtifact_ListType_AppendedToItsHomeUnderTheRowLock() {
        RequirementDocument visionScope = new RequirementDocument();
        visionScope.setId(2L);
        visionScope.setType(DocumentType.VISION_SCOPE);
        DocumentSection stakeholders = new DocumentSection();
        stakeholders.setId(40L);
        given(this.documentTemplateRegistry.homeOf(RequirementArtifactType.STAKEHOLDER))
                .willReturn(Optional.of(new Home(DocumentType.VISION_SCOPE, "STAKEHOLDER_PROFILES")));
        given(this.documentRepository.findByTeamTeamIdAndType(1, DocumentType.VISION_SCOPE)).willReturn(Optional.of(visionScope));
        given(this.documentSectionRepository.findByDocumentIdAndSectionKey(2L, "STAKEHOLDER_PROFILES")).willReturn(Optional.of(stakeholders));
        given(this.documentSectionRepository.findByIdForUpdate(40L)).willReturn(Optional.of(stakeholders));
        RequirementArtifact stakeholder = new RequirementArtifact(null, RequirementArtifactType.STAKEHOLDER, "Student", "A student on the team.", "");

        RequirementArtifact saved = this.requirementArtifactService.createRequirementArtifact(1, stakeholder);

        assertThat(saved.getArtifactKey()).isEqualTo("STK-1");
        assertThat(saved.getSourceDocumentSection()).isSameAs(stakeholders);
        assertThat(stakeholders.getRequirementArtifacts()).containsExactly(saved);
        verify(this.documentSectionRepository).findByIdForUpdate(40L);
    }

}
