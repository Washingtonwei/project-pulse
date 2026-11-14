package team.projectpulse.ram.usecase;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class UseCaseExtension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The main step this extension branches from.
     */
    @ManyToOne
    private UseCaseMainStep baseStep;

    /**
     * Label like "2a", "2b" for exporting only.
     * DON'T HARDCODE. We SHOULD compute this from (baseStep.main_step_index + 1 and extension_index + 1).
     */
    private String label;

    /**
     * The condition that triggers this extension.
     * Example: "If payment is declined"
     */
    @Lob
    private String conditionText;

    @Enumerated(EnumType.STRING)
    private ExtensionKind kind = ExtensionKind.ALTERNATE;

    /**
     * How this extension terminates.
     */
    @Enumerated(EnumType.STRING)
    private ExtensionExit extensionExit = ExtensionExit.RESUME;

    /**
     * If exit == RESUME, continue at this main step number.
     * Example: "Then continue at step 5."
     * DON'T HARDCODE. We SHOULD compute this from the baseStep.
     */
    private Integer resumeStepNo;

    /**
     * Steps in this extension (ordered).
     */
    @OneToMany(mappedBy = "extension", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "extension_step_index")
    private List<UseCaseExtensionStep> steps = new ArrayList<>();


    public UseCaseExtension() {

    }

    public UseCaseExtension(String conditionText, ExtensionKind kind, ExtensionExit extensionExit) {
        this.conditionText = conditionText;
        this.kind = kind;
        this.extensionExit = extensionExit;
    }

    public UseCaseExtension(Long id, String conditionText, ExtensionKind kind, ExtensionExit extensionExit) {
        this.id = id;
        this.conditionText = conditionText;
        this.kind = kind;
        this.extensionExit = extensionExit;
    }

    public void addStep(UseCaseExtensionStep step) {
        steps.add(step);
        step.setExtension(this);
    }

    public void removeStep(UseCaseExtensionStep step) {
        steps.remove(step);
        step.setExtension(null);
    }

    public void replaceSteps(List<UseCaseExtensionStep> newSteps) {
        this.steps.clear();
        if (newSteps != null) {
            newSteps.forEach(this::addStep);
        }
    }

    /**
     * Revises this extension's steps in place to match the submitted ones, pairing them by id among this
     * extension's own steps only. See {@link OwnedChildren}.
     */
    public void reviseSteps(List<UseCaseExtensionStep> submitted) {
        OwnedChildren.revise(this.steps, submitted, UseCaseExtensionStep::getId,
                UseCaseExtensionStep::reviseFrom, UseCaseExtensionStep::copyOf, step -> step.setExtension(this));
    }

    /**
     * Copies a submitted extension's content onto this existing extension, keeping its id. {@code label} and
     * {@code resumeStepNo} are left alone, because no request carries them.
     */
    void reviseFrom(UseCaseExtension submitted) {
        this.conditionText = submitted.getConditionText();
        this.kind = submitted.getKind();
        this.extensionExit = submitted.getExtensionExit();
        reviseSteps(submitted.getSteps());
    }

    /**
     * Builds a new extension, without an id, with the submitted extension's content and fresh copies of its steps.
     */
    static UseCaseExtension copyOf(UseCaseExtension submitted) {
        UseCaseExtension copy = new UseCaseExtension(submitted.getConditionText(), submitted.getKind(), submitted.getExtensionExit());
        submitted.getSteps().forEach(step -> copy.addStep(UseCaseExtensionStep.copyOf(step)));
        return copy;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UseCaseMainStep getBaseStep() {
        return baseStep;
    }

    public void setBaseStep(UseCaseMainStep baseStep) {
        this.baseStep = baseStep;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getConditionText() {
        return conditionText;
    }

    public void setConditionText(String conditionText) {
        this.conditionText = conditionText;
    }

    public ExtensionKind getKind() {
        return kind;
    }

    public void setKind(ExtensionKind kind) {
        this.kind = kind;
    }

    public ExtensionExit getExtensionExit() {
        return extensionExit;
    }

    public void setExtensionExit(ExtensionExit exit) {
        this.extensionExit = exit;
    }

    public Integer getResumeStepNo() {
        return resumeStepNo;
    }

    public void setResumeStepNo(Integer resumeStepNo) {
        this.resumeStepNo = resumeStepNo;
    }

    public List<UseCaseExtensionStep> getSteps() {
        return steps;
    }

    public void setSteps(List<UseCaseExtensionStep> steps) {
        this.steps.clear();
        if (steps != null) {
            steps.forEach(step -> {
                step.setExtension(this);
                this.steps.add(step);
            });
        }
    }

}
