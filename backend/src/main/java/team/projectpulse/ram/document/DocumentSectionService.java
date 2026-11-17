package team.projectpulse.ram.document;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.OptimisticLockException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.projectpulse.ram.document.template.DocumentTemplateRegistry;
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.requirement.RequirementArtifactService;
import team.projectpulse.ram.requirement.RequirementArtifactType;
import team.projectpulse.ram.requirement.SectionType;
import team.projectpulse.system.UserUtils;
import team.projectpulse.system.exception.*;
import team.projectpulse.user.PeerEvaluationUser;
import team.projectpulse.user.UserRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class DocumentSectionService {

    private final Duration defaultLockTtl;
    private final DocumentSectionRepository documentSectionRepository;
    private final RequirementArtifactService requirementArtifactService;
    private final UserUtils userUtils;
    private final UserRepository userRepository;
    private final DocumentTemplateRegistry documentTemplateRegistry;
    private final EntityManager entityManager;


    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "The EntityManager is Spring's shared, transaction-scoped proxy; holding a reference to it is how it is meant to be used.")
    public DocumentSectionService(DocumentSectionRepository documentSectionRepository, RequirementArtifactService requirementArtifactService, UserUtils userUtils, UserRepository userRepository,
                                  DocumentTemplateRegistry documentTemplateRegistry, EntityManager entityManager,
                                  @Value("${ram.lock.default-lock-ttl:PT15M}") Duration defaultLockTtl) {
        this.documentSectionRepository = documentSectionRepository;
        this.documentTemplateRegistry = documentTemplateRegistry;
        this.entityManager = entityManager;
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

    /**
     * Saves document section {@code documentSectionId} of {@code teamId} at {@code expectedVersion}: its content and, for
     * a LIST section, its items in submission order (UC-DOC-edit-document; new items realize UC-ART-create-artifact). The
     * caller must hold the lock. Like {@code UseCaseService.updateUseCase}, the save runs in two phases: the items are
     * resolved and checked first while nothing is written, then the version and lock are checked, and only then is
     * anything written. The submitted items are {@code update.getRequirementArtifacts()}; a RICH_TEXT section ignores
     * them.
     * <p>
     * {@code update} is built by {@code DocumentSectionDtoToDocumentSectionConverter} straight from the request body, so
     * its items are request data, not rows: each carries the body's {@code id} as a pairing hint only. Never save them,
     * and never hand them to {@code replaceAllRequirementArtifacts}: that collection cascades, so an item carrying
     * another team's id would be merged over that team's row. {@link #resolveSectionItems} is the only reader.
     *
     * @throws ObjectNotFoundException       if the document section is not in this team's document
     * @throws SectionItemRejectedException  (400) if a new item's type is not admitted here, or an id is repeated
     * @throws IllegalArgumentException      if no version is given
     * @throws OptimisticLockException       (409) if {@code expectedVersion} is stale
     * @throws SectionLockRequiredException  (409) if no one holds the lock
     * @throws SectionAlreadyLockedException (423) if someone else holds it
     */
    public DocumentSection updateDocumentSectionContent(Integer teamId, Long documentId, Long documentSectionId, DocumentSection update, Integer expectedVersion) {
        DocumentSection oldDocumentSection = this.documentSectionRepository.findByIdAndDocumentIdAndDocumentTeamTeamId(documentSectionId, documentId, teamId)
                .orElseThrow(() -> new ObjectNotFoundException("document section", documentSectionId));

        // Phase 1: resolve and check every item. This only reads, so a refused item leaves the database untouched.
        List<ResolvedItem> resolvedItems = oldDocumentSection.getType() == SectionType.LIST
                ? resolveSectionItems(oldDocumentSection, update.getRequirementArtifacts())
                : null;

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

        // Every accepted save increments the version, so the next save from a stale editor is refused above. Hibernate
        // only increments it by itself when a DocumentSection column is dirty, and requirementArtifacts is the mappedBy
        // side, so a save that changes only the list would otherwise leave the version as it was. This is the same fix
        // as UseCaseService.updateUseCase: PESSIMISTIC_FORCE_INCREMENT writes the new version now, guarded by the old one.
        this.entityManager.lock(oldDocumentSection, LockModeType.PESSIMISTIC_FORCE_INCREMENT);

        // Phase 2: write.
        oldDocumentSection.setContent(update.getContent());
        if (resolvedItems != null) {
            oldDocumentSection.replaceAllRequirementArtifacts(applySectionItems(teamId, resolvedItems));
        }
        return this.documentSectionRepository.save(oldDocumentSection);
    }

    /**
     * One item as {@link #resolveSectionItems} found it: either one of the document section's own rows, with the edits
     * to copy onto it, or a new artifact built from the accepted fields and not yet saved ({@code edits} is null).
     * <p>
     * Why a pair and not just the artifact, as {@code UseCaseService.resolveConditions} returns: a use case links an
     * existing condition exactly as stored (it never rewords one), so once its id is resolved nothing is left to do to
     * that row. An existing section item, by contrast, takes the student's edits to its title, content, priority and
     * notes, and those must not be copied on until after the version and lock checks, because phase 1 writes nothing.
     * So phase 1 hands phase 2 both the row and the edits waiting for it. ({@code edits == null} marks a new item here,
     * as {@code id == null} does for a condition.) {@code edits} is the submitted item itself, request data that is only
     * ever read, never saved.
     */
    private record ResolvedItem(RequirementArtifact artifact, RequirementArtifact edits) {
    }

    /**
     * Resolves the submitted items of a LIST section against its own rows and checks them, writing nothing:
     * <ul>
     *   <li>An item whose {@code id} names one of this document section's current rows pairs with that row. The id is
     *       looked up only among those rows, never through a repository, so an id from a request body can only ever
     *       select a row the caller may already edit (BR-team-scoped-access, OI-46).</li>
     *   <li>Any other item (no id, or an id naming none of those rows, such as another team's artifact or one in
     *       another document section) becomes a new artifact, built fresh from the accepted fields. Its type must be
     *       one the document section admits (BR-artifact-home). It is a copy, never a move.</li>
     *   <li>The same id twice in one save is refused, as {@code UseCaseService.resolveConditions} refuses a repeated
     *       condition.</li>
     * </ul>
     * An existing item keeps its type, so the admitted-type check applies to new items only: a legacy {@code OTHER} row
     * saves unchanged. The first failure is reported, by the item's 1-based position in the list.
     */
    private List<ResolvedItem> resolveSectionItems(DocumentSection section, List<RequirementArtifact> items) {
        // The rows this document section holds right now, by id. This map is the only place a submitted id is looked
        // up, which is what makes trusting the id safe: it can only find a row that is already in this section.
        Map<Long, RequirementArtifact> ownRowsById = new HashMap<>();
        for (RequirementArtifact own : section.getRequirementArtifacts()) {
            ownRowsById.put(own.getId(), own);
        }

        // The types this section may hold, from its template (for example ASSUMPTION and DEPENDENCY for Assumptions and
        // Dependencies). Only new items are checked against it, below.
        List<RequirementArtifactType> admittedTypes = this.documentTemplateRegistry.admittedTypes(section.getDocument().getType(), section.getSectionKey());

        List<ResolvedItem> resolved = new ArrayList<>(items.size());
        Set<Long> seenIds = new HashSet<>();
        for (int i = 0; i < items.size(); i++) {
            RequirementArtifact item = items.get(i);
            int position = i + 1; // what the student sees in the editor, for the error message

            // Check 1: no id twice. Two entries naming one row would make the list ambiguous (which edits win, and
            // where does the row go?), so the whole save is refused. Set.add returns false for an id already seen.
            if (item.getId() != null && !seenIds.add(item.getId())) {
                throw new SectionItemRejectedException("Item " + position + " repeats an item already in this list.");
            }

            // Case A, an existing item: its id names one of this section's rows. Pair the row with the submitted
            // fields; applySectionItems copies them on later. No type check here: the row keeps the type it has.
            RequirementArtifact own = item.getId() != null ? ownRowsById.get(item.getId()) : null;
            if (own != null) {
                resolved.add(new ResolvedItem(own, item));
                continue;
            }

            // Case B, a new item: it has no id, or an id that names none of this section's rows (stale, invented, or
            // another team's). Either way the id is ignored and a fresh artifact is built from the submitted fields.
            // Check 2: its type must be one the section admits (BR-artifact-home).
            if (!admittedTypes.contains(item.getType())) {
                throw new SectionItemRejectedException("Item " + position + " is a " + item.getType() + ", which the " + section.getTitle() + " section does not hold.");
            }
            // Built fresh, never the submitted object itself: that one carries whatever else the body said (its id,
            // an artifact key, a source document section), and only the accepted fields should reach the new row.
            // This one has no id and no key yet, and is saved only after every item has passed and the version and
            // lock checks.
            RequirementArtifact artifact = new RequirementArtifact();
            artifact.setType(item.getType());
            artifact.setTitle(item.getTitle());
            artifact.setContent(item.getContent());
            artifact.setPriority(item.getPriority());
            artifact.setNotes(item.getNotes());
            resolved.add(new ResolvedItem(artifact, null));
        }
        return resolved;
    }

    /**
     * Applies the items {@link #resolveSectionItems} checked, after the version and lock checks. A paired row takes only
     * its four editable fields, the same ones {@code RequirementArtifactService.updateRequirementArtifact} allows, so its
     * type and artifact key stay as they were. A new item is saved through
     * {@link RequirementArtifactService#saveRequirementArtifact}, which sets the route's team and mints its key
     * (BR-artifact-key-unique), as for every artifact. Returns the list in submission order; a row left out of it loses
     * only its link to this document section (removing an item does not yet delete it, OI-23).
     */
    private List<RequirementArtifact> applySectionItems(Integer teamId, List<ResolvedItem> resolvedItems) {
        List<RequirementArtifact> artifacts = new ArrayList<>(resolvedItems.size());
        // Walk the items in the order the student submitted them; that order becomes the list's order on save
        // (the @OrderColumn on DocumentSection.requirementArtifacts).
        for (ResolvedItem item : resolvedItems) {
            RequirementArtifact artifact = item.artifact();
            if (item.edits() != null) {
                // Existing item: the row is already managed by Hibernate, so setting fields is enough; the change is
                // flushed when the transaction commits. Type, key, team and source section are deliberately not
                // copied, so a request cannot change them.
                artifact.setTitle(item.edits().getTitle());
                artifact.setContent(item.edits().getContent());
                artifact.setPriority(item.edits().getPriority());
                artifact.setNotes(item.edits().getNotes());
                artifacts.add(artifact);
            } else {
                // New item: save it the same way every artifact is created, so it gets the route's team and its next
                // key (for example FR-4). Keep the returned, saved instance, which now has its id and key.
                artifacts.add(this.requirementArtifactService.saveRequirementArtifact(teamId, artifact));
            }
        }
        // The caller hands this list to replaceAllRequirementArtifacts, which makes it the section's whole list: rows
        // in it are linked in this order, and rows missing from it are unlinked.
        return artifacts;
    }

}
