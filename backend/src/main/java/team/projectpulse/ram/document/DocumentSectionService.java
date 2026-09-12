package team.projectpulse.ram.document;

import jakarta.persistence.OptimisticLockException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.requirement.RequirementArtifactService;
import team.projectpulse.ram.requirement.SectionType;
import team.projectpulse.system.UserUtils;
import team.projectpulse.system.exception.*;
import team.projectpulse.user.PeerEvaluationUser;
import team.projectpulse.user.UserRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class DocumentSectionService {

    private final Duration defaultLockTtl;
    private final DocumentSectionRepository documentSectionRepository;
    private final RequirementArtifactService requirementArtifactService;
    private final UserUtils userUtils;
    private final UserRepository userRepository;


    public DocumentSectionService(DocumentSectionRepository documentSectionRepository, RequirementArtifactService requirementArtifactService, UserUtils userUtils, UserRepository userRepository,
                                  @Value("${ram.lock.default-lock-ttl:PT15M}") Duration defaultLockTtl) {
        this.documentSectionRepository = documentSectionRepository;
        this.requirementArtifactService = requirementArtifactService;
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

        oldDocumentSection.setContent(update.getContent());
        if (oldDocumentSection.getType() == SectionType.LIST) {
            oldDocumentSection.replaceAllRequirementArtifacts(mergeSubmittedArtifacts(teamId, oldDocumentSection, update.getRequirementArtifacts()));
        }
        return this.documentSectionRepository.save(oldDocumentSection);
    }

    /**
     * Works out the new artifact list for a LIST section, reusing the section's own artifact rows where the
     * submission names one and creating a row for every entry that it does not.
     *
     * <p><strong>Why an entry is matched rather than adopted.</strong> This used to hand the submitted artifacts
     * to {@code replaceAllRequirementArtifacts} directly. Those objects carried whatever id the request body
     * named, and {@code DocumentSection.requirementArtifacts} cascades ALL, so the save pulled the named rows into
     * this section and stamped this section's team onto them. Any artifact in the database could be taken that
     * way, including another team's. The converter no longer maps an id, and the only rows written here are the
     * ones already hanging off {@code section}, found by a key that has to match one of them.
     *
     * <p><strong>Why the artifact key is safe to match on.</strong> It arrives in the request body, so it is not
     * trusted as an identifier. What makes it safe is the set it is looked up in, not the value itself: only the
     * section's <em>current</em> artifacts, which the caller is already authorized for by the route rule and the
     * team-scoped finder that loaded the section. Note that artifact keys are minted per team, so keys do
     * collide across teams and another team's key can well match one of this section's. That is harmless for the
     * same reason: what it matches is this section's own row, never the other team's. A key matching nothing here
     * falls through and becomes a new artifact owned by this team. So the worst a forged key can do is edit a row
     * the caller could edit anyway, or create one they could create anyway.
     *
     * <p>An existing artifact takes only its editable fields from the submission, the same four that
     * {@code RequirementArtifactService.updateRequirementArtifact} allows. Type and artifact key stay as they
     * were, so a key cannot come to disagree with the type it was minted for; changing an item's type means
     * removing it and adding a new one.
     *
     * <p>Artifacts the submission leaves out keep their rows and lose only their link to this section. Removing
     * an item from a document does not delete the requirement it names: deletion is its own explicit operation,
     * and an artifact belonging to no section is an ordinary state rather than an orphan. Plenty never belong to
     * one at all, including a use case's preconditions and postconditions, its actors, and glossary terms. That
     * is why the association has no {@code orphanRemoval}.
     */
    private List<RequirementArtifact> mergeSubmittedArtifacts(Integer teamId, DocumentSection section, List<RequirementArtifact> submittedArtifacts) {
        if (submittedArtifacts == null) {
            return List.of();
        }

        Map<String, RequirementArtifact> existingByArtifactKey = new HashMap<>();
        for (RequirementArtifact existing : section.getRequirementArtifacts()) {
            if (StringUtils.hasText(existing.getArtifactKey())) {
                existingByArtifactKey.put(existing.getArtifactKey(), existing);
            }
        }

        List<RequirementArtifact> newArtifactList = new ArrayList<>();
        for (RequirementArtifact submitted : submittedArtifacts) {
            RequirementArtifact existing = StringUtils.hasText(submitted.getArtifactKey())
                    ? existingByArtifactKey.get(submitted.getArtifactKey())
                    : null;

            if (existing != null) {
                existing.setTitle(submitted.getTitle());
                existing.setContent(submitted.getContent());
                existing.setPriority(submitted.getPriority());
                existing.setNotes(submitted.getNotes());
                newArtifactList.add(existing);
            } else {
                if (submitted.getType() == null) {
                    throw new IllegalArgumentException("Requirement artifact type is required.");
                }
                submitted.setTeam(section.getDocument().getTeam());
                submitted.setArtifactKey(this.requirementArtifactService.generateNextArtifactKey(teamId, submitted.getType()));
                newArtifactList.add(submitted);
            }
        }
        return newArtifactList;
    }

}
