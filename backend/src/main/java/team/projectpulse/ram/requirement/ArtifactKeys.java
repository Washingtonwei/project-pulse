package team.projectpulse.ram.requirement;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import team.projectpulse.system.exception.RequirementArtifactRejectedException;

import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The one home of the artifact key format and order (BR-artifact-key-unique, DI-artifact-key-assignment; the Project
 * Glossary's Artifact Key; design: {@code docs/design/art.md}, "Artifact keys"). A key is the type's prefix
 * ({@link ArtifactKeyPrefix}), a hyphen, and a slug: lowercase letters, digits and single hyphens, starting with a
 * letter, at most 40 characters. For a functional requirement:
 * <pre>
 * FR-export-csv        valid
 * FR-export-v2         valid (digits are fine after the first letter)
 * FR-Export            refused: uppercase in the slug
 * FR-2fa-login         refused: the slug starts with a digit, so a key is never a bare number (write FR-two-factor-login)
 * FR--x, FR-x-         refused: an empty part between hyphens, or a trailing hyphen
 * RSK-data-loss        refused for a functional requirement: another type's prefix
 * </pre>
 * Every create and every key rename goes through {@link #problemWith}, in the service, whichever route it came by.
 */
public final class ArtifactKeys {

    /** The longest slug a key may carry. */
    public static final int MAX_SLUG_LENGTH = 40;

    /**
     * The name of the unique index on {@code (team_team_id, artifact_key)}, declared on {@link RequirementArtifact} and
     * created by migration V14. A violation of it means a teammate took the key between the check and the write.
     */
    public static final String KEY_INDEX = "uk_requirement_artifact_team_key";

    private static final Pattern SLUG = Pattern.compile("[a-z][a-z0-9]*(-[a-z0-9]+)*");

    // A key split into its prefix and the rest: "FR-export-csv" -> ("FR", "export-csv"). The prefix is letters only, so the
    // first hyphen ends it.
    private static final Pattern KEY = Pattern.compile("([A-Z]+)-(.*)");

    private ArtifactKeys() {
    }

    /** Whether {@code slug} alone (no prefix) has the slug format, for example {@code export-csv} but not {@code 1abc}. */
    public static boolean isSlug(String slug) {
        return slug != null && slug.length() <= MAX_SLUG_LENGTH && SLUG.matcher(slug).matches();
    }

    /**
     * What is wrong with {@code key} as the artifact key of an artifact of {@code type}, written for the student, or
     * empty when nothing is. For a functional requirement, {@code "FR-export-csv"} gives empty, {@code null} and
     * {@code "FR-Export"} give the format message, and {@code "RSK-data-loss"} gives "The artifact key of a functional
     * requirement starts with FR-."
     */
    public static Optional<String> problemWith(RequirementArtifactType type, String key) {
        // The examples below are for a functional requirement, whose prefix is FR.
        String prefix = ArtifactKeyPrefix.of(type);

        // 1. No key at all: null (a request that left artifactKey out), "" or "   " (an empty key box). Each gets the
        //    format message, "The artifact key must be FR- followed by a name of ...".
        if (key == null || key.isBlank()) {
            return Optional.of(formatMessage(prefix));
        }

        // Split the key at its first hyphen into an uppercase prefix and the rest:
        //   "FR-export-csv"  -> ("FR", "export-csv")
        //   "RSK-data-loss"  -> ("RSK", "data-loss")
        //   "FR--x"          -> ("FR", "-x")
        //   "fr-export", "FRexport", "-export"  -> no match (a lowercase prefix, no hyphen, no prefix)
        Matcher matcher = KEY.matcher(key);

        // 2. Another type's prefix: "RSK-data-loss" or "UC-export-csv" on a functional requirement gives "The artifact
        //    key of a functional requirement starts with FR-." It is checked before the slug, so "RSK-Data" gets this
        //    message too: the wrong prefix is the first thing to fix.
        if (matcher.matches() && !matcher.group(1).equals(prefix)) {
            return Optional.of("The artifact key of " + withArticle(label(type)) + " starts with " + prefix + "-.");
        }

        // 3. The right prefix but a slug out of format, or no prefix the pattern recognizes. Each gets the format
        //    message:
        //   "FR-Export"     uppercase in the slug
        //   "FR-2fa-login"  the slug starts with a digit
        //   "FR--x"         an empty part between hyphens (the slug is "-x")
        //   "FR-x-"         a trailing hyphen
        //   "FR-" + 41 letters  longer than 40 characters
        //   "fr-export"     no match: the prefix is lowercase
        if (!matcher.matches() || !isSlug(matcher.group(2))) {
            return Optional.of(formatMessage(prefix));
        }

        // "FR-export-csv": the right prefix and a well-formed slug, so nothing is wrong.
        return Optional.empty();
    }

    /**
     * Refuses {@code key} for an artifact of {@code type} with {@link #problemWith}'s message (UC-ART-create-artifact 4a,
     * UC-ART-edit-artifact 6a, and their glossary and use case siblings).
     *
     * @throws RequirementArtifactRejectedException (400) when the key is missing or malformed
     */
    public static void requireValid(RequirementArtifactType type, String key) {
        problemWith(type, key).ifPresent(message -> {
            throw new RequirementArtifactRejectedException(message);
        });
    }

    /**
     * Whether {@code exception} is a violation of {@link #KEY_INDEX} and nothing else. MySQL names the index with its
     * table in front ({@code requirement_artifact.uk_requirement_artifact_team_key}), so the name is matched by suffix.
     * Any other integrity error, a foreign key for example, gives false and is left to propagate.
     */
    public static boolean isKeyIndexViolation(DataIntegrityViolationException exception) {
        Throwable cause = exception.getCause();
        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation) {
                String name = violation.getConstraintName();
                return name != null && name.toLowerCase(Locale.ROOT).endsWith(KEY_INDEX);
            }
            cause = cause.getCause();
        }
        return false;
    }

    /**
     * Artifact keys in alphabetical order, a null key last. Keys are names, so alphabetical is how a student reads
     * them: the prefix groups them by type, and the slug orders them within it. For example:
     * <pre>
     * [UC-create-use-case, FR-input-validation, null, FR-export-csv, BO-requirements-quality]
     *   -> [BO-requirements-quality, FR-export-csv, FR-input-validation, UC-create-use-case, null]
     * </pre>
     */
    public static final Comparator<String> KEY_ORDER = Comparator.nullsLast(Comparator.naturalOrder());

    private static String formatMessage(String prefix) {
        return "The artifact key must be " + prefix + "- followed by a name of lowercase letters, digits and single hyphens, "
                + "starting with a letter, at most " + MAX_SLUG_LENGTH + " characters.";
    }

    // FUNCTIONAL_REQUIREMENT -> "functional requirement", for messages written to the student.
    static String label(RequirementArtifactType type) {
        return type.name().toLowerCase(Locale.ROOT).replace('_', ' ');
    }

    // "an" before a vowel sound. Every type label that starts with "u" (use case, user story, user interface) starts
    // with a "you" sound, so "u" takes "a": "a use case", not "an use case".
    static String withArticle(String noun) {
        return ("aeio".indexOf(noun.charAt(0)) >= 0 ? "an " : "a ") + noun;
    }

}
