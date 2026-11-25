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
 * FR-step-2            valid (digits are fine after the first letter)
 * FR-Export            refused: uppercase in the slug
 * FR-1abc              refused: the slug starts with a digit, so a key is never a bare number
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

    // A key split into its prefix and the rest: "FR-step-10" -> ("FR", "step-10"). The prefix is letters only, so the
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
        String prefix = ArtifactKeyPrefix.of(type);
        if (key == null || key.isBlank()) {
            return Optional.of(formatMessage(prefix));
        }
        Matcher matcher = KEY.matcher(key);
        if (matcher.matches() && !matcher.group(1).equals(prefix)) {
            return Optional.of("The artifact key of " + withArticle(label(type)) + " starts with " + prefix + "-.");
        }
        if (!matcher.matches() || !isSlug(matcher.group(2))) {
            return Optional.of(formatMessage(prefix));
        }
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
     * Artifact keys in the order a student reads them: by prefix, then by the rest with runs of digits compared as
     * numbers, so {@code FR-step-2} comes before {@code FR-step-10} (a plain string sort puts "10" first), and other
     * slugs sort alphabetically. A null key sorts last. For example:
     * <pre>
     * [UC-submit-report, FR-step-10, null, FR-step-2, BO-quality]
     *   -> [BO-quality, FR-step-2, FR-step-10, UC-submit-report, null]
     * </pre>
     */
    public static final Comparator<String> NATURAL_ORDER = Comparator.nullsLast(ArtifactKeys::compareNatural);

    private static int compareNatural(String left, String right) {
        int i = 0;
        int j = 0;
        while (i < left.length() && j < right.length()) {
            char a = left.charAt(i);
            char b = right.charAt(j);
            if (Character.isDigit(a) && Character.isDigit(b)) {
                // Compare the two digit runs as numbers: first by length once leading zeros are skipped, then digit by
                // digit, so "2" < "10" and no run can overflow a long.
                int runStartA = i;
                int runStartB = j;
                while (i < left.length() && Character.isDigit(left.charAt(i))) {
                    i++;
                }
                while (j < right.length() && Character.isDigit(right.charAt(j))) {
                    j++;
                }
                String runA = stripLeadingZeros(left.substring(runStartA, i));
                String runB = stripLeadingZeros(right.substring(runStartB, j));
                int byLength = Integer.compare(runA.length(), runB.length());
                if (byLength != 0) {
                    return byLength;
                }
                int byDigits = runA.compareTo(runB);
                if (byDigits != 0) {
                    return byDigits;
                }
            } else {
                if (a != b) {
                    return Character.compare(a, b);
                }
                i++;
                j++;
            }
        }
        return Integer.compare(left.length() - i, right.length() - j);
    }

    private static String stripLeadingZeros(String digits) {
        int k = 0;
        while (k < digits.length() - 1 && digits.charAt(k) == '0') {
            k++;
        }
        return digits.substring(k);
    }

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
