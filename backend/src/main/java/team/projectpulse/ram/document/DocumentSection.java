package team.projectpulse.ram.document;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import team.projectpulse.ram.collaboration.CommentThread;
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.requirement.SectionType;
import team.projectpulse.user.PeerEvaluationUser;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class DocumentSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private RequirementDocument document;

    @Column
    private String sectionKey;

    @Column
    private String title;

    @Enumerated(EnumType.STRING)
    private SectionType type;

    // Used when sectionType == RICH_TEXT
    @Lob
    private String content;

    @OneToMany(mappedBy = "sourceDocumentSection", cascade = CascadeType.ALL)
    @OrderColumn(name = "artifact_index")
    private List<RequirementArtifact> requirementArtifacts = new ArrayList<>();

    @Lob
    private String guidance;

    @OneToMany(mappedBy = "documentSection", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CommentThread> commentThreads = new HashSet<>();

    @OneToOne(mappedBy = "documentSection", cascade = CascadeType.ALL, orphanRemoval = true)
    private DocumentSectionLock lock;

    @Version
    private Integer version;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @ManyToOne
    private PeerEvaluationUser createdBy;

    @ManyToOne
    private PeerEvaluationUser updatedBy;


    public DocumentSection() {
    }

    /* ---------- Convenience helpers ---------- */
    public void addRequirementArtifact(RequirementArtifact artifact) {
        this.requirementArtifacts.add(artifact);
        artifact.setSourceDocumentSection(this);
    }

    /**
     * Replaces all requirement artifacts with new ones, maintaining bidirectional relationships.
     */
    public void replaceAllRequirementArtifacts(List<RequirementArtifact> newArtifacts) {
        if (this.requirementArtifacts != null) {
            System.out.println("Before copying, requirementArtifacts initialized? " +
                    org.hibernate.Hibernate.isInitialized(this.requirementArtifacts));
            // Create a copy to avoid ConcurrentModificationException
            List<RequirementArtifact> oldArtifacts = new ArrayList<>(this.requirementArtifacts);
            System.out.println("After copying, requirementArtifacts initialized? " +
                    org.hibernate.Hibernate.isInitialized(this.requirementArtifacts));
            for (RequirementArtifact artifact : oldArtifacts) {
                artifact.setSourceDocumentSection(null);  // Break back-reference
            }
            this.requirementArtifacts.clear();  // Clear the collection
        }

        // Establish new bidirectional relationships
        if (newArtifacts != null) {
            newArtifacts.forEach(this::addRequirementArtifact);
        }
    }

    public void addCommentThread(CommentThread thread) {
        if (thread == null) return;
        this.commentThreads.add(thread);
        thread.setDocumentSection(this);
    }

    public void removeCommentThread(CommentThread thread) {
        if (thread == null) return;
        this.commentThreads.remove(thread);
        if (thread.getDocumentSection() == this) {
            thread.setDocumentSection(null);
        }
    }

    public void initLockIfMissing() {
        if (this.lock == null) {
            this.lock = new DocumentSectionLock();
            this.lock.setDocumentSection(this);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RequirementDocument getDocument() {
        return document;
    }

    public void setDocument(RequirementDocument document) {
        this.document = document;
    }

    public String getSectionKey() {
        return sectionKey;
    }

    public void setSectionKey(String sectionKey) {
        this.sectionKey = sectionKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public SectionType getType() {
        return type;
    }

    public void setType(SectionType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<RequirementArtifact> getRequirementArtifacts() {
        return requirementArtifacts;
    }

    public void setRequirementArtifacts(List<RequirementArtifact> requirementArtifacts) {
        this.requirementArtifacts = requirementArtifacts;
    }

    public String getGuidance() {
        return guidance;
    }

    public void setGuidance(String guidance) {
        this.guidance = guidance;
    }

    public Set<CommentThread> getCommentThreads() {
        return commentThreads;
    }

    public void setCommentThreads(Set<CommentThread> commentThreads) {
        this.commentThreads = commentThreads;
    }

    public DocumentSectionLock getLock() {
        return lock;
    }

    public Integer getVersion() {
        return version;
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
