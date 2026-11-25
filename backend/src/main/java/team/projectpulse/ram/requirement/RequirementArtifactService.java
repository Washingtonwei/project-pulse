package team.projectpulse.ram.requirement;

import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.util.StringUtils;
import team.projectpulse.ram.document.DocumentRepository;
import team.projectpulse.ram.document.DocumentSection;
import team.projectpulse.ram.document.DocumentSectionRepository;
import team.projectpulse.ram.document.DocumentStatus;
import team.projectpulse.ram.document.DocumentType;
import team.projectpulse.ram.document.RequirementDocument;
import team.projectpulse.ram.document.template.DocumentTemplate;
import team.projectpulse.ram.document.template.DocumentTemplateRegistry;
import team.projectpulse.ram.document.template.DocumentTemplateRegistry.Home;
import team.projectpulse.ram.requirement.dto.ArtifactKeyHolderDto;
import team.projectpulse.ram.requirement.dto.ArtifactReferrerDto;
import team.projectpulse.ram.requirement.dto.ArtifactReferrerDto.Relation;
import team.projectpulse.system.exception.ObjectNotFoundException;
import team.projectpulse.system.exception.RequirementArtifactRejectedException;
import team.projectpulse.team.Team;
import team.projectpulse.team.TeamRepository;

import static team.projectpulse.ram.requirement.ArtifactKeys.label;
import static team.projectpulse.ram.requirement.ArtifactKeys.withArticle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class RequirementArtifactService {

    private final RequirementArtifactRepository requirementArtifactRepository;
    private final TeamRepository teamRepository;
    private final DocumentRepository documentRepository;
    private final DocumentSectionRepository documentSectionRepository;
    private final DocumentTemplateRegistry documentTemplateRegistry;
    private final ArtifactLinkRepository artifactLinkRepository;

    public RequirementArtifactService(RequirementArtifactRepository requirementArtifactRepository, TeamRepository teamRepository,
                                      DocumentRepository documentRepository, DocumentSectionRepository documentSectionRepository, DocumentTemplateRegistry documentTemplateRegistry,
                                      ArtifactLinkRepository artifactLinkRepository) {
        this.requirementArtifactRepository = requirementArtifactRepository;
        this.teamRepository = teamRepository;
        this.documentRepository = documentRepository;
        this.documentSectionRepository = documentSectionRepository;
        this.documentTemplateRegistry = documentTemplateRegistry;
        this.artifactLinkRepository = artifactLinkRepository;
    }

    public Page<RequirementArtifact> findByCriteria(Integer teamId, Map<String, String> searchCriteria, Pageable pageable) {
        Specification<RequirementArtifact> spec = Specification.unrestricted(); // Start with an unrestricted specification

        if (StringUtils.hasLength(searchCriteria.get("type"))) {
            spec = spec.and(RequirementArtifactSpecs.hasType(RequirementArtifactType.valueOf(searchCriteria.get("type"))));
        }

        if (StringUtils.hasLength(searchCriteria.get("artifactKey"))) {
            spec = spec.and(RequirementArtifactSpecs.hasArtifactKeyLike(searchCriteria.get("artifactKey")));
        }

        if (StringUtils.hasLength(searchCriteria.get("title"))) {
            spec = spec.and(RequirementArtifactSpecs.hasTitleLike(searchCriteria.get("title")));
        }

        if (StringUtils.hasLength(searchCriteria.get("content"))) {
            spec = spec.and(RequirementArtifactSpecs.hasContentLike(searchCriteria.get("content")));
        }

        if (StringUtils.hasLength(searchCriteria.get("priority"))) {
            spec = spec.and(RequirementArtifactSpecs.hasPriority(Priority.valueOf(searchCriteria.get("priority"))));
        }

        spec = spec.and(RequirementArtifactSpecs.hasTeamId(teamId));

        return this.requirementArtifactRepository.findAll(spec, pageable);
    }

    public RequirementArtifact findRequirementArtifactById(Integer teamId, Long requirementArtifactId) {
        return this.requirementArtifactRepository.findByIdAndTeamTeamId(requirementArtifactId, teamId).orElseThrow(() ->
                new ObjectNotFoundException("requirement artifact", requirementArtifactId));
    }

    /**
     * Creates {@code draft} in team {@code teamId}. This is the only way to create a requirement artifact, so
     * BR-artifact-home and BR-artifact-key-unique hold for every caller. The team comes from the route. The artifact
     * key is the one the draft carries, which the student chose (UC-ART-create-artifact, "Artifact key rules"): it is
     * checked here, never minted or changed. The object must be new: every caller builds it with {@code new}, and no
     * converter maps an id (OI-46), so its null version makes the save an insert.
     * <p>
     * The key is checked twice (design: {@code docs/design/art.md}, "Artifact keys"). First its format and prefix
     * ({@link ArtifactKeys#requireValid}, 4a), then whether another artifact of the team holds it (4b), naming the
     * holder. A teammate can still commit the same key between that check and this insert; the unique key index then
     * refuses the insert, and {@link #saveChecked} turns that into a refusal naming the key (8c), after which the editor
     * asks the key-holder lookup for the holder. For example, a functional
     * requirement sent with {@code FR-user-authentication} in team 1, where the seed already holds that key, is refused
     * with "FR-user-authentication is already used by FR-user-authentication: User authentication.", the holder as data.
     * <p>
     * The type decides what else happens:
     * <ul>
     *   <li>A type with no list home (glossary term, use case, precondition, postcondition, user story) is only
     *   checked and inserted, with any document section reference cleared. Its own service calls this
     *   ({@code GlossaryService}, {@code UseCaseService}), and so does the dev seed. The call joins the caller's
     *   transaction when there is one (the services'), and runs in its own when there is none (the seed's, so each
     *   seeded artifact commits on its own). This branch reads no document and takes no lock. The generic endpoint
     *   refuses these types before calling here ({@link #notCreatedHere}), since each has its own editor.</li>
     *   <li>A list type becomes an item of its home document section, appended at the end (UC-ART-create-artifact).
     *   The home comes from the type alone. The document section's own lock is not checked: the section view offers
     *   Add only to its holder, as a UI gate (BR-edit-lock-required).</li>
     * </ul>
     * <p>
     * Concurrency (list types): the home section's row is locked ({@link DocumentSectionRepository#findByIdForUpdate})
     * before its list is read, so two creates in one section append one after the other instead of taking the same
     * {@code artifact_index}. The method runs at {@code READ COMMITTED} because under MySQL's default
     * {@code REPEATABLE READ} the snapshot fixed by the first plain read below would hide a create committed while this
     * one waited for the lock. The document section's version is left alone: the artifact is its own aggregate, and
     * {@code requirementArtifacts} is the {@code mappedBy} side, so the teammate editing the section's prose is not
     * made stale.
     *
     * @throws RequirementArtifactRejectedException (400) for a missing or malformed key (4a), a key another artifact of
     *                                              the team holds (4b, 8c, with the holder as data), or a list type
     *                                              whose home document is not created (8b) or is locked for review (8a)
     */
    // READ COMMITTED, so the list loaded after the row lock sees a teammate's just-committed item. Under MySQL's
    // default REPEATABLE READ it would read the snapshot from before the lock, and two concurrent creates could take
    // the same position. Named in full: the class imports jakarta.transaction.Transactional, which has no isolation
    // setting.
    @org.springframework.transaction.annotation.Transactional(isolation = Isolation.READ_COMMITTED)
    public RequirementArtifact createRequirementArtifact(Integer teamId, RequirementArtifact draft) {
        if (draft.getType() == null) {
            throw new IllegalArgumentException("RequirementArtifact.type is required to check its artifact key.");
        }
        // The key's format and prefix first, before any read: GLO-Artifact or FR-x on a risk is refused here (4a).
        ArtifactKeys.requireValid(draft.getType(), draft.getArtifactKey());

        // No list home (a glossary term, use case, precondition, postcondition or user story): it sits in no document
        // section's list, so there is no home to look up, no document to check and no row to lock. It only gets the
        // route's team and the holder check, then is inserted. For example, a new glossary term GLO-artifact from
        // GlossaryService, or a use case's new precondition PRE-student-logged-in from UseCaseService.
        if (!draft.getType().hasListHome()) {
            // Cleared, so a draft that arrives carrying a document section still lands in no list (BR-artifact-home).
            draft.setSourceDocumentSection(null);
            assignTeamAndCheckKeyAvailability(teamId, draft);
            return saveChecked(draft);
        }

        // For example: a RISK resolves to (VISION_SCOPE, "RISKS"), an ASSUMPTION to (SRS, "ASSUMPTIONS_DEPENDENCIES").
        Home home = resolveHome(draft.getType());

        RequirementDocument document = this.documentRepository.findByTeamTeamIdAndType(teamId, home.documentType())
                .orElseThrow(() -> new RequirementArtifactRejectedException("The team's " + documentTitle(home.documentType())
                        + " has not been created yet. The course admin creates the team's documents."));
        requireNotLockedForReview(document);

        // Two steps: find the home section's id with a plain read, then lock that one row by primary key. A FOR UPDATE
        // on this lookup could lock more than one row (the index range it scans), blocking creates in other sections.
        // The two calls return the same object, and its requirementArtifacts list is lazy: leave it untouched until the
        // lock is held, so it loads with every item a teammate has committed and the new item goes at the true end.
        // Reading it here would keep an outdated copy, and two concurrent creates could both take the same position.
        DocumentSection found = this.documentSectionRepository.findByDocumentIdAndSectionKey(document.getId(), home.sectionKey())
                .orElseThrow(() -> new RequirementArtifactRejectedException("The team's " + document.getTitle()
                        + " was created from an older template and has no list for " + withArticle(label(draft.getType())) + "."));
        DocumentSection section = this.documentSectionRepository.findByIdForUpdate(found.getId())
                .orElseThrow(() -> new ObjectNotFoundException("document section", found.getId()));

        // Team and key holder first: the holder check runs a query, and a query may flush pending changes, so the draft
        // must not be in the section's cascading list yet. Then appending sets its home, and the row is inserted once,
        // keyed and already in its list, with its index taken from its position at the end of the list read under the
        // lock.
        assignTeamAndCheckKeyAvailability(teamId, draft);
        section.addRequirementArtifact(draft);
        return saveChecked(draft);
    }

    /**
     * Prepares a new artifact for its first insert: sets the route's team and refuses its key if another artifact of
     * the team holds it (4b). Its id is never taken from a request body (no converter maps one, OI-46), and its version
     * is null until the insert, so the save inserts.
     */
    private void assignTeamAndCheckKeyAvailability(Integer teamId, RequirementArtifact newRequirementArtifact) {
        Team team = this.teamRepository.findById(teamId)
                .orElseThrow(() -> new ObjectNotFoundException("team", teamId));

        newRequirementArtifact.setTeam(team);
        requireKeyFree(teamId, newRequirementArtifact.getArtifactKey(), null);
    }

    /**
     * Renames the artifact key of {@code artifact}, already loaded through a team-scoped finder, to {@code newKey}: a
     * deliberate rename (UC-ART-edit-artifact, UC-GLO-rename-term, UC-DOC-edit-use-case). The prefix never changes, so
     * {@code newKey} must carry the type's own. A {@code newKey} equal to the current key changes nothing and checks
     * nothing, so every save may send the key it read. Otherwise the new key is checked as a create checks it, and the
     * change is flushed at once, so a teammate's key committed in between is refused here (10b) rather than at commit.
     * Links connect artifacts, not keys, so a rename touches no link. For example, renaming {@code FR-input-validation}
     * to {@code FR-validate-inputs} stores the new key, while renaming it to {@code FR-user-authentication}, which the
     * seed already holds, is refused naming that artifact.
     *
     * @throws RequirementArtifactRejectedException (400) for a missing or
     *                                              malformed key (6a) or one another artifact of the team holds (6b,
     *                                              10b, with the holder as data)
     */
    public RequirementArtifact renameKey(Integer teamId, RequirementArtifact artifact, String newKey) {
        if (Objects.equals(newKey, artifact.getArtifactKey())) {
            return artifact;
        }
        ArtifactKeys.requireValid(artifact.getType(), newKey);
        requireKeyFree(teamId, newKey, artifact.getId());
        artifact.setArtifactKey(newKey);
        return saveChecked(artifact);
    }

    /**
     * The artifact of team {@code teamId} that holds {@code artifactKey}, or empty when the key is free (the editor's
     * Availability check, UC-ART-create-artifact "Artifact key rules"). The key is matched as the column's collation
     * matches, which ignores case; the key field asks only about well-formed keys, and a create or rename checks the
     * format itself. Another team's artifact is never found: the lookup is scoped to the team.
     */
    public Optional<ArtifactKeyHolderDto> findKeyHolder(Integer teamId, String artifactKey) {
        if (artifactKey == null || artifactKey.isBlank()) {
            return Optional.empty();
        }
        return this.requirementArtifactRepository.findByTeamTeamIdAndArtifactKey(teamId, artifactKey).map(ArtifactKeyHolderDto::of);
    }

    // Refuses artifactKey when an artifact of the team other than the one with id selfId holds it (4b, 6b). selfId is
    // null for a new artifact, which has no id yet.
    private void requireKeyFree(Integer teamId, String artifactKey, Long selfId) {
        this.requirementArtifactRepository.findByTeamTeamIdAndArtifactKey(teamId, artifactKey)
                .filter(holder -> !holder.getId().equals(selfId))
                .ifPresent(holder -> {
                    throw keyInUse(artifactKey, ArtifactKeyHolderDto.of(holder), null);
                });
    }

    /**
     * Saves and flushes {@code artifact}, so a write that breaks the unique key index fails here, with the key known,
     * not later at commit. That violation means a teammate committed the same key after this request's check
     * ({@link #requireKeyFree}): the race of extensions 8c and 10b. It becomes a refusal naming the key, without the
     * holder: this transaction can only roll back now, and its session cannot be trusted after the failed flush, so it
     * reads nothing more. The editor then asks the key-holder lookup again, in a request of its own, and shows the
     * holder. For example: John and Eric both submit FR-export-csv, both checks find it free, Eric's insert commits
     * first, and John's fails on the index, so John reads "FR-export-csv is already used by another requirement artifact
     * of the team." and his key field then shows Eric's artifact. Any other integrity error is not about the key and
     * propagates unchanged.
     */
    private RequirementArtifact saveChecked(RequirementArtifact artifact) {
        try {
            return this.requirementArtifactRepository.saveAndFlush(artifact);
        } catch (DataIntegrityViolationException exception) {
            if (!ArtifactKeys.isKeyIndexViolation(exception)) {
                throw exception;
            }
            throw keyInUse(artifact.getArtifactKey(), null, exception);
        }
    }

    // "FR-user-authentication is already used by FR-user-authentication: User authentication.", with the holder as
    // the response's data so the editor can offer to open it, when the check found the holder (4b). The race (8c,
    // 10b) has no holder to name: "FR-export-csv is already used by another requirement artifact of the team.", with
    // cause the index violation the refusal replaces.
    private static RequirementArtifactRejectedException keyInUse(String artifactKey, ArtifactKeyHolderDto holder, Throwable cause) {
        if (holder == null) {
            return new RequirementArtifactRejectedException(artifactKey + " is already used by another requirement artifact of the team.", null, cause);
        }
        String name = holder.title() == null || holder.title().isBlank() ? holder.artifactKey() : holder.artifactKey() + ": " + holder.title();
        return new RequirementArtifactRejectedException(artifactKey + " is already used by " + name + ".", holder, cause);
    }

    /**
     * Saves the editable fields of artifact {@code requirementArtifactId} of {@code teamId} at {@code expectedVersion}
     * (UC-ART-edit-artifact). Its type and home never change here. Its key changes only when {@code update} carries a
     * different one, a deliberate rename through {@link #renameKey} (6a, 6b, 10b); the editor always sends the key it
     * read, so an ordinary edit renames nothing. The save is flushed so the returned artifact carries its new version,
     * which the editor sends with its next change.
     *
     * @throws RequirementArtifactRejectedException (400) if no version is given, the artifact's type has no list home
     *                                              (a glossary term, use case or condition, each edited through its
     *                                              own use case and lock), the home document is locked for review
     *                                              (PRE-3), or a new key is malformed or held by another artifact
     * @throws OptimisticLockException              (409) if {@code expectedVersion} is stale (extension 10a)
     */
    public RequirementArtifact updateRequirementArtifact(Integer teamId, Long requirementArtifactId, RequirementArtifact update, Integer expectedVersion) {
        if (expectedVersion == null) {
            throw new RequirementArtifactRejectedException("The requirement artifact's version is required to save a change.");
        }
        RequirementArtifact oldRequirementArtifact = this.requirementArtifactRepository.findByIdAndTeamTeamId(requirementArtifactId, teamId)
                .orElseThrow(() -> new ObjectNotFoundException("requirement artifact", requirementArtifactId));
        if (!oldRequirementArtifact.getType().hasListHome()) {
            // These have their own editors and rules: a use case is edited under its use case lock
            // (BR-edit-lock-required), a condition only on the condition itself (BR-shared-condition-wording), and a
            // glossary term through the glossary. This generic route would bypass all three.
            throw new RequirementArtifactRejectedException(capitalize(withArticle(label(oldRequirementArtifact.getType())))
                    + " is not edited here: glossary terms are edited in the glossary, and use cases and their conditions in the use case editor.");
        }
        if (!expectedVersion.equals(oldRequirementArtifact.getVersion())) {
            throw new OptimisticLockException("This requirement artifact has been changed by someone else. Please refresh and try again.");
        }
        if (oldRequirementArtifact.getSourceDocumentSection() != null) {
            requireNotLockedForReview(oldRequirementArtifact.getSourceDocumentSection().getDocument());
        }
        oldRequirementArtifact.setTitle(update.getTitle());
        oldRequirementArtifact.setContent(update.getContent());
        oldRequirementArtifact.setPriority(update.getPriority());
        oldRequirementArtifact.setNotes(update.getNotes());
        // The key last, after every other check, so a refused rename leaves nothing written (the transaction rolls
        // back). An unchanged key returns at once from renameKey, and the flush below then stores the other fields.
        renameKey(teamId, oldRequirementArtifact, update.getArtifactKey());
        return this.requirementArtifactRepository.saveAndFlush(oldRequirementArtifact);
    }

    /**
     * What refers to artifact {@code requirementArtifactId} of {@code teamId}, so that it cannot be deleted yet
     * (UC-ART-delete-artifact step 4, BR-deletion-integrity): every link with it at either end, and every use case
     * naming it as an actor. Links come first, then actor references, each group in artifact-key order. For example,
     * the seeded FR-user-authentication, linked to UC-create-use-case, gives one entry: (UC-create-use-case, LINK_FROM,
     * DERIVES_FROM). An empty list means nothing blocks the delete.
     *
     * @throws ObjectNotFoundException (404) if the artifact is not the team's
     */
    public List<ArtifactReferrerDto> findReferences(Integer teamId, Long requirementArtifactId) {
        RequirementArtifact artifact = this.findRequirementArtifactById(teamId, requirementArtifactId);
        return referrersOf(teamId, artifact);
    }

    /**
     * Everything in team {@code teamId} that refers to {@code artifact}, and so blocks its delete (BR-deletion-integrity):
     * the shared body of {@link #findReferences} (the editor's check before the confirm, step 4) and of
     * {@link #deleteRequirementArtifact} (the check again inside the delete, 4a). The caller has already loaded the
     * artifact through a team-scoped finder, so this does not check the team again.
     * <p>
     * Two queries, sorted together by artifact key: the links with the artifact as source ({@code LINK_FROM}, the other
     * artifact is the target), then those with it as target ({@code LINK_TO}, the other artifact is the source). A link
     * from the artifact to itself comes back from both queries; the second loop skips it, so it is listed once, as
     * {@code LINK_FROM}. A use case's actors are links too ({@code PRIMARY_ACTOR}, {@code SECONDARY_ACTOR}, from the use
     * case's own artifact, which carries the key and title the student recognizes), so they need no query of their own.
     * Examples from team 1's seed:
     * <pre>
     * FR-user-authentication   [(UC-create-use-case, LINK_FROM, DERIVES_FROM)]    it derives from the use case
     * BO-requirements-quality  [(UC-create-use-case, LINK_TO, DERIVES_FROM)]      the use case derives from it
     * STK-student              [(UC-create-use-case, LINK_TO, PRIMARY_ACTOR)]     the use case's primary actor
     * STK-instructor           [(UC-create-use-case, LINK_TO, SECONDARY_ACTOR)]   a secondary actor
     * FR-change-notification   []                                                  nothing refers to it: it can go
     * </pre>
     * The order is the one {@code docs/design/art.md} fixes (Refusals), and the 4a message lists the referrers in it too.
     */
    private List<ArtifactReferrerDto> referrersOf(Integer teamId, RequirementArtifact artifact) {
        Long id = artifact.getId();
        List<ArtifactReferrerDto> links = new ArrayList<>();
        // A link from the artifact to itself shows up in both queries; it is listed once, as LINK_FROM.
        for (ArtifactLink link : this.artifactLinkRepository.findBySourceArtifactId(id, teamId)) {
            links.add(referrer(link.getTargetArtifact(), Relation.LINK_FROM, link.getType()));
        }
        for (ArtifactLink link : this.artifactLinkRepository.findByTargetArtifactId(id, teamId)) {
            if (!id.equals(link.getSourceArtifact().getId())) {
                links.add(referrer(link.getSourceArtifact(), Relation.LINK_TO, link.getType()));
            }
        }
        links.sort(BY_ARTIFACT_KEY);
        return List.copyOf(links);
    }

    // Artifact keys in alphabetical order, a missing key last (ArtifactKeys.KEY_ORDER).
    private static final Comparator<ArtifactReferrerDto> BY_ARTIFACT_KEY = Comparator.comparing(ArtifactReferrerDto::artifactKey, ArtifactKeys.KEY_ORDER);

    private static ArtifactReferrerDto referrer(RequirementArtifact other, Relation relation, ArtifactLinkType linkType) {
        return new ArtifactReferrerDto(other.getId(), other.getArtifactKey(), other.getTitle(), relation, linkType);
    }

    /**
     * Deletes artifact {@code requirementArtifactId} of {@code teamId} for good (UC-ART-delete-artifact; the deletion is
     * physical, BR-deletion-integrity). Checks, in order: the artifact is the team's, its type has a list home, its home
     * document is not locked for review (PRE-3), {@code expectedVersion} is current (7a), and nothing refers to it (4a).
     * Then it takes the artifact out of its home's list under the same row lock a create takes, so the items after it
     * move up and the list keeps no gap (a gap in {@code artifact_index} would load as a null item), and deletes the
     * row. Its comment threads go with it by cascade; its links cannot, since a referenced artifact is refused, and the
     * entity does not cascade them.
     * <p>
     * The references are checked here, inside the delete, even though the editor asked {@link #findReferences} before
     * the confirm: a teammate may have linked the artifact since. A reference committed during this transaction is not
     * seen; the delete then fails on its foreign key (OI-75).
     *
     * @throws RequirementArtifactRejectedException (400) for a missing version, a type with no list home, a home
     *                                              document locked for review, or a referenced artifact (with the
     *                                              referrers as data)
     * @throws OptimisticLockException              (409) if {@code expectedVersion} is stale (7a)
     * @throws ObjectNotFoundException              (404) if the artifact is not the team's
     */
    // READ COMMITTED, as on the create: the list loaded after the row lock must include every item a teammate has
    // committed, and the reference check must see a link committed after the editor's check. Named in full: the class
    // imports jakarta.transaction.Transactional, which has no isolation setting.
    @org.springframework.transaction.annotation.Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteRequirementArtifact(Integer teamId, Long requirementArtifactId, Integer expectedVersion) {
        // The checks run in a fixed order and the first failure stops the delete, before anything is written. The
        // examples below use team 1's seed: FR-user-authentication (linked to UC-create-use-case),
        // FR-change-notification (nothing refers to it), stakeholder STK-student (UC-create-use-case's primary actor),
        // and the glossary term "Artifact".

        // 1. A version is required (7a). The editor always sends the version it last read, for example DELETE
        //    /teams/1/requirement-artifacts/11?version=0 for FR-change-notification; a call without one is refused
        //    here, before any read, as the PUT does.
        if (expectedVersion == null) {
            throw new RequirementArtifactRejectedException("The requirement artifact's version is required to delete it.");
        }

        // 2. The artifact must be the team's (BR-team-scoped-access). Loading by id AND team is the check: Woody, on
        //    team 2, sending FR-change-notification's id 11 through /teams/2/... finds nothing and gets 404, never team
        //    1's row.
        RequirementArtifact existing = this.findRequirementArtifactById(teamId, requirementArtifactId);

        // 3. Only a type with a list home is deleted here. A glossary term, use case or condition has its own delete
        //    use case and rules (UC-GLO-delete-term, the use case editor), so deleting the "Artifact" glossary term
        //    through this route is refused with "A glossary term is not deleted here: ...".
        if (!existing.getType().hasListHome()) {
            throw new RequirementArtifactRejectedException(capitalize(withArticle(label(existing.getType())))
                    + " is not deleted here: glossary terms are deleted in the glossary, and use cases and their conditions in the use case editor.");
        }

        // 4. The home document must not be locked for review (PRE-3, BR-review-lock). While team 1's SRS is
        //    SUBMITTED, nobody can delete FR-change-notification, just as nobody can edit it. This reads the home
        //    section and its document, but not the section's requirementArtifacts: that list must load only after the
        //    row lock in step 7, or it could come from a snapshot that misses a teammate's create, and closing up the
        //    list would then leave a gap.
        DocumentSection home = existing.getSourceDocumentSection();
        if (home != null) {
            requireNotLockedForReview(home.getDocument());
        }

        // 5. The version must be current (7a). John opened FR-change-notification at version 0; Eric then saved a
        //    new wording (version 1). John's delete with version=0 is refused with 409, so he never deletes text he has
        //    not seen; the editor then shows him FR-change-notification's current details.
        if (!expectedVersion.equals(existing.getVersion())) {
            throw new OptimisticLockException("This requirement artifact has been changed by someone else. Please refresh and try again.");
        }

        // 6. Nothing may refer to it (4a, BR-deletion-integrity). The editor asked findReferences before the
        //    confirm, but a teammate may have linked the artifact since, so the check runs again here, at READ
        //    COMMITTED, where it sees every committed link. Deleting FR-user-authentication is refused with
        //    "FR-user-authentication cannot be deleted while other artifacts refer to it: the link
        //    FR-user-authentication DERIVES_FROM UC-create-use-case. ...", and deleting STK-student with "... the link
        //    UC-create-use-case PRIMARY_ACTOR STK-student. ...". The referrers also go back as the response's data, so
        //    the editor can list them.
        List<ArtifactReferrerDto> referrers = referrersOf(teamId, existing);
        if (!referrers.isEmpty()) {
            throw new RequirementArtifactRejectedException(existing.getArtifactKey() + " cannot be deleted while other artifacts refer to it: "
                    + describe(existing, referrers) + ". Remove or repoint these references first.", referrers);
        }

        // 7. Take it out of its home list. Lock the home section's row first, then load the list, so a create or
        //    delete running at the same time in that section waits its turn, and the list read here holds every
        //    committed item. Removing FR-change-notification from section 16's list [FR-user-authentication,
        //    FR-input-validation, FR-change-notification, FR-audit-log] makes Hibernate rewrite the indexes of the
        //    items after it, so FR-audit-log moves from index 3 to 2 and the list keeps no gap (a gap would load as a
        //    null item). Since OI-73 every list type has a home; one found without is deleted without this step.
        if (home != null) {
            Long sectionId = home.getId();
            DocumentSection section = this.documentSectionRepository.findByIdForUpdate(sectionId)
                    .orElseThrow(() -> new ObjectNotFoundException("document section", sectionId));
            section.getRequirementArtifacts().remove(existing);
        }

        // 8. Delete the row, for good (the deletion is physical). Hibernate issues DELETE ... WHERE id = ? AND
        //    version = ?, so an edit committed between step 5 and here matches no row and also ends in 409.
        //    FR-change-notification's comment threads and their comments go with it by cascade. Its links cannot: step
        //    6 refused any, and the entity does not cascade them, so a link committed during this transaction (OI-75)
        //    makes the delete fail on its foreign key instead of being deleted with FR-change-notification.
        this.requirementArtifactRepository.delete(existing);
    }

    // "the link FR-user-authentication DERIVES_FROM UC-create-use-case; the link UC-create-use-case PRIMARY_ACTOR
    // STK-student": a link always reads source, type, target.
    private static String describe(RequirementArtifact deleted, List<ArtifactReferrerDto> referrers) {
        return referrers.stream().map(referrer -> switch (referrer.relation()) {
            case LINK_FROM ->
                    "the link " + deleted.getArtifactKey() + " " + referrer.linkType() + " " + referrer.artifactKey();
            case LINK_TO ->
                    "the link " + referrer.artifactKey() + " " + referrer.linkType() + " " + deleted.getArtifactKey();
        }).collect(Collectors.joining("; "));
    }

    /**
     * The home of {@code type} (BR-artifact-home): the one (document type, section key) pair the templates give it,
     * read by {@link DocumentTemplateRegistry#homeOf}. For example:
     * <pre>
     * RISK                 (VISION_SCOPE, "RISKS")
     * BUSINESS_ASSUMPTION  (VISION_SCOPE, "ASSUMPTIONS_DEPENDENCIES")
     * ASSUMPTION           (SRS, "ASSUMPTIONS_DEPENDENCIES")
     * </pre>
     * Called only for a type that {@link RequirementArtifactType#hasListHome() has a list home}.
     */
    private Home resolveHome(RequirementArtifactType type) {
        // The startup check (DocumentTemplateRegistry.buildHomes) gives every type with a list home exactly one home.
        return this.documentTemplateRegistry.homeOf(type)
                .orElseThrow(() -> new IllegalStateException("The templates give " + type + " no home."));
    }

    /**
     * The generic endpoint's refusal for a type with no list home (glossary term, use case, precondition,
     * postcondition, user story): each is created through its own editor, so {@code RequirementArtifactController}
     * refuses it there. For example: "A glossary term is not created here: glossary terms are created in the glossary,
     * and use cases and their conditions in the use case editor."
     */
    static RequirementArtifactRejectedException notCreatedHere(RequirementArtifactType type) {
        return new RequirementArtifactRejectedException(capitalize(withArticle(label(type)))
                + " is not created here: glossary terms are created in the glossary, and use cases and their conditions in the use case editor.");
    }

    /**
     * Refuses a change to a document locked for review (BR-review-lock): {@code SUBMITTED}, until an instructor returns
     * it. ({@code ACCEPTED} joins this once the review use cases add it.)
     */
    private static void requireNotLockedForReview(RequirementDocument document) {
        if (document.getStatus() == DocumentStatus.SUBMITTED) {
            throw new RequirementArtifactRejectedException("The " + document.getTitle() + " is locked for review, so it cannot be changed.");
        }
    }

    private String documentTitle(DocumentType documentType) {
        return this.documentTemplateRegistry.find(documentType).map(DocumentTemplate::getTitle).orElse(documentType.name());
    }

    private static String capitalize(String text) {
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

}
