package team.projectpulse.ram.requirement.dto;

/**
 * The answer to a press of Suggest (UC-ART-create-artifact, "Artifact key rules"). Three shapes, each telling the key
 * field what to say:
 * <pre>
 * {"artifactKey": "FR-export-csv", "aiAvailable": true}    a suggestion: it replaces the slug box
 * {"artifactKey": null,            "aiAvailable": true}    the model answered, but with nothing usable: "No suggestion this time."
 * {"artifactKey": null,            "aiAvailable": false}   the AI proxy is unavailable: "AI suggestions are temporarily unavailable. Name it yourself."
 * </pre>
 *
 * @param artifactKey the suggested key, with the type's prefix, or null
 * @param aiAvailable false when the AI proxy answered {@code Unavailable} (FR-AI-degradation, AVL-llm-degradation)
 */
public record ArtifactKeySuggestionDto(String artifactKey, boolean aiAvailable) {
}
