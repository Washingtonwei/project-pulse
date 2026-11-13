package team.projectpulse.ram.usecase;

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
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.requirement.RequirementArtifactType;
import team.projectpulse.system.StatusCode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Characterization tests for how the use-case step lists are ordered in the database, and for the editing scenarios
 * the use-case editor can produce on save (see {@link UseCase#reviseMainSteps}).
 *
 * <p>{@code UseCase.mainSteps}, {@code UseCaseMainStep.extensions} and {@code UseCaseExtension.steps} are the
 * inverse ({@code mappedBy}) side of their associations and carry {@code @OrderColumn}. Hibernate 7 logs HHH160246
 * for that mapping, and we keep it deliberately: the Hibernate 7.4 User Guide ("Mapping Lists") says the annotation
 * "must be applied explicitly" to retain the order of a {@code mappedBy} list. With it, Hibernate inserts each
 * child without the index and then writes the index from the parent's list position with an {@code UPDATE}.
 *
 * <p>These tests pin that behavior down so that a Hibernate upgrade which stops writing the index fails the build
 * rather than production, where a NULL index makes loading the use case throw ("Illegal null value for list
 * index"). Mapping the index column on the child entity has exactly that effect, which is how these tests were
 * checked to fail. Every test flushes and clears the persistence context before
 * asserting and reads the index columns with native SQL, because {@link AbstractIntegrationTest} wraps each test in
 * a transaction: without the flush and clear, an assertion would read the in-memory list and prove nothing about
 * the database.
 */
class UseCaseStepOrderingIntegrationTest extends AbstractIntegrationTest {

    private static final String SEEDED_USE_CASE_TITLE = "Create a use case";

    private static final String MAIN_STEP_ORDER_SQL =
            "select id, main_step_index from use_case_main_step where use_case_artifact_id = :parentId order by main_step_index";

    private static final String EXTENSION_ORDER_SQL =
            "select id, extension_index from use_case_extension where base_step_id = :parentId order by extension_index";

    private static final String EXTENSION_STEP_ORDER_SQL =
            "select id, extension_step_index from use_case_extension_step where extension_id = :parentId order by extension_step_index";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    EntityManager entityManager;

    @Autowired
    UseCaseRepository useCaseRepository;

    @Autowired
    UseCaseService useCaseService;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    String studentJohnToken; // John is on team 1, which owns the seeded use case

    Long seededUseCaseId;


    @BeforeEach
    void setUp() throws Exception {
        MvcResult login = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("j.smith@abc.edu", "123456"))).andReturn();
        this.studentJohnToken = "Bearer " + new JSONObject(login.getResponse().getContentAsString()).getJSONObject("data").getString("token");

        this.seededUseCaseId = this.useCaseRepository.findAll().stream()
                .filter(useCase -> SEEDED_USE_CASE_TITLE.equals(useCase.getArtifact().getTitle()))
                .map(UseCase::getId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No seeded use case titled '" + SEEDED_USE_CASE_TITLE + "'"));
    }

    @Test
    @DisplayName("A new use case stores its steps, extensions and extension steps at indexes 0..n-1 in list order")
    void testCreatedUseCaseStoresContiguousIndexes() {
        UseCase seeded = loadSeededUseCase();

        UseCase useCase = new UseCase();
        useCase.setArtifact(new RequirementArtifact(seeded.getArtifact().getTeam(), RequirementArtifactType.USE_CASE, "Ordering probe", "A use case created to check step order.", ""));
        useCase.setPrimaryActor(seeded.getPrimaryActor());
        useCase.setUseCaseTrigger("The Student starts the ordering probe.");
        for (int i = 0; i < 4; i++) {
            useCase.addMainStep(new UseCaseMainStep("System", "Step " + i));
        }
        UseCaseExtension extension = new UseCaseExtension("Something goes wrong", ExtensionKind.EXCEPTION, ExtensionExit.RESUME);
        extension.addStep(new UseCaseExtensionStep("System", "Extension step 0"));
        extension.addStep(new UseCaseExtensionStep("System", "Extension step 1"));
        useCase.getMainSteps().get(2).addExtension(extension);

        Long useCaseId = this.useCaseService.saveUseCase(1, useCase).getId();
        flushAndClear();

        UseCase reloaded = this.useCaseRepository.findByIdWithStepGraph(useCaseId).orElseThrow();
        assertThat(reloaded.getMainSteps()).extracting(UseCaseMainStep::getActionText)
                .containsExactly("Step 0", "Step 1", "Step 2", "Step 3");
        assertThat(idsInIndexOrder(MAIN_STEP_ORDER_SQL, useCaseId)).hasSize(4);

        UseCaseExtension reloadedExtension = reloaded.getMainSteps().get(2).getExtensions().get(0);
        assertThat(reloadedExtension.getSteps()).extracting(UseCaseExtensionStep::getActionText)
                .containsExactly("Extension step 0", "Extension step 1");
        assertThat(idsInIndexOrder(EXTENSION_STEP_ORDER_SQL, reloadedExtension.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Inserting a step in the middle renumbers the list and keeps every existing step id")
    void testInsertingAStepRenumbersAndKeepsIds() {
        UseCase useCase = loadSeededUseCase();
        List<Long> idsBefore = stepIds(useCase);

        UseCaseMainStep inserted = new UseCaseMainStep("System", "Inserted step");
        useCase.getMainSteps().add(1, inserted);
        inserted.setUseCase(useCase);
        flushAndClear();

        List<Long> idsAfter = idsInIndexOrder(MAIN_STEP_ORDER_SQL, this.seededUseCaseId);
        assertThat(idsAfter).hasSize(idsBefore.size() + 1);
        assertThat(idsAfter.get(0)).isEqualTo(idsBefore.get(0));
        assertThat(idsAfter.subList(2, idsAfter.size())).containsExactlyElementsOf(idsBefore.subList(1, idsBefore.size()));
        assertThat(actionTextAt(1)).isEqualTo("Inserted step");
    }

    @Test
    @DisplayName("Removing a middle step closes the gap and deletes its extensions and their steps")
    void testRemovingAStepClosesTheGapAndCascades() {
        UseCase useCase = loadSeededUseCase();
        List<Long> idsBefore = stepIds(useCase);
        UseCaseMainStep stepWithExtension = useCase.getMainSteps().stream()
                .filter(step -> !step.getExtensions().isEmpty())
                .findFirst()
                .orElseThrow(() -> new AssertionError("The seeded use case has no step with an extension"));
        Long removedStepId = stepWithExtension.getId();
        Long removedExtensionId = stepWithExtension.getExtensions().get(0).getId();

        useCase.removeMainStep(stepWithExtension);
        flushAndClear();

        List<Long> expected = new ArrayList<>(idsBefore);
        expected.remove(removedStepId);
        assertThat(idsInIndexOrder(MAIN_STEP_ORDER_SQL, this.seededUseCaseId)).containsExactlyElementsOf(expected);
        assertThat(countRows("select count(*) from use_case_extension where base_step_id = :id", removedStepId)).isZero();
        assertThat(countRows("select count(*) from use_case_extension_step where extension_id = :id", removedExtensionId)).isZero();
    }

    @Test
    @DisplayName("Moving the last step to the front stores the new order and keeps every step id")
    void testMovingAStepStoresTheNewOrder() {
        UseCase useCase = loadSeededUseCase();
        List<Long> idsBefore = stepIds(useCase);

        List<UseCaseMainStep> steps = useCase.getMainSteps();
        UseCaseMainStep last = steps.remove(steps.size() - 1);
        steps.add(0, last);
        flushAndClear();

        List<Long> expected = new ArrayList<>(idsBefore);
        expected.add(0, expected.remove(expected.size() - 1));
        assertThat(idsInIndexOrder(MAIN_STEP_ORDER_SQL, this.seededUseCaseId)).containsExactlyElementsOf(expected);
    }

    @Test
    @DisplayName("Each step numbers its extensions from 0, and each extension numbers its steps from 0")
    void testNestedListsAreNumberedPerParent() {
        UseCase useCase = loadSeededUseCase();
        UseCaseMainStep step = firstStepWithExtension(useCase);
        Long existingExtensionId = step.getExtensions().get(0).getId();

        UseCaseExtension second = new UseCaseExtension("A second thing goes wrong", ExtensionKind.ALTERNATE, ExtensionExit.RESUME);
        second.addStep(new UseCaseExtensionStep("System", "Second extension step 0"));
        second.addStep(new UseCaseExtensionStep("System", "Second extension step 1"));
        second.addStep(new UseCaseExtensionStep("System", "Second extension step 2"));
        step.addExtension(second);
        Long stepId = step.getId();
        flushAndClear();

        List<Long> extensionIds = idsInIndexOrder(EXTENSION_ORDER_SQL, stepId);
        assertThat(extensionIds).hasSize(2);
        assertThat(extensionIds.get(0)).isEqualTo(existingExtensionId);
        assertThat(idsInIndexOrder(EXTENSION_STEP_ORDER_SQL, existingExtensionId)).hasSize(2);
        assertThat(idsInIndexOrder(EXTENSION_STEP_ORDER_SQL, extensionIds.get(1))).hasSize(3);
    }

    @Test
    @DisplayName("The nested step-graph fetch returns each step once, in index order, with its extensions in order")
    void testStepGraphFetchHasNoDuplicatesAndKeepsOrder() {
        UseCase useCase = loadSeededUseCase();
        UseCaseMainStep step = firstStepWithExtension(useCase);
        int stepPosition = useCase.getMainSteps().indexOf(step);
        UseCaseExtension second = new UseCaseExtension("A second thing goes wrong", ExtensionKind.ALTERNATE, ExtensionExit.RESUME);
        second.addStep(new UseCaseExtensionStep("System", "Only step"));
        step.addExtension(second);
        flushAndClear();

        // The join repeats this step once per extension, and an indexed list puts both rows in the same slot.
        UseCase reloaded = this.useCaseRepository.findByIdWithStepGraph(this.seededUseCaseId).orElseThrow();
        List<Long> fetchedIds = stepIds(reloaded);
        assertThat(fetchedIds).doesNotHaveDuplicates();
        assertThat(fetchedIds).containsExactlyElementsOf(idsInIndexOrder(MAIN_STEP_ORDER_SQL, this.seededUseCaseId));
        assertThat(reloaded.getMainSteps().get(stepPosition).getExtensions())
                .extracting(UseCaseExtension::getConditionText)
                .endsWith("A second thing goes wrong")
                .hasSize(2);
    }

    @Test
    @DisplayName("A reordered PUT stores the payload order and keeps the ids of the steps it moves")
    void testUpdateThroughTheApiStoresThePayloadOrder() throws Exception {
        List<Long> idsBefore = stepIds(loadSeededUseCase());
        flushAndClear();

        JSONObject payload = lockAndFetchUseCase();
        JSONArray steps = payload.getJSONArray("mainSteps");
        JSONArray reordered = new JSONArray();
        reordered.put(steps.get(steps.length() - 1));
        for (int i = 0; i < steps.length() - 1; i++) {
            reordered.put(steps.get(i));
        }
        payload.put("mainSteps", reordered);
        putUseCase(payload);
        flushAndClear();

        List<Long> expected = new ArrayList<>(idsBefore);
        expected.add(0, expected.remove(expected.size() - 1));
        assertThat(idsInIndexOrder(MAIN_STEP_ORDER_SQL, this.seededUseCaseId)).containsExactlyElementsOf(expected);
    }

    @Test
    @DisplayName("A PUT that inserts one step and omits another stores the new step in place and deletes the omitted one")
    void testUpdateThroughTheApiInsertsAndDeletes() throws Exception {
        UseCase before = loadSeededUseCase();
        List<Long> idsBefore = stepIds(before);
        UseCaseMainStep omitted = firstStepWithExtension(before);
        Long omittedId = omitted.getId();
        Long omittedExtensionId = omitted.getExtensions().get(0).getId();
        flushAndClear();

        JSONObject payload = lockAndFetchUseCase();
        JSONArray steps = payload.getJSONArray("mainSteps");
        JSONArray edited = new JSONArray();
        for (int i = 0; i < steps.length(); i++) {
            JSONObject step = steps.getJSONObject(i);
            if (step.getLong("id") == omittedId) {
                continue;
            }
            edited.put(step);
            if (i == 0) {
                edited.put(new JSONObject().put("actor", "System").put("actionText", "Inserted through the API").put("extensions", new JSONArray()));
            }
        }
        payload.put("mainSteps", edited);
        putUseCase(payload);
        flushAndClear();

        List<Long> idsAfter = idsInIndexOrder(MAIN_STEP_ORDER_SQL, this.seededUseCaseId);
        List<Long> kept = new ArrayList<>(idsBefore);
        kept.remove(omittedId);
        assertThat(idsAfter).hasSize(kept.size() + 1);
        assertThat(idsAfter.get(0)).isEqualTo(kept.get(0));
        assertThat(idsAfter.subList(2, idsAfter.size())).containsExactlyElementsOf(kept.subList(1, kept.size()));
        assertThat(idsBefore).doesNotContain(idsAfter.get(1));
        assertThat(actionTextAt(1)).isEqualTo("Inserted through the API");
        assertThat(countRows("select count(*) from use_case_main_step where id = :id", omittedId)).isZero();
        assertThat(countRows("select count(*) from use_case_extension_step where extension_id = :id", omittedExtensionId)).isZero();
    }


    @Test
    @DisplayName("Two saves in a row, as autosave does, both land and keep every step id")
    void testTwoSavesInARow() throws Exception {
        List<Long> idsBefore = stepIds(loadSeededUseCase());
        flushAndClear();

        JSONObject first = lockAndFetchUseCase();
        first.getJSONArray("mainSteps").getJSONObject(0).put("actionText", "First save");
        putUseCase(first);
        flushAndClear(); // each request commits on its own in production

        JSONObject second = lockAndFetchUseCase();
        assertThat(second.getJSONArray("mainSteps").getJSONObject(0).getString("actionText")).isEqualTo("First save");
        second.getJSONArray("mainSteps").getJSONObject(1).put("actionText", "Second save");
        putUseCase(second);
        flushAndClear();

        assertThat(idsInIndexOrder(MAIN_STEP_ORDER_SQL, this.seededUseCaseId)).containsExactlyElementsOf(idsBefore);
        assertThat(actionTextAt(0)).isEqualTo("First save");
        assertThat(actionTextAt(1)).isEqualTo("Second save");
    }

    @Test
    @DisplayName("Removing a whole extension deletes it and its steps, and keeps the step it branched from")
    void testRemovingAnExtensionKeepsItsStep() throws Exception {
        UseCaseMainStep step = firstStepWithExtension(loadSeededUseCase());
        Long stepId = step.getId();
        Long extensionId = step.getExtensions().get(0).getId();
        flushAndClear();

        JSONObject payload = lockAndFetchUseCase();
        stepById(payload, stepId).put("extensions", new JSONArray());
        putUseCase(payload);
        flushAndClear();

        assertThat(countRows("select count(*) from use_case_main_step where id = :id", stepId)).isOne();
        assertThat(countRows("select count(*) from use_case_extension where id = :id", extensionId)).isZero();
        assertThat(countRows("select count(*) from use_case_extension_step where extension_id = :id", extensionId)).isZero();
    }

    @Test
    @DisplayName("Changing an extension's condition, kind and exit updates it in place")
    void testEditingAnExtensionUpdatesItInPlace() throws Exception {
        UseCaseMainStep step = firstStepWithExtension(loadSeededUseCase());
        Long stepId = step.getId();
        Long extensionId = step.getExtensions().get(0).getId();
        flushAndClear();

        JSONObject payload = lockAndFetchUseCase();
        stepById(payload, stepId).getJSONArray("extensions").getJSONObject(0)
                .put("conditionText", "Edited condition")
                .put("kind", "ALTERNATE")
                .put("exit", "END_FAILURE");
        putUseCase(payload);
        flushAndClear();

        Object[] row = (Object[]) this.entityManager.createNativeQuery(
                        "select condition_text, kind, extension_exit from use_case_extension where id = :id")
                .setParameter("id", extensionId).getSingleResult();
        assertThat(row).containsExactly("Edited condition", "ALTERNATE", "END_FAILURE");
        assertThat(idsInIndexOrder(EXTENSION_ORDER_SQL, stepId)).containsExactly(extensionId);
    }

    @Test
    @DisplayName("Inserting an extension step in the middle renumbers that extension and keeps the existing ids")
    void testInsertingAnExtensionStepInTheMiddle() throws Exception {
        UseCaseMainStep step = firstStepWithExtension(loadSeededUseCase());
        Long stepId = step.getId();
        Long extensionId = step.getExtensions().get(0).getId();
        List<Long> extensionStepIdsBefore = step.getExtensions().get(0).getSteps().stream().map(UseCaseExtensionStep::getId).toList();
        flushAndClear();

        JSONObject payload = lockAndFetchUseCase();
        JSONObject extension = stepById(payload, stepId).getJSONArray("extensions").getJSONObject(0);
        JSONArray extensionSteps = extension.getJSONArray("steps");
        JSONArray withInsert = new JSONArray().put(extensionSteps.get(0))
                .put(new JSONObject().put("actor", "System").put("actionText", "Inserted extension step"));
        for (int i = 1; i < extensionSteps.length(); i++) {
            withInsert.put(extensionSteps.get(i));
        }
        extension.put("steps", withInsert);
        putUseCase(payload);
        flushAndClear();

        List<Long> idsAfter = idsInIndexOrder(EXTENSION_STEP_ORDER_SQL, extensionId);
        assertThat(idsAfter).hasSize(extensionStepIdsBefore.size() + 1);
        assertThat(idsAfter.get(0)).isEqualTo(extensionStepIdsBefore.get(0));
        assertThat(idsAfter.subList(2, idsAfter.size())).containsExactlyElementsOf(extensionStepIdsBefore.subList(1, extensionStepIdsBefore.size()));
        assertThat(extensionStepIdsBefore).doesNotContain(idsAfter.get(1));
    }

    @Test
    @DisplayName("Editing a step's actor and an extension step's actor updates them in place")
    void testEditingActorsUpdatesThemInPlace() throws Exception {
        UseCaseMainStep step = firstStepWithExtension(loadSeededUseCase());
        Long stepId = step.getId();
        Long extensionStepId = step.getExtensions().get(0).getSteps().get(0).getId();
        flushAndClear();

        JSONObject payload = lockAndFetchUseCase();
        JSONObject submittedStep = stepById(payload, stepId).put("actor", "Instructor");
        submittedStep.getJSONArray("extensions").getJSONObject(0).getJSONArray("steps").getJSONObject(0).put("actor", "Instructor");
        putUseCase(payload);
        flushAndClear();

        assertThat(this.entityManager.createNativeQuery("select actor from use_case_main_step where id = :id")
                .setParameter("id", stepId).getSingleResult()).isEqualTo("Instructor");
        assertThat(this.entityManager.createNativeQuery("select actor from use_case_extension_step where id = :id")
                .setParameter("id", extensionStepId).getSingleResult()).isEqualTo("Instructor");
    }

    @Test
    @DisplayName("Removing every step leaves an empty main success scenario and deletes the extensions too")
    void testRemovingEveryStep() throws Exception {
        List<Long> extensionIds = loadSeededUseCase().getMainSteps().stream()
                .flatMap(step -> step.getExtensions().stream()).map(UseCaseExtension::getId).toList();
        assertThat(extensionIds).isNotEmpty();
        flushAndClear();

        JSONObject payload = lockAndFetchUseCase();
        payload.put("mainSteps", new JSONArray());
        putUseCase(payload);
        flushAndClear();

        assertThat(idsInIndexOrder(MAIN_STEP_ORDER_SQL, this.seededUseCaseId)).isEmpty();
        for (Long extensionId : extensionIds) {
            assertThat(countRows("select count(*) from use_case_extension where id = :id", extensionId)).isZero();
        }
    }

    @Test
    @DisplayName("Creating a use case from a payload carrying existing step ids makes fresh steps and leaves the originals alone")
    void testCreateWithExistingStepIdsMakesFreshSteps() throws Exception {
        List<Long> seededIds = stepIds(loadSeededUseCase());
        flushAndClear();

        // The seeded use case exactly as the API serves it, every step and extension carrying its id, posted back
        // as a new use case.
        JSONObject payload = lockAndFetchUseCase();
        payload.put("title", "Copied use case");
        MvcResult result = this.mockMvc.perform(post(this.baseUrl + "/teams/1/use-cases")
                .contentType(MediaType.APPLICATION_JSON).content(payload.toString()).accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.studentJohnToken)).andReturn();
        Long copyId = assertSucceeded(result).getJSONObject("data").getLong("id");
        flushAndClear();

        assertThat(copyId).isNotEqualTo(this.seededUseCaseId);
        List<Long> copyIds = idsInIndexOrder(MAIN_STEP_ORDER_SQL, copyId);
        assertThat(copyIds).hasSameSizeAs(seededIds).doesNotContainAnyElementsOf(seededIds);
        assertThat(idsInIndexOrder(MAIN_STEP_ORDER_SQL, this.seededUseCaseId)).containsExactlyElementsOf(seededIds);
    }

    /**
     * Finds a step in a use-case payload by its id.
     */
    private static JSONObject stepById(JSONObject payload, Long stepId) throws Exception {
        JSONArray steps = payload.getJSONArray("mainSteps");
        for (int i = 0; i < steps.length(); i++) {
            JSONObject step = steps.getJSONObject(i);
            if (step.optLong("id") == stepId) {
                return step;
            }
        }
        throw new AssertionError("No step " + stepId + " in the payload");
    }


    private UseCase loadSeededUseCase() {
        return this.useCaseRepository.findByIdWithStepGraph(this.seededUseCaseId).orElseThrow();
    }

    private static UseCaseMainStep firstStepWithExtension(UseCase useCase) {
        return useCase.getMainSteps().stream()
                .filter(step -> !step.getExtensions().isEmpty())
                .findFirst()
                .orElseThrow(() -> new AssertionError("The seeded use case has no step with an extension"));
    }

    private static List<Long> stepIds(UseCase useCase) {
        return useCase.getMainSteps().stream().map(UseCaseMainStep::getId).toList();
    }

    private void flushAndClear() {
        this.entityManager.flush();
        this.entityManager.clear();
    }

    /**
     * Reads one parent's children straight from the table and returns their ids in index order, after asserting
     * that the indexes are exactly 0..n-1. A NULL index, a gap or a duplicate fails here, in every test.
     */
    private List<Long> idsInIndexOrder(String sql, Long parentId) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = this.entityManager.createNativeQuery(sql).setParameter("parentId", parentId).getResultList();
        List<Integer> indexes = rows.stream().map(row -> row[1] == null ? null : ((Number) row[1]).intValue()).toList();
        assertThat(indexes).as("index column for parent %d", parentId)
                .containsExactlyElementsOf(IntStream.range(0, rows.size()).boxed().toList());
        return rows.stream().map(row -> ((Number) row[0]).longValue()).toList();
    }

    private String actionTextAt(int index) {
        return (String) this.entityManager.createNativeQuery(
                        "select action_text from use_case_main_step where use_case_artifact_id = :parentId and main_step_index = :index")
                .setParameter("parentId", this.seededUseCaseId)
                .setParameter("index", index)
                .getSingleResult();
    }

    private long countRows(String sql, Long id) {
        return ((Number) this.entityManager.createNativeQuery(sql).setParameter("id", id).getSingleResult()).longValue();
    }

    /**
     * Locks the seeded use case as John and returns it exactly as the API serves it, which is also what the
     * frontend sends back on save: every existing step carries its id.
     */
    private JSONObject lockAndFetchUseCase() throws Exception {
        String useCaseUrl = this.baseUrl + "/teams/1/use-cases/" + this.seededUseCaseId;
        assertSucceeded(this.mockMvc.perform(put(useCaseUrl + "/lock").contentType(MediaType.APPLICATION_JSON).content("{\"reason\": \"Ordering test\"}")
                .accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken)).andReturn());
        return assertSucceeded(this.mockMvc.perform(get(useCaseUrl).accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.studentJohnToken)).andReturn()).getJSONObject("data");
    }

    private void putUseCase(JSONObject payload) throws Exception {
        assertSucceeded(this.mockMvc.perform(put(this.baseUrl + "/teams/1/use-cases/" + this.seededUseCaseId)
                .contentType(MediaType.APPLICATION_JSON).content(payload.toString()).accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, this.studentJohnToken)).andReturn());
    }

    private static JSONObject assertSucceeded(MvcResult result) throws Exception {
        JSONObject response = new JSONObject(result.getResponse().getContentAsString());
        assertThat(response.getBoolean("flag")).as(response.toString()).isTrue();
        assertThat(response.getInt("code")).as(response.toString()).isEqualTo(StatusCode.SUCCESS);
        return response;
    }

}
