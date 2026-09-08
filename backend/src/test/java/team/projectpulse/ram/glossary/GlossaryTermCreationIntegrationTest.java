package team.projectpulse.ram.glossary;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import team.projectpulse.AbstractIntegrationTest;
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.requirement.RequirementArtifactRepository;
import team.projectpulse.ram.requirement.RequirementArtifactType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The glossary route names the artifact type, so the request body must not get a say in it.
 *
 * <p>{@code POST /teams/{teamId}/glossary-terms} creates a glossary term and nothing else. Its type is fixed by
 * the route and its artifact key is minted from the team's {@code GLO} sequence, exactly as
 * {@code RequirementArtifactService.saveRequirementArtifact} does for the generic artifact route. A body that
 * names a different type, or supplies a key of its own, is submitting values the server owns.
 *
 * <p>Nothing here is a cross-team reach: the team is taken from the path. What a forged type or key corrupts is
 * the caller's own glossary. A {@code USE_CASE} row created through the glossary route is invisible to
 * {@code GlossaryService}, which is type-scoped, but visible to the use-case listings, and a body-chosen key such
 * as {@code UC-1} collides with a real key the team's sequence has already minted or will mint later.
 *
 * <p><strong>Why these probes call the service rather than the endpoint.</strong> The four glossary routes still
 * match no rule in {@code SecurityConfiguration} and so fall to the API catch-all {@code denyAll()} (OI-47), which
 * refuses them for every caller. A MockMvc probe would therefore be answered {@code 403} by the filter chain and
 * would prove nothing about what the service does with the body. Promote these to full-stack probes in
 * {@code RequestBodyIdIntegrationTest} once the route rules land.
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
                TEAM_1, submitted(RequirementArtifactType.USE_CASE, null, "Forged type"));

        RequirementArtifact stored = reread(saved.getId());
        assertThat(stored.getType()).isEqualTo(RequirementArtifactType.GLOSSARY_TERM);
        assertThat(stored.getTeam().getTeamId()).isEqualTo(TEAM_1);
    }

    @Test
    @DisplayName("A body supplying an artifact key has it replaced by one minted from the team GLO sequence")
    void testCreateWithSuppliedArtifactKeyIsRekeyed() {
        RequirementArtifact saved = this.glossaryService.saveGlossaryTerm(
                TEAM_1, submitted(RequirementArtifactType.GLOSSARY_TERM, "UC-1", "Forged key"));

        assertThat(reread(saved.getId()).getArtifactKey()).startsWith("GLO-").isNotEqualTo("UC-1");
    }

    @Test
    @DisplayName("A body omitting the artifact key still gets one, so no glossary term is created keyless")
    void testCreateWithoutArtifactKeyIsStillKeyed() {
        RequirementArtifact saved = this.glossaryService.saveGlossaryTerm(
                TEAM_1, submitted(RequirementArtifactType.GLOSSARY_TERM, null, "No key supplied"));

        assertThat(reread(saved.getId()).getArtifactKey()).startsWith("GLO-");
    }

    @Test
    @DisplayName("A body omitting the type is created as a glossary term rather than typeless")
    void testCreateWithoutTypeIsStoredAsGlossaryTerm() {
        RequirementArtifact saved = this.glossaryService.saveGlossaryTerm(
                TEAM_1, submitted(null, null, "No type supplied"));

        RequirementArtifact stored = reread(saved.getId());
        assertThat(stored.getType()).isEqualTo(RequirementArtifactType.GLOSSARY_TERM);
        assertThat(stored.getArtifactKey()).startsWith("GLO-");
    }

    /**
     * The two update paths copy one field each onto the row the URL names, so a forged type or key in the body is
     * already ignored. Probed rather than assumed, because that is the half the peer-evaluation slice of OI-46
     * turned out to be wrong about.
     */
    @Test
    @DisplayName("Updating a definition leaves the type and key of the term alone")
    void testUpdateDefinitionIgnoresBodyTypeAndKey() {
        RequirementArtifact term = this.glossaryService.saveGlossaryTerm(
                TEAM_1, submitted(RequirementArtifactType.GLOSSARY_TERM, null, "Definable"));
        String originalKey = term.getArtifactKey();

        this.glossaryService.updateGlossaryTermDefinition(TEAM_1, term.getId(),
                submitted(RequirementArtifactType.USE_CASE, "UC-1", "Definable"));

        RequirementArtifact stored = reread(term.getId());
        assertThat(stored.getType()).isEqualTo(RequirementArtifactType.GLOSSARY_TERM);
        assertThat(stored.getArtifactKey()).isEqualTo(originalKey);
        assertThat(stored.getContent()).isEqualTo("A definition.");
    }

    @Test
    @DisplayName("Renaming a term leaves its type and key alone")
    void testRenameIgnoresBodyTypeAndKey() {
        RequirementArtifact term = this.glossaryService.saveGlossaryTerm(
                TEAM_1, submitted(RequirementArtifactType.GLOSSARY_TERM, null, "Renamable"));
        String originalKey = term.getArtifactKey();

        this.glossaryService.renameGlossaryTerm(TEAM_1, term.getId(),
                submitted(RequirementArtifactType.USE_CASE, "UC-1", "Renamed"));

        RequirementArtifact stored = reread(term.getId());
        assertThat(stored.getType()).isEqualTo(RequirementArtifactType.GLOSSARY_TERM);
        assertThat(stored.getArtifactKey()).isEqualTo(originalKey);
        assertThat(stored.getTitle()).isEqualTo("Renamed");
    }

    @Test
    @DisplayName("A team the caller could not reach is still refused by the create path")
    void testCreateForAnUnknownTeamIsRefused() {
        assertThatThrownBy(() -> this.glossaryService.saveGlossaryTerm(
                999, submitted(RequirementArtifactType.GLOSSARY_TERM, null, "Nowhere")))
                .hasMessageContaining("team");
    }

}
