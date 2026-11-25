package team.projectpulse.ram.usecase;

import team.projectpulse.ram.TestKeys;

import jakarta.persistence.EntityManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import team.projectpulse.AbstractIntegrationTest;
import team.projectpulse.ram.document.DocumentRepository;
import team.projectpulse.ram.document.DocumentSectionRepository;
import team.projectpulse.ram.document.DocumentType;
import team.projectpulse.ram.requirement.ArtifactLink;
import team.projectpulse.ram.requirement.ArtifactLinkRepository;
import team.projectpulse.ram.requirement.ArtifactLinkType;
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.requirement.RequirementArtifactRepository;
import team.projectpulse.system.StatusCode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * The use case template's text fields and business rules (UC-DOC-create-use-case, UC-DOC-edit-use-case,
 * UC-DOC-view-use-case; docs/design/doc.md "Business rules as links", "Creating a business rule from the picker" and
 * "The template's text fields"). Team 1's seed: UC-create-use-case is use case 3 (its artifact 3), with STK-student as primary actor and
 * STK-instructor as secondary actor; RULE-own-team-documents is the team's one business rule, in its Business Rules document, and no use case
 * names it.
 */
class UseCaseTemplateFieldsIntegrationTest extends AbstractIntegrationTest {

    private static final long UC_1 = 3L;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UseCaseRepository useCaseRepository;

    @Autowired
    ArtifactLinkRepository artifactLinkRepository;

    @Autowired
    RequirementArtifactRepository requirementArtifactRepository;

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    DocumentSectionRepository documentSectionRepository;

    @Autowired
    EntityManager entityManager;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    String johnToken; // John is on team 1

    String woodyToken; // Woody is on team 2

    long rule1Id;

    @BeforeEach
    void setUp() throws Exception {
        this.johnToken = login("j.smith@abc.edu");
        this.woodyToken = login("w.allen@abc.edu");
        this.rule1Id = firstArtifact(1, "BUSINESS_RULE", this.johnToken).getLong("id");
    }

    // ---- Create ----

    @Test
    void addUseCase_TextFieldsAndBusinessRules_Stored() throws Exception {
        // Main flow: the four text fields round-trip, and RULE-own-team-documents is linked by one CONSTRAINS link, from the rule to the
        // new use case's artifact, with the route's team.
        JSONObject payload = minimalUseCase()
                .put("frequencyOfUse", "About 50 times a day during the semester.")
                .put("associatedInformation", "<p>Details:</p><ul><li><p>Name</p></li></ul>")
                .put("assumptions", "<p>The student has a team.</p>")
                .put("openIssues", "<p>None yet.</p>")
                .put("businessRuleIds", new JSONArray().put(this.rule1Id));

        JSONObject response = postUseCase(payload);

        assertThat(response.getBoolean("flag")).as(response.toString()).isTrue();
        JSONObject data = response.getJSONObject("data");
        assertThat(data.getString("frequencyOfUse")).isEqualTo("About 50 times a day during the semester.");
        assertThat(data.getString("associatedInformation")).isEqualTo("<p>Details:</p><ul><li><p>Name</p></li></ul>");
        assertThat(data.getString("assumptions")).isEqualTo("<p>The student has a team.</p>");
        assertThat(data.getString("openIssues")).isEqualTo("<p>None yet.</p>");
        assertThat(ids(data.getJSONArray("businessRuleIds"))).containsExactly(this.rule1Id);

        long useCaseId = data.getLong("id");
        assertThat(constrainsLinksTo(useCaseId)).extracting(link -> link.getSourceArtifact().getId(), link -> link.getTeam().getTeamId())
                .containsExactly(tuple(this.rule1Id, 1));

        // Read back through GET, after the transaction's writes are flushed.
        JSONObject fetched = fetchUseCase(useCaseId);
        assertThat(fetched.getString("assumptions")).isEqualTo("<p>The student has a team.</p>");
        assertThat(ids(fetched.getJSONArray("businessRuleIds"))).containsExactly(this.rule1Id);
    }

    @Test
    void addUseCase_Extension4a_NotABusinessRule_Refused() throws Exception {
        long featureLikeId = firstArtifact(1, "FUNCTIONAL_REQUIREMENT", this.johnToken).getLong("id");
        long useCasesBefore = this.useCaseRepository.count();
        long linksBefore = this.artifactLinkRepository.count();

        JSONObject response = postUseCase(minimalUseCase().put("businessRuleIds", new JSONArray().put(featureLikeId)));

        assertThat(response.getBoolean("flag")).isFalse();
        assertThat(response.getInt("code")).isEqualTo(StatusCode.INVALID_ARGUMENT);
        assertThat(response.getString("message")).isEqualTo("Business rule " + featureLikeId + " must be a business rule.");
        assertThat(this.useCaseRepository.count()).isEqualTo(useCasesBefore);
        assertThat(this.artifactLinkRepository.count()).isEqualTo(linksBefore);
    }

    @Test
    void addUseCase_BusinessRuleOfAnotherTeam_NotFound_LikeAnUnknownId() throws Exception {
        // Team 2's functional requirement through team 1's URL: the scoped load refuses it before its type is read,
        // with the same message as an id that names nothing.
        long foreignId = firstArtifact(2, "FUNCTIONAL_REQUIREMENT", this.woodyToken).getLong("id");
        long unknownId = 999_999L;
        long linksBefore = this.artifactLinkRepository.count();

        JSONObject foreign = postUseCase(minimalUseCase().put("businessRuleIds", new JSONArray().put(foreignId)));
        JSONObject unknown = postUseCase(minimalUseCase().put("businessRuleIds", new JSONArray().put(unknownId)));

        assertThat(foreign.getInt("code")).isEqualTo(StatusCode.NOT_FOUND);
        assertThat(unknown.getInt("code")).isEqualTo(StatusCode.NOT_FOUND);
        assertThat(foreign.getString("message").replace(String.valueOf(foreignId), "<id>"))
                .isEqualTo(unknown.getString("message").replace(String.valueOf(unknownId), "<id>"));
        assertThat(this.artifactLinkRepository.count()).isEqualTo(linksBefore);
    }

    @Test
    void addUseCase_FrequencyOfUseLength_255Saves_256Refused() throws Exception {
        JSONObject saved = postUseCase(minimalUseCase().put("frequencyOfUse", "x".repeat(255)));
        assertThat(saved.getBoolean("flag")).as(saved.toString()).isTrue();

        JSONObject refused = postUseCase(minimalUseCase().put("title", "Another probe").put("frequencyOfUse", "x".repeat(256)));
        assertThat(refused.getBoolean("flag")).isFalse();
        assertThat(refused.getInt("code")).isEqualTo(StatusCode.INVALID_ARGUMENT);
        assertThat(refused.toString()).contains("frequencyOfUse");
    }

    @Test
    void addUseCase_Extension3b_RuleCreatedFirst_ThenLinked() throws Exception {
        JSONObject created = createBusinessRule("Each team has exactly one instructor.");
        assertThat(created.getBoolean("flag")).as(created.toString()).isTrue();
        long newRuleId = created.getJSONObject("data").getLong("id");

        JSONObject response = postUseCase(minimalUseCase().put("businessRuleIds", new JSONArray().put(newRuleId)));

        assertThat(response.getBoolean("flag")).as(response.toString()).isTrue();
        long useCaseId = response.getJSONObject("data").getLong("id");
        assertThat(constrainsLinksTo(useCaseId)).extracting(link -> link.getSourceArtifact().getId()).containsExactly(newRuleId);
    }

    // ---- Edit ----

    @Test
    void updateUseCase_Extension7a_NotABusinessRule_Refused_NothingChanges() throws Exception {
        long frId = firstArtifact(1, "FUNCTIONAL_REQUIREMENT", this.johnToken).getLong("id");
        lockUseCase();
        JSONObject before = fetchUseCase(UC_1);
        long linksBefore = this.artifactLinkRepository.count();

        JSONObject response = putUseCase(new JSONObject(before.toString()).put("businessRuleIds", new JSONArray().put(frId)));

        assertThat(response.getBoolean("flag")).isFalse();
        assertThat(response.getInt("code")).isEqualTo(StatusCode.INVALID_ARGUMENT);
        assertThat(response.getString("message")).isEqualTo("Business rule " + frId + " must be a business rule.");
        JSONObject after = fetchUseCase(UC_1);
        assertThat(after.getInt("version")).isEqualTo(before.getInt("version"));
        assertThat(after.getJSONArray("businessRuleIds").length()).isZero();
        assertThat(this.artifactLinkRepository.count()).isEqualTo(linksBefore);
    }

    @Test
    void updateUseCase_BusinessRuleOfAnotherTeam_NotFound() throws Exception {
        long foreignId = firstArtifact(2, "FUNCTIONAL_REQUIREMENT", this.woodyToken).getLong("id");
        lockUseCase();
        JSONObject before = fetchUseCase(UC_1);

        JSONObject response = putUseCase(new JSONObject(before.toString()).put("businessRuleIds", new JSONArray().put(foreignId)));

        assertThat(response.getInt("code")).isEqualTo(StatusCode.NOT_FOUND);
        assertThat(fetchUseCase(UC_1).getInt("version")).isEqualTo(before.getInt("version"));
        assertThat(constrainsLinksTo(UC_1)).isEmpty();
    }

    @Test
    void updateUseCase_BusinessRulesChanged_OnlyTheChangedLinksAreWritten() throws Exception {
        // RULE-own-team-documents also constrains FR-user-authentication through the link route; that link is an incoming link of FR-user-authentication, not of UC-create-use-case, so no
        // use case save may touch it. UC-create-use-case's actor links must stay as they are too.
        long rule2Id = createBusinessRule("A use case name is unique within its team.").getJSONObject("data").getLong("id");
        long fr1Id = firstArtifact(1, "FUNCTIONAL_REQUIREMENT", this.johnToken).getLong("id");
        createLink(this.rule1Id, fr1Id, "CONSTRAINS");
        lockUseCase();

        // First save: RULE-own-team-documents only.
        JSONObject first = putUseCase(fetchUseCase(UC_1).put("businessRuleIds", new JSONArray().put(this.rule1Id)));
        assertThat(first.getBoolean("flag")).as(first.toString()).isTrue();
        ArtifactLink rule1Link = constrainsLinksTo(UC_1).getFirst();
        Set<Long> actorLinkIdsBefore = actorLinkIds();

        // Second save: RULE-own-team-documents kept, a second rule added.
        JSONObject second = putUseCase(fetchUseCase(UC_1).put("businessRuleIds", new JSONArray().put(this.rule1Id).put(rule2Id)));
        assertThat(second.getBoolean("flag")).as(second.toString()).isTrue();
        List<ArtifactLink> afterAdd = constrainsLinksTo(UC_1);
        assertThat(afterAdd).extracting(link -> link.getSourceArtifact().getId()).containsExactlyInAnyOrder(this.rule1Id, rule2Id);
        ArtifactLink keptRule1Link = afterAdd.stream().filter(link -> link.getSourceArtifact().getId().equals(this.rule1Id)).findFirst().orElseThrow();
        assertThat(keptRule1Link.getId()).isEqualTo(rule1Link.getId());
        assertThat(keptRule1Link.getCreatedBy().getId()).isEqualTo(rule1Link.getCreatedBy().getId());

        // Third save: RULE-own-team-documents dropped. Its link goes, the rule stays, and its link to FR-user-authentication is untouched.
        JSONObject third = putUseCase(fetchUseCase(UC_1).put("businessRuleIds", new JSONArray().put(rule2Id)));
        assertThat(third.getBoolean("flag")).as(third.toString()).isTrue();
        assertThat(constrainsLinksTo(UC_1)).extracting(link -> link.getSourceArtifact().getId()).containsExactly(rule2Id);
        assertThat(this.requirementArtifactRepository.findById(this.rule1Id)).isPresent();
        assertThat(this.artifactLinkRepository.findBySourceArtifactId(this.rule1Id, 1))
                .extracting(link -> link.getType(), link -> link.getTargetArtifact().getId())
                .containsExactly(tuple(ArtifactLinkType.CONSTRAINS, fr1Id));
        assertThat(actorLinkIds()).isEqualTo(actorLinkIdsBefore);
    }

    @Test
    void updateUseCase_TextFieldsChangedThenCleared() throws Exception {
        lockUseCase();
        JSONObject before = fetchUseCase(UC_1);

        JSONObject changed = putUseCase(new JSONObject(before.toString())
                .put("frequencyOfUse", "Weekly.")
                .put("associatedInformation", "<p>Duplication detection rules: name must be unique.</p>")
                .put("assumptions", "<p>Teams are assigned.</p>")
                .put("openIssues", "<p>Ask the client about exports.</p>"));
        assertThat(changed.getBoolean("flag")).as(changed.toString()).isTrue();
        JSONObject afterChange = fetchUseCase(UC_1);
        assertThat(afterChange.getString("frequencyOfUse")).isEqualTo("Weekly.");
        assertThat(afterChange.getString("openIssues")).isEqualTo("<p>Ask the client about exports.</p>");
        assertThat(afterChange.getInt("version")).isGreaterThan(before.getInt("version"));

        // Emptied and left out: an empty string is stored as sent, and a field left out arrives as null and clears.
        JSONObject cleared = new JSONObject(afterChange.toString()).put("frequencyOfUse", "").put("associatedInformation", "");
        cleared.remove("assumptions");
        cleared.remove("openIssues");
        JSONObject clearedResponse = putUseCase(cleared);
        assertThat(clearedResponse.getBoolean("flag")).as(clearedResponse.toString()).isTrue();
        JSONObject afterClear = fetchUseCase(UC_1);
        assertThat(afterClear.getString("frequencyOfUse")).isEmpty();
        assertThat(afterClear.getString("associatedInformation")).isEmpty();
        assertThat(afterClear.isNull("assumptions")).isTrue();
        assertThat(afterClear.isNull("openIssues")).isTrue();
        assertThat(afterClear.getInt("version")).isGreaterThan(afterChange.getInt("version"));
    }

    @Test
    void updateUseCase_FrequencyOfUseTooLong_Refused() throws Exception {
        lockUseCase();
        JSONObject before = fetchUseCase(UC_1);

        JSONObject response = putUseCase(new JSONObject(before.toString()).put("frequencyOfUse", "x".repeat(256)));

        assertThat(response.getInt("code")).isEqualTo(StatusCode.INVALID_ARGUMENT);
        assertThat(response.toString()).contains("frequencyOfUse");
        assertThat(fetchUseCase(UC_1).getInt("version")).isEqualTo(before.getInt("version"));
    }

    @Test
    void updateUseCase_Extension6d_RuleCreatedWhileLocked_ThenLinked() throws Exception {
        // While John holds UC-create-use-case's lock, he creates a business rule: it keeps the key it was sent, at the end of the Business Rules
        // list, and UC-create-use-case's version and lock are untouched. His save then links it.
        lockUseCase();
        JSONObject before = fetchUseCase(UC_1);

        JSONObject created = createBusinessRule("A requirement document locked for review cannot be edited.");
        assertThat(created.getBoolean("flag")).as(created.toString()).isTrue();
        assertThat(created.getJSONObject("data").getString("artifactKey")).isEqualTo("RULE-a-requirement-document-locked-for-review");
        long newRuleId = created.getJSONObject("data").getLong("id");
        long documentId = this.documentRepository.findAll().stream()
                .filter(document -> document.getTeam().getTeamId().equals(1) && document.getType() == DocumentType.BUSINESS_RULES)
                .findFirst().orElseThrow().getId();
        assertThat(sectionItemKeys(documentId, "BUSINESS_RULES")).containsExactly("RULE-own-team-documents", "RULE-a-requirement-document-locked-for-review");
        JSONObject unchanged = fetchUseCase(UC_1);
        assertThat(unchanged.getInt("version")).isEqualTo(before.getInt("version"));
        assertThat(unchanged.getJSONObject("lock").getBoolean("locked")).isTrue();

        JSONObject saved = putUseCase(unchanged.put("businessRuleIds", new JSONArray().put(newRuleId)));
        assertThat(saved.getBoolean("flag")).as(saved.toString()).isTrue();
        assertThat(constrainsLinksTo(UC_1)).extracting(link -> link.getSourceArtifact().getId()).containsExactly(newRuleId);
    }

    @Test
    void ruleKept_WhenTheEditIsAbandoned() throws Exception {
        // 6a and 11a: a rule created during an edit that is then abandoned stays in the Business Rules document, and
        // nothing links it to UC-create-use-case.
        lockUseCase();
        long newRuleId = createBusinessRule("Peer evaluations close at the end of the week.").getJSONObject("data").getLong("id");

        this.mockMvc.perform(delete(this.baseUrl + "/teams/1/use-cases/" + UC_1 + "/lock")
                        .header(HttpHeaders.AUTHORIZATION, this.johnToken))
                .andReturn();

        assertThat(this.requirementArtifactRepository.findById(newRuleId)).isPresent();
        assertThat(constrainsLinksTo(UC_1)).isEmpty();
    }

    // ---- View ----

    @Test
    void findUseCase_ShowsRulesLinkedThroughTheLinkRoute_ButNotNonRules() throws Exception {
        // A CONSTRAINS link made through UC-LNK-create-link shows as a business rule. One from a functional
        // requirement, which the link routes do not refuse yet (OI-27), does not.
        long frId = firstArtifact(1, "FUNCTIONAL_REQUIREMENT", this.johnToken).getLong("id");
        createLink(this.rule1Id, UC_1, "CONSTRAINS");
        createLink(frId, UC_1, "CONSTRAINS");

        JSONObject fetched = fetchUseCase(UC_1);

        assertThat(ids(fetched.getJSONArray("businessRuleIds"))).containsExactly(this.rule1Id);
    }

    // ---- Delete ----

    @Test
    void deleteRule_ThatConstrainsAUseCase_Refused_AndTheLinkIsNamed() throws Exception {
        lockUseCase();
        putUseCase(fetchUseCase(UC_1).put("businessRuleIds", new JSONArray().put(this.rule1Id)));
        this.entityManager.flush();
        this.entityManager.clear();
        Integer version = this.requirementArtifactRepository.findById(this.rule1Id).orElseThrow().getVersion();

        MvcResult result = this.mockMvc.perform(delete(this.baseUrl + "/teams/1/requirement-artifacts/" + this.rule1Id)
                        .param("version", String.valueOf(version))
                        .header(HttpHeaders.AUTHORIZATION, this.johnToken))
                .andReturn();
        JSONObject response = new JSONObject(result.getResponse().getContentAsString());

        assertThat(response.getBoolean("flag")).isFalse();
        assertThat(response.getString("message")).contains("the link RULE-own-team-documents CONSTRAINS UC-create-use-case");
        assertThat(this.requirementArtifactRepository.findById(this.rule1Id)).isPresent();
    }

    // ---- Helpers ----

    private String login(String username) throws Exception {
        MvcResult result = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic(username, "123456"))).andReturn();
        return "Bearer " + new JSONObject(result.getResponse().getContentAsString()).getJSONObject("data").getString("token");
    }

    private JSONObject minimalUseCase() throws Exception {
        return new JSONObject()
                .put("title", "Template fields probe")
                .put("description", "A minimal use case for checking the template's fields.")
                .put("primaryActorId", 1L)
                .put("secondaryActorIds", new JSONArray())
                .put("businessRuleIds", new JSONArray())
                .put("trigger", "The student starts the probe.")
                .put("preconditions", new JSONArray())
                .put("postconditions", new JSONArray())
                .put("mainSteps", new JSONArray());
    }

    private JSONObject postUseCase(JSONObject payload) throws Exception {
        return send(post(this.baseUrl + "/teams/1/use-cases"), TestKeys.fillUseCaseKeys(payload, null));
    }

    private JSONObject putUseCase(JSONObject payload) throws Exception {
        // A payload with no key sends the use case's current one, so the save renames nothing.
        String currentKey = payload.has("artifactKey") ? null : fetchUseCase(UC_1).getString("artifactKey");
        return send(put(this.baseUrl + "/teams/1/use-cases/" + UC_1), TestKeys.fillUseCaseKeys(payload, currentKey));
    }

    private JSONObject send(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request, JSONObject payload) throws Exception {
        MvcResult result = this.mockMvc.perform(request
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload.toString())
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.johnToken))
                .andReturn();
        return new JSONObject(result.getResponse().getContentAsString());
    }

    private JSONObject fetchUseCase(long useCaseId) throws Exception {
        MvcResult result = this.mockMvc.perform(get(this.baseUrl + "/teams/1/use-cases/" + useCaseId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.johnToken))
                .andReturn();
        return new JSONObject(result.getResponse().getContentAsString()).getJSONObject("data");
    }

    private void lockUseCase() throws Exception {
        MvcResult result = this.mockMvc.perform(put(this.baseUrl + "/teams/1/use-cases/" + UC_1 + "/lock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"Editing\"}")
                        .header(HttpHeaders.AUTHORIZATION, this.johnToken))
                .andReturn();
        assertThat(new JSONObject(result.getResponse().getContentAsString()).getBoolean("flag")).isTrue();
    }

    /** Creates a team 1 business rule as John through the artifact create route, as the picker's dialog does. */
    private JSONObject createBusinessRule(String title) throws Exception {
        MvcResult result = this.mockMvc.perform(post(this.baseUrl + "/teams/1/requirement-artifacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new JSONObject().put("type", "BUSINESS_RULE").put("artifactKey", TestKeys.keyFor("BUSINESS_RULE", title)).put("title", title).put("content", "Course policy.").toString())
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.johnToken))
                .andReturn();
        return new JSONObject(result.getResponse().getContentAsString());
    }

    private void createLink(long sourceId, long targetId, String type) throws Exception {
        MvcResult result = this.mockMvc.perform(post(this.baseUrl + "/teams/1/artifact-links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new JSONObject().put("sourceArtifactId", sourceId).put("targetArtifactId", targetId).put("type", type).toString())
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.johnToken))
                .andReturn();
        JSONObject response = new JSONObject(result.getResponse().getContentAsString());
        assertThat(response.getBoolean("flag")).as(response.toString()).isTrue();
    }

    private JSONObject firstArtifact(int teamId, String type, String token) throws Exception {
        MvcResult result = this.mockMvc.perform(post(this.baseUrl + "/teams/" + teamId + "/requirement-artifacts/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new JSONObject().put("type", type).toString())
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andReturn();
        return new JSONObject(result.getResponse().getContentAsString()).getJSONObject("data").getJSONArray("content").getJSONObject(0);
    }

    /** The CONSTRAINS links that end at a use case's artifact, read from the database after a flush. */
    private List<ArtifactLink> constrainsLinksTo(long useCaseId) {
        this.entityManager.flush();
        this.entityManager.clear();
        return this.artifactLinkRepository.findByTargetArtifactId(useCaseId, 1).stream()
                .filter(link -> link.getType() == ArtifactLinkType.CONSTRAINS)
                .toList();
    }

    private Set<Long> actorLinkIds() {
        this.entityManager.flush();
        this.entityManager.clear();
        Set<Long> ids = new HashSet<>();
        this.artifactLinkRepository.findBySourceArtifactId(UC_1, 1).stream()
                .filter(link -> link.getType() == ArtifactLinkType.PRIMARY_ACTOR || link.getType() == ArtifactLinkType.SECONDARY_ACTOR)
                .forEach(link -> ids.add(link.getId()));
        return ids;
    }

    private List<String> sectionItemKeys(long documentId, String sectionKey) {
        this.entityManager.flush();
        this.entityManager.clear();
        return this.documentSectionRepository.findByDocumentIdAndSectionKey(documentId, sectionKey).orElseThrow()
                .getRequirementArtifacts().stream().map(RequirementArtifact::getArtifactKey).toList();
    }

    private static List<Long> ids(JSONArray array) throws Exception {
        List<Long> ids = new java.util.ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            ids.add(array.getLong(i));
        }
        return ids;
    }
}
