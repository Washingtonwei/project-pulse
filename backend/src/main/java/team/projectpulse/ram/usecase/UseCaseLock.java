package team.projectpulse.ram.usecase;

import jakarta.persistence.*;
import team.projectpulse.user.PeerEvaluationUser;

import java.time.Instant;

/**
 * Represents a lock on a specific use case to prevent concurrent edits.
 */
@Entity
public class UseCaseLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Exactly one lock per use case
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private UseCase useCase;

    @ManyToOne
    private PeerEvaluationUser lockedBy;

    private Instant lockedAt;

    private Instant expiresAt;

    private String reason;

    @Version
    private Integer version;


    public UseCaseLock() {
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
        this.expiresAt = expiresAt;
        this.reason = reason;
    }

    public void extend(Instant newExpiresAt, Instant now) {
        if (lockedAt == null) {
            throw new IllegalStateException("Cannot extend: use case is not locked.");
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
     * Release the lock
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

    public UseCase getUseCase() {
        return useCase;
    }

    public void setUseCase(UseCase useCase) {
        this.useCase = useCase;
    }

    public PeerEvaluationUser getLockedBy() {
        return lockedBy;
    }

    public Instant getLockedAt() {
        return lockedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public String getReason() {
        return reason;
    }

}