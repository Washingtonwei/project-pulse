package team.projectpulse.system.exception;

/**
 * A requirement artifact create, edit or delete refused for a reason the student can act on: a type with no list
 * home, a missing or wrong home, a home document not created yet (UC-ART-create-artifact 8b), a home document locked
 * for review (8a, BR-review-lock), an artifact other artifacts still refer to (UC-ART-delete-artifact 4a,
 * BR-deletion-integrity), or an artifact key that is malformed or held by another artifact of the team (4a, 4b, 8c,
 * BR-artifact-key-unique). Mapped to 400; the message is written for the student.
 * <p>
 * A refusal may carry {@code data} for the client, which the handler puts in {@code Result.data}. Two do: the delete's
 * 4a refusal carries the referrers, so the editor can list them, and a key in use carries its holder, so the editor
 * can offer to open it (UC-ART-create-artifact 4b, 8c, and their siblings). Every other refusal carries none.
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

    /**
     * A refusal that replaces a lower-level failure, kept as the cause: a key taken by a teammate between the check and
     * the write is refused by the unique key index, and that violation becomes this refusal (UC-ART-create-artifact
     * 8c), with no data.
     */
    public RequirementArtifactRejectedException(String message, Object data, Throwable cause) {
        super(message, cause);
        this.data = data;
    }

    public Object getData() {
        return this.data;
    }

}
