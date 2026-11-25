package team.projectpulse.ram;

import team.projectpulse.ram.requirement.ArtifactKeyPrefix;
import team.projectpulse.ram.requirement.RequirementArtifactType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Artifact keys for tests that create artifacts and do not test keys themselves. Every create now needs a key the
 * student chose (BR-artifact-key-unique), so these give one the way a student might: the type's prefix and the title
 * in kebab case. For example:
 * <pre>
 * keyFor("RISK", "Data loss")                       -> "RSK-data-loss"
 * keyFor("FUNCTIONAL_REQUIREMENT", "3D export!")    -> "FR-x3d-export"   (a slug must start with a letter)
 * </pre>
 * Two creates with the same type and title in one test get the same key, and the second is refused as a key in use,
 * exactly as it would be for a student.
 */
public final class TestKeys {

    private TestKeys() {
    }

    public static String keyFor(String type, String title) {
        String prefix;
        try {
            prefix = ArtifactKeyPrefix.of(RequirementArtifactType.valueOf(type));
        } catch (IllegalArgumentException unknownType) {
            // A test sending a type that does not exist (such as the removed OTHER) is about that refusal, not the key.
            prefix = "X";
        }
        return prefix + "-" + slug(title);
    }

    /**
     * Fills the keys a use case save needs and the test left out, as the use case editor would: the use case's own key
     * when the body has none ({@code currentKey} on an update, so the save renames nothing; a key from the title on a
     * create, when {@code currentKey} is null), and a key from its wording for each new condition (no id) that has none.
     * A key the body already has, or {@code JSONObject.NULL}, is left as it is. For example, a create of "Submit report"
     * with a new precondition "The student is logged in." gets {@code UC-submit-report} and
     * {@code PRE-the-student-is-logged-in}.
     */
    public static JSONObject fillUseCaseKeys(JSONObject useCase, String currentKey) throws JSONException {
        if (!useCase.has("artifactKey")) {
            useCase.put("artifactKey", currentKey != null ? currentKey : keyFor("USE_CASE", useCase.optString("title")));
        }
        fillConditionKeys(useCase.optJSONArray("preconditions"), "PRECONDITION");
        fillConditionKeys(useCase.optJSONArray("postconditions"), "POSTCONDITION");
        return useCase;
    }

    public static String fillUseCaseKeys(String useCaseJson, String currentKey) throws JSONException {
        return fillUseCaseKeys(new JSONObject(useCaseJson), currentKey).toString();
    }

    private static void fillConditionKeys(JSONArray conditions, String type) throws JSONException {
        if (conditions == null) {
            return;
        }
        for (int i = 0; i < conditions.length(); i++) {
            JSONObject condition = conditions.getJSONObject(i);
            boolean isNew = !condition.has("id") || condition.isNull("id");
            if (isNew && !condition.has("artifactKey")) {
                condition.put("artifactKey", keyFor(type, condition.optString("condition")));
            }
        }
    }

    public static String slug(String text) {
        String slug = text == null ? "" : text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
        if (slug.isEmpty() || !Character.isLetter(slug.charAt(0))) {
            slug = "x" + slug;
        }
        if (slug.length() > 40) {
            slug = slug.substring(0, 40).replaceAll("-+$", "");
        }
        return slug;
    }

}
