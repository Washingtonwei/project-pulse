package team.projectpulse.system.exception;

/**
 * A requirement artifact create or edit refused for a reason the student can act on: a type with no list home, a
 * missing or wrong home, a home document not created yet (UC-ART-create-artifact 8b), or a home document locked for
 * review (8a, BR-review-lock). Mapped to 400; the message is written for the student.
 */
public class RequirementArtifactRejectedException extends RuntimeException {

    public RequirementArtifactRejectedException(String message) {
        super(message);
    }

}
