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
     * Shared identity with RequirementArtifact.
     * Invariant (service-layer): artifact.type == USE_CASE
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @MapsId
    private RequirementArtifact artifact;

    /**
     * Primary actor must be a stakeholder artifact.
     * Invariant (service-layer): primaryActor.type == STAKEHOLDER
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private RequirementArtifact primaryActor;

    /**
     * Secondary actors (0..N), also stakeholders.
     */
    @ManyToMany
    @JoinTable(
            name = "use_case_secondary_actor",
            joinColumns = @JoinColumn(name = "use_case_id"),
            inverseJoinColumns = @JoinColumn(name = "stakeholder_artifact_id")
    )
    private Set<RequirementArtifact> secondaryActors = new HashSet<>();

    @Lob
    private String useCaseTrigger;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "use_case_precondition",
            joinColumns = @JoinColumn(name = "use_case_id"),
            inverseJoinColumns = @JoinColumn(name = "precondition_artifact_id")
    )
    private Set<RequirementArtifact> preconditions = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "use_case_postcondition",
            joinColumns = @JoinColumn(name = "use_case_id"),
            inverseJoinColumns = @JoinColumn(name = "postcondition_artifact_id")
    )
    private Set<RequirementArtifact> postconditions = new HashSet<>();

    /**
     * Main success scenario steps (ordered).
     */
    @OneToMany(mappedBy = "useCase", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "main_step_index")
    private List<UseCaseMainStep> mainSteps = new ArrayList<>();

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

    public void addSecondaryActor(RequirementArtifact stakeholder) {
        secondaryActors.add(stakeholder);
    }

    public void removeSecondaryActor(RequirementArtifact stakeholder) {
        secondaryActors.remove(stakeholder);
    }

    public void addPrecondition(RequirementArtifact preconditionArtifact) {
        preconditions.add(preconditionArtifact);
    }

    public void removePrecondition(RequirementArtifact preconditionArtifact) {
        preconditions.remove(preconditionArtifact);
    }

    public void addPostcondition(RequirementArtifact postconditionArtifact) {
        postconditions.add(postconditionArtifact);
    }

    public void removePostcondition(RequirementArtifact postconditionArtifact) {
        postconditions.remove(postconditionArtifact);
    }

    public void replaceSecondaryActors(Set<RequirementArtifact> actors) {
        this.secondaryActors.clear();
        if (actors != null) {
            this.secondaryActors.addAll(actors);
        }
    }

    public void replacePreconditions(Set<RequirementArtifact> conditions) {
        this.preconditions.clear();
        if (conditions != null) {
            this.preconditions.addAll(conditions);
        }
    }

    public void replacePostconditions(Set<RequirementArtifact> conditions) {
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

    public Set<RequirementArtifact> getPreconditions() {
        return preconditions;
    }

    public void setPreconditions(Set<RequirementArtifact> preconditions) {
        this.preconditions = preconditions;
    }

    public Set<RequirementArtifact> getPostconditions() {
        return postconditions;
    }

    public void setPostconditions(Set<RequirementArtifact> postconditions) {
        this.postconditions = postconditions;
    }

    public List<UseCaseMainStep> getMainSteps() {
        return mainSteps;
    }

    public void setMainSteps(List<UseCaseMainStep> mainSteps) {
        this.mainSteps = mainSteps;
    }

    public Integer getVersion() {
        return version;
    }

}
