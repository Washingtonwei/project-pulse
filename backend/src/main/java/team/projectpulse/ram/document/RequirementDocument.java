package team.projectpulse.ram.document;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import team.projectpulse.ram.collaboration.CommentThread;
import team.projectpulse.team.Team;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class RequirementDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private DocumentType type;

    @ManyToOne
    private Team team;

    // e.g., "VISION_SCOPE", "GLOSSARY", "USE_CASES", "SRS"
    private String documentKey;

    private String title;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "document")
    @OrderColumn(name = "section_index")
    private List<DocumentSection> sections = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private DocumentStatus status = DocumentStatus.DRAFT;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CommentThread> commentThreads = new HashSet<>();

    @Version
    private Integer version;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;


    public RequirementDocument() {
    }

    public void addSection(DocumentSection section) {
        if (section == null) return;

        this.sections.add(section);
        section.setDocument(this);
    }

    public void removeSection(DocumentSection section) {
        if (section == null) return;

        this.sections.remove(section);
        if (section.getDocument() == this) {
            section.setDocument(null);
        }
    }

    public void addCommentThread(CommentThread thread) {
        if (thread == null) return;

        this.commentThreads.add(thread);
        thread.setDocument(this);
    }

    public void removeCommentThread(CommentThread thread) {
        if (thread == null) return;

        this.commentThreads.remove(thread);
        if (thread.getDocument() == this) {
            thread.setDocument(null);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DocumentType getType() {
        return type;
    }

    public void setType(DocumentType type) {
        this.type = type;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public String getDocumentKey() {
        return documentKey;
    }

    public void setDocumentKey(String documentKey) {
        this.documentKey = documentKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<DocumentSection> getSections() {
        return sections;
    }

    public void setSections(List<DocumentSection> sections) {
        this.sections = sections;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }
    
    public Set<CommentThread> getCommentThreads() {
        return commentThreads;
    }

    public void setCommentThreads(Set<CommentThread> commentThreads) {
        this.commentThreads = commentThreads;
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

}
