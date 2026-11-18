package team.projectpulse.ram.document;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.OptimisticLockException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.projectpulse.system.UserUtils;
import team.projectpulse.system.exception.*;
import team.projectpulse.user.PeerEvaluationUser;
import team.projectpulse.user.UserRepository;

import java.time.Duration;
import java.time.Instant;

@Service
@Transactional
public class DocumentSectionService {

    private final Duration defaultLockTtl;
    private final DocumentSectionRepository documentSectionRepository;
    private final UserUtils userUtils;
    private final UserRepository userRepository;
    private final EntityManager entityManager;


    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "The EntityManager is Spring's shared, transaction-scoped proxy; holding a reference to it is how it is meant to be used.")
    public DocumentSectionService(DocumentSectionRepository documentSectionRepository, UserUtils userUtils, UserRepository userRepository, EntityManager entityManager,
                                  @Value("${ram.lock.default-lock-ttl:PT15M}") Duration defaultLockTtl) {
        this.documentSectionRepository = documentSectionRepository;
        this.entityManager = entityManager;
        this.userUtils = userUtils;
        this.userRepository = userRepository;
        this.defaultLockTtl = defaultLockTtl;
    }

    public DocumentSection findDocumentSectionByIdWithFullGraph(Integer teamId, Long documentId, Long documentSectionId) {
        return this.documentSectionRepository
                .findByIdWithFullGraph(teamId, documentId, documentSectionId)
                .orElseThrow(() -> new ObjectNotFoundException("document section", documentSectionId));
    }

    public DocumentSectionLock findSectionLock(Integer teamId, Long documentId, Long documentSectionId) {
        DocumentSection documentSection = this.documentSectionRepository.findByIdAndDocumentIdAndDocumentTeamTeamId(documentSectionId, documentId, teamId)
                .orElseThrow(() -> new ObjectNotFoundException("document section", documentSectionId));

        Instant now = Instant.now();
        DocumentSectionLock lock = documentSection.getLock();

        // Auto-unlock if expired
        if (lock.isExpired(now)) {
            lock.unlock();
        }

        return lock;
    }

    public DocumentSectionLock lockSection(Integer teamId, Long documentId, Long documentSectionId, String reason) {
        DocumentSectionLock sectionLock = findSectionLock(teamId, documentId, documentSectionId);

        Integer currentUserId = this.userUtils.getUserId();

        PeerEvaluationUser currentUser = this.userRepository.findById(currentUserId)
                .orElseThrow(() -> new ObjectNotFoundException("user", currentUserId));

        Instant now = Instant.now();
        Instant expiresAt = now.plus(defaultLockTtl);

        // Normalize expired lock
        if (sectionLock.isExpired(now)) {
            sectionLock.unlock();
        }

        if (sectionLock.isLocked(now)) {
            // already locked by the same user, then extend the lock
            if (sectionLock.getLockedBy() != null && sectionLock.getLockedBy().getId().equals(currentUserId)) {
                sectionLock.extend(expiresAt, now);
                return sectionLock;
            }

            String lockerName = sectionLock.getLockedBy() != null ? sectionLock.getLockedBy().getFirstName() + " " + sectionLock.getLockedBy().getLastName() : "another user";
            String until = sectionLock.getExpiresAt() != null ? sectionLock.getExpiresAt().toString() : "(no expiry)";
            throw new SectionAlreadyLockedException("Document section is locked by " + lockerName + " until " + until + ".");
        }

        sectionLock.lock(currentUser, now, expiresAt, reason);
        return sectionLock;
    }

    public void unlockSection(Integer teamId, Long documentId, Long documentSectionId) {
        DocumentSectionLock sectionLock = findSectionLock(teamId, documentId, documentSectionId);

        Integer currentUserId = this.userUtils.getUserId();

        if (this.userUtils.hasRole("ROLE_instructor")) {
            sectionLock.unlock();
            return;
        }

        Integer ownerId = sectionLock.getLockedBy() != null ? sectionLock.getLockedBy().getId() : null;
        if (ownerId == null || !ownerId.equals(currentUserId)) {
            throw new SectionUnlockNotAllowedException("Only the lock owner or an instructor can unlock this section.");
        }

        sectionLock.unlock();
    }

    /**
     * Saves the content of document section {@code documentSectionId} of {@code teamId} at {@code expectedVersion}
     * (UC-DOC-edit-document step 9). The caller must hold the lock. Only the document section's own content is
     * stored: a list section's items are requirement artifacts, created and edited on their own through the artifact
     * endpoints (UC-ART-create-artifact, UC-ART-edit-artifact), so any {@code requirementArtifacts} the request carries
     * are ignored.
     *
     * @throws ObjectNotFoundException       if the document section is not in this team's document
     * @throws IllegalArgumentException      if no version is given
     * @throws OptimisticLockException       (409) if {@code expectedVersion} is stale
     * @throws SectionLockRequiredException  (409) if no one holds the lock
     * @throws SectionAlreadyLockedException (423) if someone else holds it
     */
    public DocumentSection updateDocumentSectionContent(Integer teamId, Long documentId, Long documentSectionId, DocumentSection update, Integer expectedVersion) {
        DocumentSection oldDocumentSection = this.documentSectionRepository.findByIdAndDocumentIdAndDocumentTeamTeamId(documentSectionId, documentId, teamId)
                .orElseThrow(() -> new ObjectNotFoundException("document section", documentSectionId));

        if (expectedVersion == null) {
            throw new IllegalArgumentException("Document section version is required for update.");
        }

        Integer currentVersion = oldDocumentSection.getVersion();
        if (!expectedVersion.equals(currentVersion)) {
            throw new OptimisticLockException("Document section has been updated by another user. Please refresh and try again.");
        }

        Instant now = Instant.now();
        DocumentSectionLock lock = oldDocumentSection.getLock();

        if (lock.isExpired(now)) {
            lock.unlock();
        }

        Integer currentUserId = this.userUtils.getUserId();

        if (!lock.isLocked(now)) {
            throw new SectionLockRequiredException("You must first lock this document section before updating its content.");
        }

        Integer ownerId = lock.getLockedBy() != null ? lock.getLockedBy().getId() : null;
        if (ownerId == null || !ownerId.equals(currentUserId)) {
            throw new SectionAlreadyLockedException("Document section is locked by another user. You cannot update its content.");
        }

        // Every accepted save increments the version, as the architecture's Transactions & consistency rule asks, so
        // the next save from a stale editor is refused above even when this one left the content as it was (Hibernate
        // increments the version by itself only when a column is dirty). PESSIMISTIC_FORCE_INCREMENT writes the new
        // version now, guarded by the old one, as UseCaseService.updateUseCase does.
        this.entityManager.lock(oldDocumentSection, LockModeType.PESSIMISTIC_FORCE_INCREMENT);

        oldDocumentSection.setContent(update.getContent());
        return this.documentSectionRepository.save(oldDocumentSection);
    }

}
