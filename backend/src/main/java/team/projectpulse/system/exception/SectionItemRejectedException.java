package team.projectpulse.system.exception;

/**
 * A document section save refused one of its items: a new item of a type the document section does not admit
 * (BR-artifact-home), or an id repeated within one save. Mapped to 400; the message is written for the student.
 */
public class SectionItemRejectedException extends RuntimeException {

    public SectionItemRejectedException(String message) {
        super(message);
    }

}
