package team.projectpulse.system.exception;

/**
 * A requirement document create refused for a reason the course admin can act on: the team already has a document of
 * that type, and an existing document is never regenerated or overwritten (UC-TPL-provision-documents 3a,
 * BR-document-creation). Mapped to 400; the message is written for the course admin, for example "This team already
 * has the Architectural Design. A team's document is created once and never regenerated or overwritten."
 */
public class RequirementDocumentRejectedException extends RuntimeException {

    public RequirementDocumentRejectedException(String message) {
        super(message);
    }

}
