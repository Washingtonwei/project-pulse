package team.projectpulse.ram.glossary;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import team.projectpulse.AbstractIntegrationTest;
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.requirement.RequirementArtifactRepository;
import team.projectpulse.ram.requirement.RequirementArtifactType;
import team.projectpulse.system.exception.RequirementArtifactRejectedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The glossary route names the artifact type, so the request body must not get a say in it; the artifact key is the
 * student's, so the body names it, and the create path checks it (UC-GLO-create-term, BR-artifact-key-unique).
 *
 * <p>{@code POST /teams/{teamId}/glossary-terms} creates a glossary term and nothing else. Its type is fixed by
 * the route. Its key is the one the body carries, checked by {@code RequirementArtifactService.createRequirementArtifact}
 * exactly as for the generic artifact route: {@code GLO-} and a slug, held by no other artifact of the team. A body
 * that names a different type is submitting a value the server owns; a body with a malformed key, or another type's
 * prefix, is refused rather than corrected, since the system never renames a key on the student's behalf.
 *
 * <p>Nothing here is a cross-team reach: the team is taken from the path. What a forged type corrupts is the caller's
 * own glossary. A {@code USE_CASE} row created through the glossary route would be invisible to
 * {@code GlossaryService}, which is type-scoped, but visible to the use-case listings.
 *
 * <p><strong>Why these probes call the service rather than the endpoint.</strong> They were written on 2026-09-08,
 * when the four glossary routes matched no rule in {@code SecurityConfiguration} and so fell to the API catch-all
 * {@code denyAll()} (OI-47), which refused them for every caller. A MockMvc probe would have been answered
 * {@code 403} by the filter chain and would have proved nothing about what the service does with the body. The
 * rules landed on 2026-09-09 (see {@code GlossaryControllerTest}), so that reason has expired: these can now be
 * promoted to full-stack probes in {@code RequestBodyIdIntegrationTest}, where the equivalents for the other
 * create paths live. Until they are, they still cover the service, which is where the type and key are decided.
 */
@DisplayName("The glossary create path owns the artifact type and key, not the request body")
public class GlossaryTermCreationIntegrationTest extends AbstractIntegrationTest {

    private static final Integer TEAM_1 = 1;

    @Autowired
    GlossaryService glossaryService;

    @Autowired
    RequirementArtifactRepository requirementArtifactRepository;

    @Autowired
    EntityManager entityManager;


    /**
     * Builds the artifact the converter would hand the service for a given request body. The converter maps type
     * and artifact key straight through, so whatever the body says arrives here.
     */
    private RequirementArtifact submitted(RequirementArtifactType type, String artifactKey, String title) {
        RequirementArtifact glossaryTerm = new RequirementArtifact();
        glossaryTerm.setType(type);
        glossaryTerm.setArtifactKey(artifactKey);
        glossaryTerm.setTitle(title);
        glossaryTerm.setContent("A definition.");
        return glossaryTerm;
    }

    /** Re-reads a row straight from the database, past the persistence context this test shares with the service. */
    private RequirementArtifact reread(Long id) {
        this.entityManager.flush();
        this.entityManager.clear();
        return this.requirementArtifactRepository.findById(id).orElseThrow();
    }

    @Test
    @DisplayName("A body naming another artifact type still creates a glossary term")
    void testCreateWithForeignTypeIsStoredAsGlossaryTerm() {
        RequirementArtifact saved = this.glossaryService.saveGlossaryTerm(
                TEAM_1, submitted(RequirementArtifactType.USE_CASE, "GLO-forged-type", "Forged type"));

        RequirementArtifact stored = reread(saved.getId());
        assertThat(stored.getType()).isEqualTo(RequirementArtifactType.GLOSSARY_TERM);
        assertThat(stored.getTeam().getTeamId()).isEqualTo(TEAM_1);
    }

    @Test
    @DisplayName("The key the body carries is the term's key (UC-GLO-create-term)")
    void testCreateKeepsTheStudentsKey() {
        RequirementArtifact saved = this.glossaryService.saveGlossaryTerm(
                TEAM_1, submitted(RequirementArtifactType.GLOSSARY_TERM, "GLO-peer-evaluation", "Peer evaluation"));

        assertThat(reread(saved.getId()).getArtifactKey()).isEqualTo("GLO-peer-evaluation");
    }

    @Test
    @DisplayName("A missing key, a malformed one, or another type's prefix is refused, never corrected (4a)")
    void testCreateWithAMissingOrMalformedKeyIsRefused() {
        long before = this.requirementArtifactRepository.count();
        for (String key : new String[]{null, "GLO-Peer", "GLO-1", "UC-peer"}) {
            assertThatThrownBy(() -> this.glossaryService.saveGlossaryTerm(
                    TEAM_1, submitted(RequirementArtifactType.GLOSSARY_TERM, key, "Peer evaluation")))
                    .as(String.valueOf(key)).isInstanceOf(RequirementArtifactRejectedException.class);
        }
        assertThat(this.requirementArtifactRepository.count()).isEqualTo(before);
    }

    @Test
    @DisplayName("A key another artifact of the team holds is refused, naming it (4b)")
    void testCreateWithAHeldKeyIsRefused() {
        // The seed gives team 1 the term "Artifact" as GLO-artifact.
        assertThatThrownBy(() -> this.glossaryService.saveGlossaryTerm(
                TEAM_1, submitted(RequirementArtifactType.GLOSSARY_TERM, "GLO-artifact", "Artifact again")))
                .isInstanceOf(RequirementArtifactRejectedException.class)
                .hasMessage("GLO-artifact is already used by GLO-artifact: Artifact.");
    }

    @Test
    @DisplayName("A body omitting the type is created as a glossary term rather than typeless")
    void testCreateWithoutTypeIsStoredAsGlossaryTerm() {
        RequirementArtifact saved = this.glossaryService.saveGlossaryTerm(
                TEAM_1, submitted(null, "GLO-no-type-supplied", "No type supplied"));

        RequirementArtifact stored = reread(saved.getId());
        assertThat(stored.getType()).isEqualTo(RequirementArtifactType.GLOSSARY_TERM);
        assertThat(stored.getArtifactKey()).isEqualTo("GLO-no-type-supplied");
    }

    /**
     * The definition update copies one field onto the row the URL names, so a type or key in its body is ignored. The
     * rename copies the name and treats a different key as a deliberate key rename (UC-GLO-rename-term), so its body's
     * type is ignored and its key is checked. Probed rather than assumed, because that is the half the peer-evaluation
     * slice of OI-46 turned out to be wrong about.
     */
    @Test
    @DisplayName("Updating a definition leaves the type and key of the term alone")
    void testUpdateDefinitionIgnoresBodyTypeAndKey() {
        RequirementArtifact term = this.glossaryService.saveGlossaryTerm(
                TEAM_1, submitted(RequirementArtifactType.GLOSSARY_TERM, "GLO-definable", "Definable"));
        String originalKey = term.getArtifactKey();

        this.glossaryService.updateGlossaryTermDefinition(TEAM_1, term.getId(),
                submitted(RequirementArtifactType.USE_CASE, "UC-1", "Definable"));

        RequirementArtifact stored = reread(term.getId());
        assertThat(stored.getType()).isEqualTo(RequirementArtifactType.GLOSSARY_TERM);
        assertThat(stored.getArtifactKey()).isEqualTo(originalKey);
        assertThat(stored.getContent()).isEqualTo("A definition.");
    }

    @Test
    @DisplayName("Renaming a term ignores the body's type and keeps the key it was sent (UC-GLO-rename-term)")
    void testRenameKeepsTheKeyItWasSent() {
        RequirementArtifact term = this.glossaryService.saveGlossaryTerm(
                TEAM_1, submitted(RequirementArtifactType.GLOSSARY_TERM, "GLO-renamable", "Renamable"));

        this.glossaryService.renameGlossaryTerm(TEAM_1, term.getId(),
                submitted(RequirementArtifactType.USE_CASE, "GLO-renamable", "Renamed"));

        RequirementArtifact stored = reread(term.getId());
        assertThat(stored.getType()).isEqualTo(RequirementArtifactType.GLOSSARY_TERM);
        assertThat(stored.getArtifactKey()).isEqualTo("GLO-renamable");
        assertThat(stored.getTitle()).isEqualTo("Renamed");
    }

    @Test
    @DisplayName("A rename sent with a new free key renames the key too")
    void testRenameWithANewKeyRenamesTheKey() {
        RequirementArtifact term = this.glossaryService.saveGlossaryTerm(
                TEAM_1, submitted(RequirementArtifactType.GLOSSARY_TERM, "GLO-renamable", "Renamable"));

        this.glossaryService.renameGlossaryTerm(TEAM_1, term.getId(),
                submitted(RequirementArtifactType.GLOSSARY_TERM, "GLO-renamed", "Renamed"));

        assertThat(reread(term.getId()).getArtifactKey()).isEqualTo("GLO-renamed");
    }

    @Test
    @DisplayName("A rename to a malformed key or a held one is refused (4b, 4c)")
    void testRenameWithABadKeyIsRefused() {
        RequirementArtifact term = this.glossaryService.saveGlossaryTerm(
                TEAM_1, submitted(RequirementArtifactType.GLOSSARY_TERM, "GLO-renamable", "Renamable"));

        assertThatThrownBy(() -> this.glossaryService.renameGlossaryTerm(TEAM_1, term.getId(),
                submitted(RequirementArtifactType.GLOSSARY_TERM, "GLO-Renamed", "Renamed")))
                .isInstanceOf(RequirementArtifactRejectedException.class);
        assertThatThrownBy(() -> this.glossaryService.renameGlossaryTerm(TEAM_1, term.getId(),
                submitted(RequirementArtifactType.GLOSSARY_TERM, "GLO-artifact", "Renamed")))
                .isInstanceOf(RequirementArtifactRejectedException.class)
                .hasMessage("GLO-artifact is already used by GLO-artifact: Artifact.");
    }

    @Test
    @DisplayName("A team the caller could not reach is still refused by the create path")
    void testCreateForAnUnknownTeamIsRefused() {
        assertThatThrownBy(() -> this.glossaryService.saveGlossaryTerm(
                999, submitted(RequirementArtifactType.GLOSSARY_TERM, "GLO-nowhere", "Nowhere")))
                .hasMessageContaining("team");
    }

}
