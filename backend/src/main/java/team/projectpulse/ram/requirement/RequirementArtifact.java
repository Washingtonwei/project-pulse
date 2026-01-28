package team.projectpulse.ram.requirement;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import team.projectpulse.ram.collaboration.CommentThread;
import team.projectpulse.ram.document.DocumentSection;
import team.projectpulse.team.Team;
import team.projectpulse.user.PeerEvaluationUser;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class RequirementArtifact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Team team;

    @Enumerated(EnumType.STRING)
    private RequirementArtifactType type;

    // e.g., "GLO-3", "UC-1", "FR-12"
    private String artifactKey;

    // For GLOSSARY_TERM: term
    // For USE_CASE: use case name
    private String title;

    // For GLOSSARY_TERM: definition
    // For FR: "The system shall..."
    @Lob
    private String content;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @ManyToOne
    private DocumentSection sourceDocumentSection;

    @Lob
    private String notes;

    // Links where this artifact is the source
    @OneToMany(mappedBy = "sourceArtifact", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ArtifactLink> outgoingArtifactLinks = new HashSet<>();

    // Links where this artifact is the target
    @OneToMany(mappedBy = "targetArtifact", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ArtifactLink> incomingArtifactLinks = new HashSet<>();

    @OneToMany(mappedBy = "artifact", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CommentThread> commentThreads = new HashSet<>();

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


    public RequirementArtifact() {

    }

    public RequirementArtifact(Team team, RequirementArtifactType type, String title, String content, String notes) {
        this.team = team;
        this.type = type;
        this.title = title;
        this.content = content;
        this.notes = notes;
    }

    public void addOutgoingLink(ArtifactLink link) {
        outgoingArtifactLinks.add(link);
        link.setSourceArtifact(this);
    }

    public void removeOutgoingLink(ArtifactLink link) {
        outgoingArtifactLinks.remove(link);
        link.setSourceArtifact(null);
    }

    public void addIncomingLink(ArtifactLink link) {
        incomingArtifactLinks.add(link);
        link.setTargetArtifact(this);
    }

    public void removeIncomingLink(ArtifactLink link) {
        incomingArtifactLinks.remove(link);
        link.setTargetArtifact(null);
    }

    public void addCommentThread(CommentThread thread) {
        if (thread == null) return;
        this.commentThreads.add(thread);
        thread.setArtifact(this);
    }

    public void removeCommentThread(CommentThread thread) {
        if (thread == null) return;
        this.commentThreads.remove(thread);
        if (thread.getArtifact() == this) {
            thread.setArtifact(null);
        }
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

    public RequirementArtifactType getType() {
        return type;
    }

    public void setType(RequirementArtifactType type) {
        this.type = type;
    }

    public String getArtifactKey() {
        return artifactKey;
    }

    public void setArtifactKey(String artifactKey) {
        this.artifactKey = artifactKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public DocumentSection getSourceDocumentSection() {
        return sourceDocumentSection;
    }

    public void setSourceDocumentSection(DocumentSection sourceDocumentSection) {
        this.sourceDocumentSection = sourceDocumentSection;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Set<ArtifactLink> getOutgoingArtifactLinks() {
        return outgoingArtifactLinks;
    }

    public void setOutgoingArtifactLinks(Set<ArtifactLink> outgoingArtifactLinks) {
        this.outgoingArtifactLinks = outgoingArtifactLinks;
    }

    public Set<ArtifactLink> getIncomingArtifactLinks() {
        return incomingArtifactLinks;
    }

    public void setIncomingArtifactLinks(Set<ArtifactLink> incomingArtifactLinks) {
        this.incomingArtifactLinks = incomingArtifactLinks;
    }

    public Set<CommentThread> getCommentThreads() {
        return commentThreads;
    }

    public void setCommentThreads(Set<CommentThread> commentThreads) {
        this.commentThreads = commentThreads;
    }

    public PeerEvaluationUser getCreatedBy() {
        return createdBy;
    }

    public PeerEvaluationUser getUpdatedBy() {
        return updatedBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

}
