package team.projectpulse.ram.collaboration;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import team.projectpulse.ram.document.DocumentSection;
import team.projectpulse.ram.document.RequirementDocument;
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.team.Team;
import team.projectpulse.user.PeerEvaluationUser;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class CommentThread {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Team team;

    // Thread can be attached at doc-level, section-level, or artifact-level.
    @ManyToOne
    private RequirementDocument document;

    @ManyToOne
    private DocumentSection documentSection;

    @ManyToOne
    private RequirementArtifact artifact;

    @Enumerated(EnumType.STRING)
    private CommentThreadStatus status = CommentThreadStatus.OPEN;

    @OneToMany(mappedBy = "commentThread", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "comment_index")
    private List<Comment> comments = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @ManyToOne
    @CreatedBy
    private PeerEvaluationUser createdBy;

    @ManyToOne
    @LastModifiedBy
    private PeerEvaluationUser updatedBy;


    public CommentThread() {
    }

    public void addComment(Comment comment) {
        comment.setCommentThread(this);
        this.comments.add(comment);
    }

    public void removeComment(Comment comment) {
        this.comments.remove(comment);
        comment.setCommentThread(null);
    }

    public boolean isAnchoredToDocument() {
        return document != null;
    }

    public boolean isAnchoredToSection() {
        return documentSection != null;
    }

    public boolean isAnchoredToArtifact() {
        return artifact != null;
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

    public RequirementDocument getDocument() {
        return document;
    }

    public void setDocument(RequirementDocument document) {
        this.document = document;
    }

    public DocumentSection getDocumentSection() {
        return documentSection;
    }

    public void setDocumentSection(DocumentSection documentSection) {
        this.documentSection = documentSection;
    }

    public RequirementArtifact getArtifact() {
        return artifact;
    }

    public void setArtifact(RequirementArtifact artifact) {
        this.artifact = artifact;
    }

    public CommentThreadStatus getStatus() {
        return status;
    }

    public void setStatus(CommentThreadStatus status) {
        this.status = status;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
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

    public void setCreatedBy(PeerEvaluationUser createdBy) {
        this.createdBy = createdBy;
    }

    public PeerEvaluationUser getUpdatedBy() {
        return updatedBy;
    }

}

