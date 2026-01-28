package team.projectpulse.ram.document;

import jakarta.persistence.*;
import team.projectpulse.user.PeerEvaluationUser;

import java.time.Instant;

/**
 * Represents a lock on a specific section of a document to prevent concurrent edits.
 *
 *
 */
@Entity
public class DocumentSectionLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Exactly one lock per section
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private DocumentSection documentSection;

    @ManyToOne
    private PeerEvaluationUser lockedBy;

    private Instant lockedAt;

    private Instant expiresAt;

    private String reason;

    @Version
    private Integer version;


    public DocumentSectionLock() {
    }

    public boolean isLocked(Instant now) {
        if (lockedAt == null) return false;
        if (expiresAt == null) return true;
        return expiresAt.isAfter(now);
    }

    public boolean isExpired(Instant now) {
        return lockedAt != null && expiresAt != null && !expiresAt.isAfter(now);
    }

    public void lock(PeerEvaluationUser user, Instant now, Instant expiresAt, String reason) {
        this.lockedBy = user;
        this.lockedAt = now;
        this.expiresAt = expiresAt; // null => manual lock
        this.reason = reason;
    }

    public void extend(Instant newExpiresAt, Instant now) {
        if (lockedAt == null) {
            throw new IllegalStateException("Cannot extend: section is not locked.");
        }
        if (expiresAt == null) {
            throw new IllegalStateException("Cannot extend: lock is manual (no expiresAt).");
        }
        if (isExpired(now)) {
            throw new IllegalStateException("Cannot extend: lock has already expired.");
        }
        if (newExpiresAt == null || !newExpiresAt.isAfter(now)) {
            throw new IllegalArgumentException("newExpiresAt must be in the future.");
        }
        this.expiresAt = newExpiresAt;
    }

    /**
     * Release the lock (also used after detecting expiration to normalize state).
     */
    public void unlock() {
        this.lockedBy = null;
        this.lockedAt = null;
        this.expiresAt = null;
        this.reason = null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DocumentSection getDocumentSection() {
        return documentSection;
    }

    public void setDocumentSection(DocumentSection documentSection) {
        this.documentSection = documentSection;
    }

    public PeerEvaluationUser getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(PeerEvaluationUser lockedBy) {
        this.lockedBy = lockedBy;
    }

    public Instant getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(Instant lockedAt) {
        this.lockedAt = lockedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

}