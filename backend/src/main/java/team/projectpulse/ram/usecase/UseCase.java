package team.projectpulse.ram.usecase;

import jakarta.persistence.*;
import team.projectpulse.ram.requirement.RequirementArtifact;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class UseCase {

    @Id
    private Long id;

    /**
     * Shared identity with RequirementArtifact (@MapsId: the use case's id is its artifact's id).
     * Invariant: artifact.type == USE_CASE, set by UseCaseDtoToUseCaseConverter, never from the request.
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @MapsId
    private RequirementArtifact artifact;

    /**
     * The primary actor: a stakeholder or an external system of the use case's team (BR-use-case-actor-types).
     * Checked by {@code UseCaseService.resolveActors}; {@code saveUseCase} and {@code updateUseCase} set only what it
     * returns, so no actor is ever set straight from a request.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private RequirementArtifact primaryActor;

    /**
     * Secondary actors (0..N), each a stakeholder or an external system of the use case's team, under the same rule
     * and the same enforcement as the primary actor.
     */
    @ManyToMany
    @JoinTable(
            name = "use_case_secondary_actor",
            joinColumns = @JoinColumn(name = "use_case_id"),
            inverseJoinColumns = @JoinColumn(name = "actor_artifact_id")
    )
    private Set<RequirementArtifact> secondaryActors = new HashSet<>();

    @Lob
    private String useCaseTrigger;

    /**
     * The preconditions, in this use case's order: references to shared team artifacts (BR-use-case-condition-shared).
     * No cascade, because a condition is saved through {@code RequirementArtifactService} on its own and outlives the
     * link. The order belongs to the link, so it is stored on the join table.
     */
    @ManyToMany
    @JoinTable(
            name = "use_case_precondition",
            joinColumns = @JoinColumn(name = "use_case_id"),
            inverseJoinColumns = @JoinColumn(name = "precondition_artifact_id")
    )
    @OrderColumn(name = "precondition_index")
    private List<RequirementArtifact> preconditions = new ArrayList<>();

    /**
     * The postconditions, in this use case's order, mapped like {@link #preconditions}.
     */
    @ManyToMany
    @JoinTable(
            name = "use_case_postcondition",
            joinColumns = @JoinColumn(name = "use_case_id"),
            inverseJoinColumns = @JoinColumn(name = "postcondition_artifact_id")
    )
    @OrderColumn(name = "postcondition_index")
    private List<RequirementArtifact> postconditions = new ArrayList<>();

    /**
     * Main success scenario steps (ordered).
     */
    @OneToMany(mappedBy = "useCase", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "main_step_index")
    private List<UseCaseMainStep> mainSteps = new ArrayList<>();

    @OneToOne(mappedBy = "useCase", cascade = CascadeType.ALL, orphanRemoval = true)
    private UseCaseLock lock; // Only one user can edit a use case at a time

    @Version
    private Integer version;


    public UseCase() {
    }

    /* ---------- Convenience helpers ---------- */
    public void addMainStep(UseCaseMainStep step) {
        mainSteps.add(step);
        step.setUseCase(this);
    }

    public void removeMainStep(UseCaseMainStep step) {
        mainSteps.remove(step);
        step.setUseCase(null);
    }

    /**
     * Replaces all main steps with new ones, maintaining bidirectional relationships.
     * Old steps are removed (orphanRemoval will delete them).
     */
    public void replaceMainSteps(List<UseCaseMainStep> newSteps) {
        this.mainSteps.clear();
        if (newSteps != null) {
            newSteps.forEach(this::addMainStep);
        }
    }

    /**
     * Revises the main success scenario in place to match the submitted steps: a submitted step whose id is one of
     * this use case's own steps updates that step, any other submitted step becomes a new one, own steps the
     * submission omits are deleted, and the order follows the submission. The same rule applies to each step's
     * extensions and each extension's steps. See {@link OwnedChildren}.
     */
    public void reviseMainSteps(List<UseCaseMainStep> submitted) {
        OwnedChildren.revise(this.mainSteps, submitted, UseCaseMainStep::getId,
                UseCaseMainStep::reviseFrom, UseCaseMainStep::copyOf, step -> step.setUseCase(this));
    }

    public void initLockIfMissing() {
        if (this.lock == null) {
            this.lock = new UseCaseLock();
            this.lock.setUseCase(this);
        }
    }

    public void addSecondaryActor(RequirementArtifact stakeholder) {
        secondaryActors.add(stakeholder);
    }

    public void removeSecondaryActor(RequirementArtifact stakeholder) {
        secondaryActors.remove(stakeholder);
    }

    public void replaceSecondaryActors(Set<RequirementArtifact> actors) {
        this.secondaryActors.clear();
        if (actors != null) {
            this.secondaryActors.addAll(actors);
        }
    }

    /**
     * Replaces the ordered list of preconditions with {@code conditions}, which {@code UseCaseService} has already
     * resolved and checked. Only the links change; a condition dropped from the list stays in the team's graph.
     */
    public void replacePreconditions(List<RequirementArtifact> conditions) {
        this.preconditions.clear();
        if (conditions != null) {
            this.preconditions.addAll(conditions);
        }
    }

    public void replacePostconditions(List<RequirementArtifact> conditions) {
        this.postconditions.clear();
        if (conditions != null) {
            this.postconditions.addAll(conditions);
        }
    }

    // ---------- Getters and Setters ----------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RequirementArtifact getArtifact() {
        return artifact;
    }

    public void setArtifact(RequirementArtifact artifact) {
        this.artifact = artifact;
    }

    public RequirementArtifact getPrimaryActor() {
        return primaryActor;
    }

    public void setPrimaryActor(RequirementArtifact primaryActor) {
        this.primaryActor = primaryActor;
    }

    public Set<RequirementArtifact> getSecondaryActors() {
        return secondaryActors;
    }

    public void setSecondaryActors(Set<RequirementArtifact> secondaryActors) {
        this.secondaryActors = secondaryActors;
    }

    public String getUseCaseTrigger() {
        return useCaseTrigger;
    }

    public void setUseCaseTrigger(String trigger) {
        this.useCaseTrigger = trigger;
    }

    public List<RequirementArtifact> getPreconditions() {
        return preconditions;
    }

    public List<RequirementArtifact> getPostconditions() {
        return postconditions;
    }

    public List<UseCaseMainStep> getMainSteps() {
        return mainSteps;
    }

    public Integer getVersion() {
        return version;
    }

    public UseCaseLock getLock() {
        return lock;
    }

}
