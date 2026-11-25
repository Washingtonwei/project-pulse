package team.projectpulse.ram.requirement;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import team.projectpulse.ram.document.DocumentRepository;
import team.projectpulse.ram.document.DocumentSection;
import team.projectpulse.ram.document.DocumentSectionRepository;
import team.projectpulse.ram.document.DocumentType;
import team.projectpulse.ram.document.RequirementDocument;
import team.projectpulse.ram.document.template.DocumentTemplateRegistry;
import team.projectpulse.ram.document.template.DocumentTemplateRegistry.Home;
import team.projectpulse.ram.requirement.dto.ArtifactKeyHolderDto;
import team.projectpulse.system.exception.RequirementArtifactRejectedException;
import team.projectpulse.team.Team;
import team.projectpulse.team.TeamRepository;

import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * The one way to create a requirement artifact (OI-73): a type with no list home is only checked and inserted, and a
 * list type is placed in its home's list under the row lock. Whichever service calls it, a list type cannot end up with
 * no home. Every create and every key rename checks the student's key (design: art.md, "Artifact keys"): its format
 * and prefix, then its holder, with the unique key index behind both. A rolled-back integration test can never reach
 * that index past the check, so the index's refusal is turned into the friendly one here, with mocks.
 */
@ExtendWith(MockitoExtension.class)
class RequirementArtifactServiceTest {

    @Mock
    RequirementArtifactRepository requirementArtifactRepository;

    @Mock
    TeamRepository teamRepository;

    @Mock
    ArtifactKeyHolderLookup artifactKeyHolderLookup;

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
        lenient().when(this.teamRepository.findById(1)).thenReturn(Optional.of(this.team));
        lenient().when(this.requirementArtifactRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createRequirementArtifact_NoListHome_CheckedAndInsertedWithoutTouchingAnyDocument() {
        RequirementArtifact term = glossaryTerm("GLO-artifact");
        term.setSourceDocumentSection(new DocumentSection()); // a stray section reference is cleared, not kept

        RequirementArtifact saved = this.requirementArtifactService.createRequirementArtifact(1, term);

        assertThat(saved.getTeam()).isSameAs(this.team);
        assertThat(saved.getArtifactKey()).isEqualTo("GLO-artifact");
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
        stakeholder.setArtifactKey("STK-student");

        RequirementArtifact saved = this.requirementArtifactService.createRequirementArtifact(1, stakeholder);

        assertThat(saved.getArtifactKey()).isEqualTo("STK-student");
        assertThat(saved.getSourceDocumentSection()).isSameAs(stakeholders);
        assertThat(stakeholders.getRequirementArtifacts()).containsExactly(saved);
        verify(this.documentSectionRepository).findByIdForUpdate(40L);
    }

    @Test
    void createRequirementArtifact_Extension4a_MalformedKeyRefusedBeforeAnyRead() {
        assertThatThrownBy(() -> this.requirementArtifactService.createRequirementArtifact(1, glossaryTerm("GLO-1")))
                .isInstanceOf(RequirementArtifactRejectedException.class)
                .hasMessageStartingWith("The artifact key must be GLO- followed by");
        verifyNoInteractions(this.teamRepository);
        verify(this.requirementArtifactRepository, never()).saveAndFlush(any());
    }

    @Test
    void createRequirementArtifact_Extension4b_HeldKeyRefusedNamingTheHolder() {
        RequirementArtifact holder = glossaryTerm("GLO-artifact");
        holder.setId(5L);
        given(this.requirementArtifactRepository.findByTeamTeamIdAndArtifactKey(1, "GLO-artifact")).willReturn(Optional.of(holder));

        assertThatThrownBy(() -> this.requirementArtifactService.createRequirementArtifact(1, glossaryTerm("GLO-artifact")))
                .isInstanceOf(RequirementArtifactRejectedException.class)
                .hasMessage("GLO-artifact is already used by GLO-artifact: Artifact.")
                .extracting(exception -> ((RequirementArtifactRejectedException) exception).getData())
                .isEqualTo(new ArtifactKeyHolderDto(5L, "GLO-artifact", "Artifact", RequirementArtifactType.GLOSSARY_TERM));
        verify(this.requirementArtifactRepository, never()).saveAndFlush(any());
    }

    @Test
    void createRequirementArtifact_Extension8c_IndexViolationBecomesTheSameRefusal() {
        // The check found the key free, but a teammate committed it before this insert: the unique index refuses it.
        given(this.requirementArtifactRepository.saveAndFlush(any())).willThrow(keyIndexViolation());
        ArtifactKeyHolderDto teammates = new ArtifactKeyHolderDto(77L, "GLO-sprint", "Sprint", RequirementArtifactType.GLOSSARY_TERM);
        given(this.artifactKeyHolderLookup.holderOf(1, "GLO-sprint")).willReturn(Optional.of(teammates));

        assertThatThrownBy(() -> this.requirementArtifactService.createRequirementArtifact(1, glossaryTerm("GLO-sprint")))
                .isInstanceOf(RequirementArtifactRejectedException.class)
                .hasMessage("GLO-sprint is already used by GLO-sprint: Sprint.")
                .extracting(exception -> ((RequirementArtifactRejectedException) exception).getData())
                .isEqualTo(teammates);
    }

    @Test
    void createRequirementArtifact_OtherIntegrityError_NotMistakenForAKeyClash() {
        DataIntegrityViolationException foreignKey = new DataIntegrityViolationException("fk",
                new ConstraintViolationException("fk", new SQLException("fk"), "FKipwvw342vl86gt02me9k0vsse"));
        given(this.requirementArtifactRepository.saveAndFlush(any())).willThrow(foreignKey);

        assertThatThrownBy(() -> this.requirementArtifactService.createRequirementArtifact(1, glossaryTerm("GLO-sprint")))
                .isSameAs(foreignKey);
        verifyNoInteractions(this.artifactKeyHolderLookup);
    }

    @Test
    void renameKey_UnchangedKey_NothingCheckedOrWritten() {
        RequirementArtifact term = glossaryTerm("GLO-artifact");

        assertThat(this.requirementArtifactService.renameKey(1, term, "GLO-artifact")).isSameAs(term);
        verifyNoInteractions(this.requirementArtifactRepository);
    }

    @Test
    void renameKey_FreeNewKey_StoredAndFlushed() {
        RequirementArtifact term = glossaryTerm("GLO-artifact");
        term.setId(5L);

        RequirementArtifact renamed = this.requirementArtifactService.renameKey(1, term, "GLO-requirement-artifact");

        assertThat(renamed.getArtifactKey()).isEqualTo("GLO-requirement-artifact");
        verify(this.requirementArtifactRepository).saveAndFlush(term);
    }

    @Test
    void renameKey_KeyHeldByAnother_RefusedAndUnchanged() {
        RequirementArtifact term = glossaryTerm("GLO-artifact");
        term.setId(5L);
        RequirementArtifact other = glossaryTerm("GLO-sprint");
        other.setId(6L);
        given(this.requirementArtifactRepository.findByTeamTeamIdAndArtifactKey(1, "GLO-sprint")).willReturn(Optional.of(other));

        assertThatThrownBy(() -> this.requirementArtifactService.renameKey(1, term, "GLO-sprint"))
                .isInstanceOf(RequirementArtifactRejectedException.class);
        assertThat(term.getArtifactKey()).isEqualTo("GLO-artifact");
    }

    private static RequirementArtifact glossaryTerm(String artifactKey) {
        RequirementArtifact term = new RequirementArtifact(null, RequirementArtifactType.GLOSSARY_TERM, "Artifact", "A traceable requirement item.", "");
        term.setArtifactKey(artifactKey);
        return term;
    }

    // What Hibernate and Spring raise when an insert breaks the key index on MySQL, which names it after its table.
    private static DataIntegrityViolationException keyIndexViolation() {
        return new DataIntegrityViolationException("duplicate", new ConstraintViolationException("Duplicate entry",
                new SQLException("Duplicate entry '1-GLO-sprint'"), "requirement_artifact." + ArtifactKeys.KEY_INDEX));
    }

}
