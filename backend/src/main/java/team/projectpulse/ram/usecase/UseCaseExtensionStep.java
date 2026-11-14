package team.projectpulse.ram.usecase;

import jakarta.persistence.*;

@Entity
public class UseCaseExtensionStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private UseCaseExtension extension;

    private String actor;

    @Lob
    private String actionText;


    public UseCaseExtensionStep() {
    }

    public UseCaseExtensionStep(String actor, String actionText) {
        this.actor = actor;
        this.actionText = actionText;
    }

    public UseCaseExtensionStep(Long id, String actor, String actionText) {
        this.id = id;
        this.actor = actor;
        this.actionText = actionText;
    }

    /**
     * Copies a submitted extension step's content onto this existing one, keeping its id.
     */
    void reviseFrom(UseCaseExtensionStep submitted) {
        this.actor = submitted.getActor();
        this.actionText = submitted.getActionText();
    }

    /**
     * Builds a new extension step, without an id, with the submitted step's content.
     */
    static UseCaseExtensionStep copyOf(UseCaseExtensionStep submitted) {
        return new UseCaseExtensionStep(submitted.getActor(), submitted.getActionText());
    }

    // getters/setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UseCaseExtension getExtension() {
        return extension;
    }

    public void setExtension(UseCaseExtension extension) {
        this.extension = extension;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getActionText() {
        return actionText;
    }

    public void setActionText(String actionText) {
        this.actionText = actionText;
    }

}
