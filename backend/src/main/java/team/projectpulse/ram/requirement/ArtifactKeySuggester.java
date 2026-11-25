package team.projectpulse.ram.requirement;

import org.springframework.stereotype.Service;
import team.projectpulse.ram.llm.LlmProxy;
import team.projectpulse.ram.llm.LlmRequest;
import team.projectpulse.ram.llm.LlmResult;
import team.projectpulse.ram.requirement.dto.ArtifactKeySuggestionDto;
import team.projectpulse.system.exception.RequirementArtifactRejectedException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.Locale;

/**
 * Suggests an artifact key when the student presses Suggest (UC-ART-create-artifact, "Artifact key rules"; design:
 * {@code docs/design/art.md}, "Artifact keys"). It is the AI proxy's first caller, and not an assistant, so
 * FR-AI-enablement does not apply. Every type uses it the same way: a glossary term sends its term as the title and
 * its definition as the content, a use case its name and description, a condition its wording as content.
 * <p>
 * It sends the LLM only the type's label, the title and the content, each cut at 2,000 characters
 * (SEC-llm-data-minimization): no team, no user, and no other artifact or key. It reads back one slug and cleans it up
 * ({@link #normalize}), then adds the type's prefix. A slug still not in the key format, and every {@code Unavailable}
 * answer, give no key (FR-AI-degradation); the student names the artifact herself. For example, for a functional
 * requirement titled "Data export" whose content says the system lets the client export project data as a CSV file,
 * an answer {@code {"slug": "export-csv"}} gives {@code FR-export-csv}.
 * <p>
 * It keeps no state: a suggestion reserves nothing, so whether the key is free is the key-holder lookup's answer, and
 * a teammate taking it first is caught when the artifact is saved (8c).
 */
@Service
// final: the constructor parses the response schema, which can throw, and a final class cannot be subclassed to catch a
// half-built instance (SpotBugs CT_CONSTRUCTOR_THROW). Nothing proxies it, since it has no transactional method.
public final class ArtifactKeySuggester {

    static final String CALLER = "artifact-key-suggestion";

    /** Long enough for the 1 to 4 s the first live calls took; a late suggestion costs the student nothing. */
    static final Duration TIMEOUT = Duration.ofSeconds(5);

    /** A slug is a few words; the live calls answered in about 20 tokens. */
    static final int MAX_OUTPUT_TOKENS = 50;

    /** How much of the title and of the content is sent, each. */
    static final int MAX_INPUT_CHARACTERS = 2000;

    static final String INSTRUCTIONS = """
            You name requirement artifacts in a requirements authoring tool used by student software teams. \
            You are given one artifact's type, title and content. Reply with a slug that names the artifact's concept: \
            2 to 5 lowercase English words joined by single hyphens, using only the letters a to z and the digits 0 to 9, \
            starting with a letter, at most 40 characters. Name what the artifact is about, in the words a reader of the \
            requirements would use. Do not repeat the artifact type, and do not add a prefix, a number, or a word such as \
            "requirement" or "feature". Examples: a functional requirement whose content says the system lets the client \
            export project data as a CSV file: export-csv. A business objective about reducing the time instructors \
            spend grading: reduce-grading-time. A use case named "Submit a weekly activity report": submit-report. \
            A glossary term "Peer Evaluation": peer-evaluation. A precondition "The student is logged into the system.": \
            student-logged-in.""";

    static final String RESPONSE_SCHEMA = """
            {"type": "object", "properties": {"slug": {"type": "string"}}, "required": ["slug"], "additionalProperties": false}""";

    private final LlmProxy llmProxy;
    private final JsonNode responseSchema;

    public ArtifactKeySuggester(LlmProxy llmProxy, JsonMapper jsonMapper) {
        this.llmProxy = llmProxy;
        this.responseSchema = jsonMapper.readTree(RESPONSE_SCHEMA);
    }

    /**
     * A key for an artifact of {@code type} with {@code title} and {@code content}, or none.
     *
     * @throws RequirementArtifactRejectedException (400) when both the title and the content are blank, so there is
     *                                              nothing to suggest a key from
     */
    public ArtifactKeySuggestionDto suggest(RequirementArtifactType type, String title, String content) {
        if (isBlank(title) && isBlank(content)) {
            throw new RequirementArtifactRejectedException("Enter a title or content first, so there is something to suggest a key from.");
        }
        LlmRequest request = new LlmRequest(CALLER, INSTRUCTIONS, input(type, title, content), this.responseSchema, MAX_OUTPUT_TOKENS, TIMEOUT);
        return switch (this.llmProxy.complete(request)) {
            case LlmResult.Unavailable unavailable -> new ArtifactKeySuggestionDto(null, false);
            case LlmResult.Answered answered -> {
                JsonNode slug = answered.json().path("slug");
                String normalized = slug.isString() ? normalize(type, slug.asString()) : null;
                yield new ArtifactKeySuggestionDto(normalized == null ? null : ArtifactKeyPrefix.of(type) + "-" + normalized, true);
            }
        };
    }

    // What the LLM reads, and all it reads. A blank title or content is left out rather than sent empty:
    //   Type: functional requirement
    //   Title: Data export
    //   Content: The system shall let the client export project data as a CSV file.
    static String input(RequirementArtifactType type, String title, String content) {
        StringBuilder input = new StringBuilder("Type: ").append(ArtifactKeys.label(type));
        if (!isBlank(title)) {
            input.append("\nTitle: ").append(cut(title.strip()));
        }
        if (!isBlank(content)) {
            input.append("\nContent: ").append(cut(content.strip()));
        }
        return input.toString();
    }

    /**
     * Turns the model's answer into a slug, or null when it cannot. Lowercased; every run of other characters becomes
     * one hyphen; a prefix the model repeated (such as {@code fr-} for a functional requirement) and anything before
     * the first letter are dropped; then trimmed to 40 characters at the last hyphen within them, or cut at 40 when
     * there is none. For a functional requirement:
     * <pre>
     * "export-csv"         -> "export-csv"
     * "Export CSV!!"       -> "export-csv"
     * "FR-export-csv"      -> "export-csv"
     * "3d-model-export"    -> "d-model-export"
     * "!!!"                -> null
     * </pre>
     */
    static String normalize(RequirementArtifactType type, String answer) {
        String slug = answer.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
        String echoedPrefix = ArtifactKeyPrefix.of(type).toLowerCase(Locale.ROOT) + "-";
        if (slug.startsWith(echoedPrefix)) {
            slug = slug.substring(echoedPrefix.length());
        }
        slug = slug.replaceAll("^[^a-z]+", "").replaceAll("-+$", "");
        if (slug.length() > ArtifactKeys.MAX_SLUG_LENGTH) {
            int lastHyphen = slug.lastIndexOf('-', ArtifactKeys.MAX_SLUG_LENGTH);
            slug = lastHyphen > 0 ? slug.substring(0, lastHyphen) : slug.substring(0, ArtifactKeys.MAX_SLUG_LENGTH);
            slug = slug.replaceAll("-+$", "");
        }
        return ArtifactKeys.isSlug(slug) ? slug : null;
    }

    private static String cut(String text) {
        return text.length() <= MAX_INPUT_CHARACTERS ? text : text.substring(0, MAX_INPUT_CHARACTERS);
    }

    private static boolean isBlank(String text) {
        return text == null || text.isBlank();
    }

}
