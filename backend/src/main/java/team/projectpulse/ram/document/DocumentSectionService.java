package team.projectpulse.ram.document;

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


    public DocumentSectionService(DocumentSectionRepository documentSectionRepository, UserUtils userUtils, UserRepository userRepository,
                                  @Value("${ram.lock.default-lock-ttl:PT15M}") Duration defaultLockTtl) {
        this.documentSectionRepository = documentSectionRepository;
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

        // The content is the section's own column, so Hibernate raises the version when it changes, at flush. A save
        // that changes nothing stores nothing and leaves the version as it is; a stale editor still cannot save, because
        // a save needs the section lock and taking the lock changes the section's row.
        oldDocumentSection.setContent(update.getContent());
        return this.documentSectionRepository.save(oldDocumentSection);
    }

}
