package team.projectpulse.system.exception;

/**
 * A requirement artifact create, edit or delete refused for a reason the student can act on: a type with no list
 * home, a missing or wrong home, a home document not created yet (UC-ART-create-artifact 8b), a home document locked
 * for review (8a, BR-review-lock), or an artifact other artifacts still refer to (UC-ART-delete-artifact 4a,
 * BR-deletion-integrity). Mapped to 400; the message is written for the student.
 * <p>
 * A refusal may carry {@code data} for the client, which the handler puts in {@code Result.data}. Only the 4a refusal
 * does: the referrers, so the editor can list them. Every other refusal carries none.
 */
public class RequirementArtifactRejectedException extends RuntimeException {

    // transient: the payload is for the response, never for serialization of the exception.
    private final transient Object data;

    public RequirementArtifactRejectedException(String message) {
        this(message, null);
    }

    public RequirementArtifactRejectedException(String message, Object data) {
        super(message);
        this.data = data;
    }

    public Object getData() {
        return this.data;
    }

}
