package team.projectpulse.ram.usecase;

import team.projectpulse.ram.TestKeys;

import jakarta.persistence.EntityManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import team.projectpulse.AbstractIntegrationTest;
import team.projectpulse.system.StatusCode;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Preconditions and postconditions as shared, ordered references (UC-DOC-create-use-case, UC-DOC-edit-use-case,
 * BR-use-case-condition-shared, BR-shared-condition-wording). One test per row of the conditions test table in
 * {@code docs/design/doc.md}.
 * <p>
 * Seed facts these tests rely on: team 1's use case 3 has preconditions 4, 5 and 6 and postconditions 7 and 8, in the
 * order {@code DataInitializer} lists them, with keys PRE-student-logged-in to PRE-document-not-locked and POST-use-case-stored to POST-collaboration-metadata-recorded, and no other use case
 * references them.
 */
class UseCaseConditionIntegrationTest extends AbstractIntegrationTest {

    private static final String LOGGED_IN = "The Student is logged into the System.";
    private static final String MEMBER = "The Student is a member of the team that owns the use case document.";
    private static final String NOT_LOCKED = "Document is not locked for review.";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    EntityManager entityManager;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    String studentJohnToken; // John is on team 1

    String studentWoodyToken; // Woody is on team 2

    @BeforeEach
    void setUp() throws Exception {
        this.studentJohnToken = login("j.smith@abc.edu");
        this.studentWoodyToken = login("w.allen@abc.edu");
    }

    // ---- Seed and create ----

    @Test
    @DisplayName("The seeded use case keeps id 3, and its conditions have keys in seed order")
    void seededConditionsHaveKeysInOrder() throws Exception {
        JSONObject useCase = fetchUseCase(3);

        assertThat(useCase.getLong("id")).isEqualTo(3L);
        assertThat(keysOf(useCase.getJSONArray("preconditions"))).containsExactly("PRE-student-logged-in", "PRE-student-on-owning-team", "PRE-document-not-locked");
        assertThat(wordingsOf(useCase.getJSONArray("preconditions"))).containsExactly(LOGGED_IN, MEMBER, NOT_LOCKED);
        assertThat(keysOf(useCase.getJSONArray("postconditions"))).containsExactly("POST-use-case-stored", "POST-collaboration-metadata-recorded");
    }

    @Test
    @DisplayName("New conditions take their type from the list, the route's team and a key, in the submitted order")
    void createStoresNewConditionsWithServerSetTypeTeamAndKey() throws Exception {
        JSONObject payload = minimalUseCase()
                .put("preconditions", new JSONArray()
                        .put(new JSONObject().put("condition", "Second probe precondition").put("type", "STAKEHOLDER"))
                        .put(new JSONObject().put("condition", "First probe precondition")))
                .put("postconditions", new JSONArray().put(new JSONObject().put("condition", "Probe postcondition")));

        JSONObject data = requireSuccess(postUseCase(payload));

        JSONArray preconditions = data.getJSONArray("preconditions");
        assertThat(wordingsOf(preconditions)).containsExactly("Second probe precondition", "First probe precondition");
        for (int i = 0; i < preconditions.length(); i++) {
            JSONObject condition = preconditions.getJSONObject(i);
            assertThat(condition.getString("type")).isEqualTo("PRECONDITION");
            assertThat(condition.getString("artifactKey")).startsWith("PRE-");
            assertThat(teamOf(condition.getLong("id"))).isEqualTo(1);
        }
        JSONObject postcondition = data.getJSONArray("postconditions").getJSONObject(0);
        assertThat(postcondition.getString("type")).isEqualTo("POSTCONDITION");
        assertThat(postcondition.getString("artifactKey")).startsWith("POST-");
    }

    @Test
    @DisplayName("Picking an existing condition links it by id and stores no new condition")
    void createLinksAnExistingCondition() throws Exception {
        long conditionsBefore = countConditions();
        JSONObject payload = minimalUseCase()
                .put("preconditions", new JSONArray().put(new JSONObject().put("id", 4).put("condition", LOGGED_IN)));

        JSONObject data = requireSuccess(postUseCase(payload));

        assertThat(data.getJSONArray("preconditions").getJSONObject(0).getLong("id")).isEqualTo(4L);
        assertThat(countConditions()).isEqualTo(conditionsBefore);
        JSONArray others = data.getJSONArray("preconditions").getJSONObject(0).getJSONArray("otherUseCases");
        assertThat(others.length()).isEqualTo(1);
        assertThat(others.getJSONObject(0).getLong("id")).isEqualTo(3L);
    }

    @Test
    @DisplayName("The wording sent with a picked condition is ignored: it is linked with its stored wording")
    void createIgnoresTheWordingOfAPickedCondition() throws Exception {
        long conditionsBefore = countConditions();

        JSONObject data = requireSuccess(postUseCase(minimalUseCase()
                .put("preconditions", new JSONArray().put(new JSONObject().put("condition", "A new one")).put(entry(4, "Reworded login")))));

        JSONObject picked = data.getJSONArray("preconditions").getJSONObject(1);
        assertThat(picked.getLong("id")).isEqualTo(4L);
        assertThat(picked.getString("condition")).isEqualTo(LOGGED_IN);
        // Only the typed one is new.
        assertThat(countConditions()).isEqualTo(conditionsBefore + 1);
        assertThat(fetchUseCase(3).getJSONArray("preconditions").getJSONObject(0).getString("condition")).isEqualTo(LOGGED_IN);
    }

    @Test
    @DisplayName("Two new entries with the same wording become two separate conditions, in order")
    void createKeepsTwoEntriesWithTheSameWording() throws Exception {
        JSONObject payload = minimalUseCase()
                .put("preconditions", new JSONArray()
                        // Same wording, so each needs a key of its own: one key on two new entries is refused (4a).
                        .put(new JSONObject().put("condition", "TBD").put("artifactKey", "PRE-tbd-first"))
                        .put(new JSONObject().put("condition", "TBD").put("artifactKey", "PRE-tbd-second")));

        JSONArray preconditions = requireSuccess(postUseCase(payload)).getJSONArray("preconditions");

        assertThat(preconditions.length()).isEqualTo(2);
        assertThat(preconditions.getJSONObject(0).getLong("id")).isNotEqualTo(preconditions.getJSONObject(1).getLong("id"));
    }

    // ---- Artifact keys (docs/design/doc.md, "Artifact keys of a use case and its conditions") ----

    @Test
    void createStoresTheKeysSent_AndRefusesMissingRepeatedOrHeldKeys() throws Exception {
        JSONObject created = requireSuccess(postUseCase(minimalUseCase().put("artifactKey", "UC-key-probe")
                .put("preconditions", new JSONArray()
                        .put(new JSONObject().put("condition", "The probe is ready.").put("artifactKey", "PRE-probe-ready"))
                        .put(new JSONObject().put("condition", "The probe is set.").put("artifactKey", "PRE-probe-set")))));
        assertThat(created.getString("artifactKey")).isEqualTo("UC-key-probe");
        assertThat(keysOf(created.getJSONArray("preconditions"))).containsExactly("PRE-probe-ready", "PRE-probe-set");

        long conditionsBefore = countConditions();
        // 4a: a new condition with no key, and one key on two new conditions.
        JSONObject noKey = postUseCase(minimalUseCase().put("artifactKey", "UC-key-probe-two")
                .put("preconditions", new JSONArray().put(new JSONObject().put("condition", "Keyless.").put("artifactKey", JSONObject.NULL))));
        assertThat(noKey.getString("message")).isEqualTo("Precondition 1 needs an artifact key.");
        JSONObject repeated = postUseCase(minimalUseCase().put("artifactKey", "UC-key-probe-two")
                .put("preconditions", new JSONArray()
                        .put(new JSONObject().put("condition", "One.").put("artifactKey", "PRE-twice"))
                        .put(new JSONObject().put("condition", "Two.").put("artifactKey", "PRE-twice"))));
        assertThat(repeated.getString("message")).isEqualTo("Precondition 2 has the same artifact key as precondition 1.");
        // 4b: a key another artifact of the team holds refuses the whole save, naming it.
        JSONObject held = postUseCase(minimalUseCase().put("artifactKey", "UC-key-probe-two")
                .put("preconditions", new JSONArray().put(new JSONObject().put("condition", "Logged in again.").put("artifactKey", "PRE-student-logged-in"))));
        assertThat(held.getString("message")).startsWith("PRE-student-logged-in is already used by");
        assertThat(held.getJSONObject("data").getString("type")).isEqualTo("PRECONDITION");
        assertThat(countConditions()).isEqualTo(conditionsBefore);
    }

    @Test
    void editRenamesTheUseCaseKeyUnderTheLock_AndAPickedConditionKeepsItsKey() throws Exception {
        lock(3);
        JSONObject payload = fetchUseCase(3).put("artifactKey", "UC-author-a-use-case");
        // Sending another key with an existing condition changes nothing: it keeps its stored key, as its wording.
        payload.getJSONArray("preconditions").getJSONObject(0).put("artifactKey", "PRE-something-else");
        JSONObject saved = requireSuccess(putUseCase(3, payload));
        assertThat(saved.getString("artifactKey")).isEqualTo("UC-author-a-use-case");
        assertThat(saved.getJSONArray("preconditions").getJSONObject(0).getString("artifactKey")).isEqualTo("PRE-student-logged-in");

        // 7b: a held key on a new condition is refused, and nothing changes.
        JSONObject refusedPayload = fetchUseCase(3);
        refusedPayload.getJSONArray("postconditions").put(new JSONObject().put("condition", "Stored again.").put("artifactKey", "POST-use-case-stored"));
        JSONObject refused = putUseCase(3, refusedPayload);
        assertThat(refused.getString("message")).startsWith("POST-use-case-stored is already used by");
        assertThat(fetchUseCase(3).getJSONArray("postconditions").length()).isEqualTo(2);
    }

    // ---- Edit ----

    @Test
    @DisplayName("Reordering follows the submission and keeps the condition ids: a move to the front, then a swap")
    void editReordersConditions() throws Exception {
        lock(3);
        // Move the last one to the front.
        JSONObject moved = requireSuccess(putUseCase(3, withPreconditions(fetchUseCase(3), entry(6, NOT_LOCKED), entry(4, LOGGED_IN), entry(5, MEMBER))));
        assertThat(idsOf(moved.getJSONArray("preconditions"))).containsExactly(6L, 4L, 5L);

        // Swap the first two.
        JSONObject swapped = requireSuccess(putUseCase(3, withPreconditions(moved, entry(4, LOGGED_IN), entry(6, NOT_LOCKED), entry(5, MEMBER))));
        assertThat(idsOf(swapped.getJSONArray("preconditions"))).containsExactly(4L, 6L, 5L);

        // Read the stored order back from the join table, past the persistence context.
        this.entityManager.flush();
        this.entityManager.clear();
        @SuppressWarnings("unchecked")
        List<Number> stored = this.entityManager.createNativeQuery("select precondition_artifact_id from use_case_precondition where use_case_id = 3 order by precondition_index")
                .getResultList();
        assertThat(stored.stream().map(Number::longValue).toList()).containsExactly(4L, 6L, 5L);
    }

    @Test
    @DisplayName("Removing a condition drops only the link; the condition stays")
    void editRemovesOnlyTheLink() throws Exception {
        lock(3);
        requireSuccess(putUseCase(3, withPreconditions(fetchUseCase(3), entry(4, LOGGED_IN), entry(5, MEMBER))));

        this.entityManager.flush();
        this.entityManager.clear();
        assertThat(countLinks(3, 6)).isZero();
        assertThat(this.entityManager.createNativeQuery("select count(*) from requirement_artifact where id = 6").getSingleResult())
                .isEqualTo(1L);
    }

    @Test
    @DisplayName("The wording sent with an unshared condition is ignored: the save succeeds and the stored wording stands")
    void editIgnoresTheWordingOfAnUnsharedCondition() throws Exception {
        lock(3);

        JSONObject data = requireSuccess(putUseCase(3, withPreconditions(fetchUseCase(3), entry(4, LOGGED_IN), entry(5, "Reworded membership"), entry(6, NOT_LOCKED))));

        JSONObject kept = data.getJSONArray("preconditions").getJSONObject(1);
        assertThat(kept.getLong("id")).isEqualTo(5L);
        assertThat(kept.getString("condition")).isEqualTo(MEMBER);
    }

    @Test
    @DisplayName("The wording sent with a shared condition is ignored: the save succeeds and no use case sees a change")
    void editIgnoresTheWordingOfASharedCondition() throws Exception {
        long otherUseCaseId = shareConditionFourWithANewUseCase();
        lock(3);

        JSONObject data = requireSuccess(putUseCase(3, withPreconditions(fetchUseCase(3), entry(4, "Reworded login"), entry(5, MEMBER), entry(6, NOT_LOCKED))));

        JSONObject kept = data.getJSONArray("preconditions").getJSONObject(0);
        assertThat(kept.getLong("id")).isEqualTo(4L);
        assertThat(kept.getString("condition")).isEqualTo(LOGGED_IN);
        assertThat(fetchUseCase(otherUseCaseId).getJSONArray("preconditions").getJSONObject(0).getString("condition")).isEqualTo(LOGGED_IN);
    }

    @Test
    @DisplayName("Extension 6b3: a separate copy is a new condition at the same position; the other use case keeps the original")
    void editMakesASeparateCopy() throws Exception {
        long otherUseCaseId = shareConditionFourWithANewUseCase();
        lock(3);

        JSONObject data = requireSuccess(putUseCase(3, withPreconditions(fetchUseCase(3),
                new JSONObject().put("condition", LOGGED_IN), entry(5, MEMBER), entry(6, NOT_LOCKED))));

        JSONObject copy = data.getJSONArray("preconditions").getJSONObject(0);
        assertThat(copy.getLong("id")).isNotEqualTo(4L);
        assertThat(copy.getString("condition")).isEqualTo(LOGGED_IN);
        assertThat(copy.getJSONArray("otherUseCases").length()).isZero();
        assertThat(idsOf(fetchUseCase(otherUseCaseId).getJSONArray("preconditions"))).containsExactly(4L);
    }

    @Test
    @DisplayName("otherUseCases lists the other use cases referencing a condition, and is empty when unshared")
    void responseListsOtherUseCases() throws Exception {
        long otherUseCaseId = shareConditionFourWithANewUseCase();

        JSONArray preconditions = fetchUseCase(3).getJSONArray("preconditions");

        JSONArray shared = preconditions.getJSONObject(0).getJSONArray("otherUseCases");
        assertThat(shared.length()).isEqualTo(1);
        assertThat(shared.getJSONObject(0).getLong("id")).isEqualTo(otherUseCaseId);
        assertThat(shared.getJSONObject(0).getString("title")).isEqualTo("Condition probe");
        assertThat(preconditions.getJSONObject(1).getJSONArray("otherUseCases").length()).isZero();
    }

    // ---- Refusals ----

    @Test
    @DisplayName("A condition id from another team is refused like an id that names nothing")
    void conditionFromAnotherTeamIsRefusedLikeAnUnknownId() throws Exception {
        long foreignId = fetchFirstArtifactId(2, "STAKEHOLDER", this.studentWoodyToken);
        long unknownId = 999_999L;

        JSONObject foreign = postUseCase(minimalUseCase().put("preconditions", new JSONArray().put(entry(foreignId, "x"))));
        JSONObject unknown = postUseCase(minimalUseCase().put("preconditions", new JSONArray().put(entry(unknownId, "x"))));

        assertThat(foreign.getInt("code")).isEqualTo(StatusCode.NOT_FOUND);
        assertThat(unknown.getInt("code")).isEqualTo(StatusCode.NOT_FOUND);
        assertThat(foreign.getString("message").replace(String.valueOf(foreignId), "<id>"))
                .isEqualTo(unknown.getString("message").replace(String.valueOf(unknownId), "<id>"));
    }

    @Test
    @DisplayName("A repeated id in one list is refused, naming its position")
    void repeatedIdIsRefused() throws Exception {
        JSONObject response = postUseCase(minimalUseCase().put("preconditions", new JSONArray().put(entry(4, LOGGED_IN)).put(entry(4, LOGGED_IN))));

        assertThat(response.getInt("code")).isEqualTo(StatusCode.INVALID_ARGUMENT);
        assertThat(response.getString("message")).isEqualTo("Precondition 2 repeats a condition already in this list.");
    }

    @Test
    @DisplayName("A precondition's id in the postcondition list is refused")
    void conditionOfTheOtherTypeIsRefused() throws Exception {
        JSONObject response = postUseCase(minimalUseCase().put("postconditions", new JSONArray().put(entry(4, LOGGED_IN))));

        assertThat(response.getInt("code")).isEqualTo(StatusCode.INVALID_ARGUMENT);
        assertThat(response.getString("message")).isEqualTo("Postcondition 1 is not a postcondition.");
    }

    @Test
    @DisplayName("An entry with empty wording is refused by body validation")
    void emptyWordingIsRefused() throws Exception {
        JSONObject response = postUseCase(minimalUseCase().put("preconditions", new JSONArray().put(new JSONObject().put("condition", "  "))));

        assertThat(response.getInt("code")).isEqualTo(StatusCode.INVALID_ARGUMENT);
    }

    // ---- Helpers ----

    /** Creates a second team 1 use case that also references precondition 4, and returns its id. */
    private long shareConditionFourWithANewUseCase() throws Exception {
        JSONObject payload = minimalUseCase().put("title", "Condition probe")
                .put("preconditions", new JSONArray().put(entry(4, LOGGED_IN)));
        return requireSuccess(postUseCase(payload)).getLong("id");
    }

    private static JSONObject entry(long id, String wording) throws Exception {
        return new JSONObject().put("id", id).put("condition", wording);
    }

    private static JSONObject withPreconditions(JSONObject useCase, JSONObject... entries) throws Exception {
        JSONObject payload = new JSONObject(useCase.toString());
        JSONArray list = new JSONArray();
        for (JSONObject entry : entries) {
            list.put(entry);
        }
        return payload.put("preconditions", list);
    }

    private JSONObject minimalUseCase() throws Exception {
        return new JSONObject()
                .put("title", "Condition rule probe")
                .put("description", "A minimal use case for checking condition rules.")
                .put("teamId", 1)
                .put("primaryActorId", 1)
                .put("secondaryActorIds", new JSONArray())
                .put("businessRuleIds", new JSONArray())
                .put("trigger", "The student starts the probe.")
                .put("preconditions", new JSONArray())
                .put("postconditions", new JSONArray())
                .put("mainSteps", new JSONArray());
    }

    private static JSONObject requireSuccess(JSONObject response) throws Exception {
        assertThat(response.getBoolean("flag")).as(response.toString()).isTrue();
        return response.getJSONObject("data");
    }

    private JSONObject postUseCase(JSONObject payload) throws Exception {
        return send(post(this.baseUrl + "/teams/1/use-cases"), TestKeys.fillUseCaseKeys(payload, null));
    }

    private JSONObject putUseCase(long useCaseId, JSONObject payload) throws Exception {
        // A payload with no key sends the use case's current one, so the save renames nothing.
        String currentKey = payload.has("artifactKey") ? null : fetchUseCase(useCaseId).getString("artifactKey");
        return send(put(this.baseUrl + "/teams/1/use-cases/" + useCaseId), TestKeys.fillUseCaseKeys(payload, currentKey));
    }

    private JSONObject send(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request, JSONObject payload) throws Exception {
        MvcResult result = this.mockMvc.perform(request
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload.toString())
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andReturn();
        return new JSONObject(result.getResponse().getContentAsString());
    }

    private JSONObject fetchUseCase(long useCaseId) throws Exception {
        MvcResult result = this.mockMvc.perform(get(this.baseUrl + "/teams/1/use-cases/" + useCaseId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andReturn();
        return requireSuccess(new JSONObject(result.getResponse().getContentAsString()));
    }

    private void lock(long useCaseId) throws Exception {
        MvcResult result = this.mockMvc.perform(put(this.baseUrl + "/teams/1/use-cases/" + useCaseId + "/lock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"Condition tests\"}")
                        .header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andReturn();
        requireSuccess(new JSONObject(result.getResponse().getContentAsString()));
    }

    private long fetchFirstArtifactId(int teamId, String type, String token) throws Exception {
        MvcResult result = this.mockMvc.perform(post(this.baseUrl + "/teams/" + teamId + "/requirement-artifacts/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new JSONObject().put("type", type).toString())
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andReturn();
        return new JSONObject(result.getResponse().getContentAsString()).getJSONObject("data").getJSONArray("content").getJSONObject(0).getLong("id");
    }

    private String login(String email) throws Exception {
        MvcResult result = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic(email, "123456"))).andReturn();
        return "Bearer " + new JSONObject(result.getResponse().getContentAsString()).getJSONObject("data").getString("token");
    }

    private int teamOf(long artifactId) {
        this.entityManager.flush();
        return ((Number) this.entityManager.createNativeQuery("select team_team_id from requirement_artifact where id = :id")
                .setParameter("id", artifactId).getSingleResult()).intValue();
    }

    private long countConditions() {
        this.entityManager.flush();
        return ((Number) this.entityManager.createNativeQuery("select count(*) from requirement_artifact where type in ('PRECONDITION', 'POSTCONDITION')")
                .getSingleResult()).longValue();
    }

    private long countLinks(long useCaseId, long conditionId) {
        return ((Number) this.entityManager.createNativeQuery("select count(*) from use_case_precondition where use_case_id = :u and precondition_artifact_id = :c")
                .setParameter("u", useCaseId).setParameter("c", conditionId).getSingleResult()).longValue();
    }

    private static List<String> keysOf(JSONArray conditions) throws Exception {
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < conditions.length(); i++) {
            keys.add(conditions.getJSONObject(i).getString("artifactKey"));
        }
        return keys;
    }

    private static List<String> wordingsOf(JSONArray conditions) throws Exception {
        List<String> wordings = new ArrayList<>();
        for (int i = 0; i < conditions.length(); i++) {
            wordings.add(conditions.getJSONObject(i).getString("condition"));
        }
        return wordings;
    }

    private static List<Long> idsOf(JSONArray conditions) throws Exception {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < conditions.length(); i++) {
            ids.add(conditions.getJSONObject(i).getLong("id"));
        }
        return ids;
    }
}
