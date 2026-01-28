package team.projectpulse.ram.usecase;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class UseCaseMainStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private UseCase useCase;

    private String actor; // optional: "User" / "System" / etc.

    @Lob
    private String actionText;

    /**
     * 0..N extensions branching from this main step.
     */
    @OneToMany(mappedBy = "baseStep", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "extension_index")
    private List<UseCaseExtension> extensions = new ArrayList<>();

    
    public UseCaseMainStep() {

    }

    public UseCaseMainStep(Long id, String actor, String actionText) {
        this.id = id;
        this.actor = actor;
        this.actionText = actionText;
    }

    public UseCaseMainStep(String actor, String actionText) {
        this.actor = actor;
        this.actionText = actionText;
    }

    public void addExtension(UseCaseExtension ext) {
        extensions.add(ext);
        ext.setBaseStep(this);
    }

    public void removeExtension(UseCaseExtension ext) {
        extensions.remove(ext);
        ext.setBaseStep(null);
    }

    public void replaceExtensions(List<UseCaseExtension> newExtensions) {
        this.extensions.clear();
        if (newExtensions != null) {
            newExtensions.forEach(this::addExtension);
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UseCase getUseCase() {
        return useCase;
    }

    public void setUseCase(UseCase useCase) {
        this.useCase = useCase;
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

    public List<UseCaseExtension> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<UseCaseExtension> extensions) {
        this.extensions.clear();
        if (extensions != null) {
            extensions.forEach(extension -> {
                extension.setBaseStep(this);
                this.extensions.add(extension);
            });
        }
    }

}
