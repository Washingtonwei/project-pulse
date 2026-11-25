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
// The backstop of BR-artifact-key-unique, created by migration V14 and declared here as well, so the schema dev and the
// tests build from the entities (ddl-auto: create) has it too. Its name is how a violation is recognized (ArtifactKeys).
@Table(uniqueConstraints = @UniqueConstraint(name = ArtifactKeys.KEY_INDEX, columnNames = {"team_team_id", "artifact_key"}))
public class RequirementArtifact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Team team;

    @Enumerated(EnumType.STRING)
    private RequirementArtifactType type;

    // The type's prefix and a slug the student chose, e.g. "GLO-artifact", "UC-create-use-case", "FR-export-csv"
    // (BR-artifact-key-unique; the format lives in ArtifactKeys).
    @Column(nullable = false)
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

    // The artifact's home: the list-type document section it is an item of (BR-artifact-home). Null for the types
    // with no list home (glossary terms, use cases, preconditions, postconditions).
    @ManyToOne
    private DocumentSection sourceDocumentSection;

    // Optimistic lock: the artifact is its own aggregate, created and edited outside any document section save, so a
    // stale edit is refused against this version (DI-concurrency-control, UC-ART-edit-artifact 10a).
    @Version
    private Integer version;

    @Lob
    private String notes;

    // Links where this artifact is the source, and where it is the target. Neither set cascades: ArtifactLinkService
    // saves and deletes each link itself, and deleting an artifact must never take its links with it
    // (BR-deletion-integrity). A delete that slips past RequirementArtifactService's reference check fails on the
    // artifact_link foreign key instead of losing the link.
    @OneToMany(mappedBy = "sourceArtifact")
    private Set<ArtifactLink> outgoingArtifactLinks = new HashSet<>();

    @OneToMany(mappedBy = "targetArtifact")
    private Set<ArtifactLink> incomingArtifactLinks = new HashSet<>();

    // Comment threads belong to the artifact and are deleted with it (UC-ART-delete-artifact, Deletion strategy).
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

    public Integer getVersion() {
        return version;
    }

}
