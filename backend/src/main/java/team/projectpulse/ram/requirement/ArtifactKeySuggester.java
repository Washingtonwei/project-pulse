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

/**
 * Suggests an artifact key when the student presses Suggest (UC-ART-create-artifact, "Artifact key rules"; design:
 * {@code docs/design/art.md}, "Artifact keys"). It is the AI proxy's first caller, and not an assistant, so
 * FR-AI-enablement does not apply. Every type uses it the same way: a glossary term sends its term as the title and
 * its definition as the content, a use case its name and description, a condition its wording as content.
 * <p>
 * It sends the LLM only the type's label, the title and the content, each cut at 2,000 characters
 * (SEC-llm-data-minimization): no team, no user, and no other artifact or key. It reads back one slug and uses it only
 * if it is already in the slug format, exactly as the model wrote it, then adds the type's prefix. It never repairs an
 * answer: one out of format, and every {@code Unavailable} answer, give no key (FR-AI-degradation), and the student
 * names the artifact herself. For example, for a functional
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
            // No answer: Unavailable(NOT_CONFIGURED) in dev and the tests, where no provider is set, or Unavailable(DOWN),
            // TIMEOUT or BAD_OUTPUT when the call fails.
            case LlmResult.Unavailable unavailable -> new ArtifactKeySuggestionDto(null, false);
            case LlmResult.Answered answered -> {
                // answered.json() is the model's whole reply, parsed by the proxy. The strict RESPONSE_SCHEMA asks the
                // provider for one object with exactly one string property, so for the "Data export" example it is:
                //   {"slug": "export-csv"}
                // The proxy checks only that the reply is an object, so the code below still checks the slug is a string.
                JsonNode slug = answered.json().path("slug");
                // The answer as the model wrote it, used only if it is already a slug: "export-csv" is, while
                // "Export CSV", "FR-export-csv" and "2fa-login" are not, and give no suggestion.
                String answer = slug.isString() ? slug.asString() : null;
                String usable = ArtifactKeys.isSlug(answer) ? answer : null;
                // yield gives this case's value to the switch expression (return would leave the whole method).
                yield new ArtifactKeySuggestionDto(usable == null ? null : ArtifactKeyPrefix.of(type) + "-" + usable, true);
            }
        };
    }

    // What the LLM reads, and all it reads: the type's label, then the title and the content, each trimmed and cut at
    // 2,000 characters. A blank title or content is left out rather than sent empty. Two examples:
    //
    //   input(FUNCTIONAL_REQUIREMENT, "Data export", "The system shall let the client export project data as a CSV file.")
    //     Type: functional requirement
    //     Title: Data export
    //     Content: The system shall let the client export project data as a CSV file.
    //
    //   input(PRECONDITION, "", "  The student is logged into the system.  ")   a condition has no title
    //     Type: precondition
    //     Content: The student is logged into the system.
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

    private static String cut(String text) {
        return text.length() <= MAX_INPUT_CHARACTERS ? text : text.substring(0, MAX_INPUT_CHARACTERS);
    }

    private static boolean isBlank(String text) {
        return text == null || text.isBlank();
    }

}
