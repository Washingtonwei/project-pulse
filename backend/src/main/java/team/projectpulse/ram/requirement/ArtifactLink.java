package team.projectpulse.ram.requirement;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import team.projectpulse.team.Team;
import team.projectpulse.user.PeerEvaluationUser;

import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class ArtifactLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Team team;

    @ManyToOne
    private RequirementArtifact sourceArtifact;

    @ManyToOne
    private RequirementArtifact targetArtifact;

    @Enumerated(EnumType.STRING)
    private ArtifactLinkType type;

    @Lob
    private String notes;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @ManyToOne
    @CreatedBy
    private PeerEvaluationUser createdBy;

    @ManyToOne
    @LastModifiedBy
    private PeerEvaluationUser updatedBy;


    public ArtifactLink() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public RequirementArtifact getSourceArtifact() {
        return sourceArtifact;
    }

    public void setSourceArtifact(RequirementArtifact sourceArtifact) {
        this.sourceArtifact = sourceArtifact;
    }

    public RequirementArtifact getTargetArtifact() {
        return targetArtifact;
    }

    public void setTargetArtifact(RequirementArtifact targetArtifact) {
        this.targetArtifact = targetArtifact;
    }

    public ArtifactLinkType getType() {
        return type;
    }

    public void setType(ArtifactLinkType type) {
        this.type = type;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public PeerEvaluationUser getCreatedBy() {
        return createdBy;
    }

    public PeerEvaluationUser getUpdatedBy() {
        return updatedBy;
    }

}