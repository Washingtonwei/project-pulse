package team.projectpulse.ram.collaboration;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import team.projectpulse.user.PeerEvaluationUser;

import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private CommentThread commentThread;

    @ManyToOne
    @CreatedBy
    private PeerEvaluationUser author;

    @Lob
    private String content;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;


    public Comment() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CommentThread getCommentThread() {
        return commentThread;
    }

    public void setCommentThread(CommentThread commentThread) {
        this.commentThread = commentThread;
    }

    public PeerEvaluationUser getAuthor() {
        return author;
    }

    public void setAuthor(PeerEvaluationUser author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

}

