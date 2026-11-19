package team.projectpulse.ram.requirement;

import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
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
import team.projectpulse.system.exception.ObjectNotFoundException;
import team.projectpulse.system.exception.RequirementArtifactRejectedException;
import team.projectpulse.team.Team;
import team.projectpulse.team.TeamRepository;

import java.util.Locale;
import java.util.Map;

@Service
@Transactional
public class RequirementArtifactService {

    private final RequirementArtifactRepository requirementArtifactRepository;
    private final TeamRepository teamRepository;
    private final ArtifactKeySequenceRepository artifactKeySequenceRepository;
    private final DocumentRepository documentRepository;
    private final DocumentSectionRepository documentSectionRepository;
    private final DocumentTemplateRegistry documentTemplateRegistry;

    public RequirementArtifactService(RequirementArtifactRepository requirementArtifactRepository, TeamRepository teamRepository, ArtifactKeySequenceRepository artifactKeySequenceRepository,
                                      DocumentRepository documentRepository, DocumentSectionRepository documentSectionRepository, DocumentTemplateRegistry documentTemplateRegistry) {
        this.requirementArtifactRepository = requirementArtifactRepository;
        this.teamRepository = teamRepository;
        this.artifactKeySequenceRepository = artifactKeySequenceRepository;
        this.documentRepository = documentRepository;
        this.documentSectionRepository = documentSectionRepository;
        this.documentTemplateRegistry = documentTemplateRegistry;
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
     * BR-artifact-home holds for every caller. The team comes from the route and the artifact key is minted here
     * (BR-artifact-key-unique), replacing whatever the object carried. The object must be new: every caller builds it
     * with {@code new}, and no converter maps an id (OI-46), so its null version makes the save an insert.
     * <p>
     * The type decides what else happens:
     * <ul>
     *   <li>A type with no list home (glossary term, use case, precondition, postcondition, user story) is only keyed
     *   and inserted, with any document section reference cleared. Its own service calls this
     *   ({@code GlossaryService}, {@code UseCaseService}), and so does the dev seed. The call joins the caller's
     *   transaction when there is one (the services'), and runs in its own when there is none (the seed's, so each
     *   seeded artifact commits on its own). This branch reads no document and takes no lock. The generic endpoint refuses
     *   these types before calling here ({@link #notCreatedHere}), since each has its own editor.</li>
     *   <li>A list type becomes an item of its home document section, appended at the end (UC-ART-create-artifact).
     *   The home comes from the type alone. The document section's own lock is not checked: the section view offers
     *   Add only to its holder, as a UI gate (BR-edit-lock-required).</li>
     * </ul>
     * <p>
     * Concurrency (list types): the home section's row is locked ({@link DocumentSectionRepository#findByIdForUpdate}) before its
     * list is read, so two creates in one section append one after the other instead of taking the same
     * {@code artifact_index}. The method runs at {@code READ COMMITTED} because under MySQL's default
     * {@code REPEATABLE READ} the snapshot fixed by the first plain read below would hide a create committed while this
     * one waited for the lock. The document section's version is left alone: the artifact is its own aggregate, and
     * {@code requirementArtifacts} is the {@code mappedBy} side, so the teammate editing the section's prose is not
     * made stale.
     *
     * @throws RequirementArtifactRejectedException (400) for a list type whose home document is not created (8b), or is
     *                                              locked for review (8a)
     */
    // READ COMMITTED, so the list loaded after the row lock sees a teammate's just-committed item. Under MySQL's default
    // REPEATABLE READ it would read the snapshot from before the lock, and two concurrent creates could take the same
    // position. Named in full: the class imports jakarta.transaction.Transactional, which has no isolation setting.
    @org.springframework.transaction.annotation.Transactional(isolation = Isolation.READ_COMMITTED)
    public RequirementArtifact createRequirementArtifact(Integer teamId, RequirementArtifact draft) {
        if (draft.getType() == null) {
            throw new IllegalArgumentException("RequirementArtifact.type is required to generate artifactKey.");
        }
        // No list home (a glossary term, use case, precondition, postcondition or user story): it sits in no document
        // section's list, so there is no home to look up, no document to check and no row to lock. It only gets the
        // route's team and its key, then is inserted. For example, a new glossary term from GlossaryService becomes
        // GLO-n, and a use case's new precondition from UseCaseService becomes PRC-n.
        if (!draft.getType().hasListHome()) {
            // Cleared, so a draft that arrives carrying a document section still lands in no list (BR-artifact-home).
            draft.setSourceDocumentSection(null);
            assignTeamAndKey(teamId, draft);
            return this.requirementArtifactRepository.save(draft);
        }

        // For example: a RISK resolves to (VISION_SCOPE, "RISKS"), an ASSUMPTION to (SRS, "ASSUMPTIONS_DEPENDENCIES").
        Home home = resolveHome(draft.getType());

        RequirementDocument document = this.documentRepository.findByTeamTeamIdAndType(teamId, home.documentType())
                .orElseThrow(() -> new RequirementArtifactRejectedException("The team's " + documentTitle(home.documentType())
                        + " has not been created yet. The course admin creates the team's documents."));
        requireNotLockedForReview(document);

        // Two steps: find the home section's id with a plain read, then lock that one row by primary key. A FOR UPDATE on
        // this lookup could lock more than one row (the index range it scans), blocking creates in other sections. The
        // two calls return the same object, and its requirementArtifacts list is lazy: leave it untouched until the lock
        // is held, so it loads with every item a teammate has committed and the new item goes at the true end. Reading it
        // here would keep an outdated copy, and two concurrent creates could both take the same position.
        DocumentSection found = this.documentSectionRepository.findByDocumentIdAndSectionKey(document.getId(), home.sectionKey())
                .orElseThrow(() -> new RequirementArtifactRejectedException("The team's " + document.getTitle()
                        + " was created from an older template and has no list for " + withArticle(label(draft.getType())) + "."));
        DocumentSection section = this.documentSectionRepository.findByIdForUpdate(found.getId())
                .orElseThrow(() -> new ObjectNotFoundException("document section", found.getId()));

        // Team and key first: minting the key runs a query, and a query may flush pending changes, so the draft must not
        // be in the section's cascading list yet. Then appending sets its home, and the row is inserted once, keyed and
        // already in its list, with its index taken from its position at the end of the list just read under the lock.
        assignTeamAndKey(teamId, draft);
        section.addRequirementArtifact(draft);
        return this.requirementArtifactRepository.save(draft);
    }

    /**
     * Prepares a new artifact for its first insert: sets the route's team and mints its key. Its id is never taken from
     * a request body (no converter maps one, OI-46), and its version is null until the insert, so the save inserts.
     */
    private void assignTeamAndKey(Integer teamId, RequirementArtifact newRequirementArtifact) {
        Team team = this.teamRepository.findById(teamId)
                .orElseThrow(() -> new ObjectNotFoundException("team", teamId));

        newRequirementArtifact.setTeam(team);
        newRequirementArtifact.setArtifactKey(generateNextArtifactKey(teamId, newRequirementArtifact.getType()));
    }

    /**
     * Saves the editable fields of artifact {@code requirementArtifactId} of {@code teamId} at {@code expectedVersion}
     * (UC-ART-edit-artifact). Its type, key and home never change here. The save is flushed so the returned artifact
     * carries its new version, which the editor sends with its next change.
     *
     * @throws RequirementArtifactRejectedException (400) if no version is given, the artifact's type has no list home
     *                                              (a glossary term, use case or condition, each edited through its
     *                                              own use case and lock), or the home document is locked for review
     *                                              (PRE-3)
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
        return this.requirementArtifactRepository.saveAndFlush(oldRequirementArtifact);
    }

    /**
     * Deletes artifact {@code requirementArtifactId} of {@code teamId}. An artifact in a list is first taken out of its
     * home's list, under the same row lock a create takes, so the items after it move up and the list keeps no gap: a
     * gap in {@code artifact_index} would load as a null item. The logical delete and the referenced-artifact block are
     * UC-ART-delete-artifact's, not yet designed; the editor offers no Remove until then.
     */
    public void deleteRequirementArtifact(Integer teamId, Long requirementArtifactId) {
        RequirementArtifact existing = this.findRequirementArtifactById(teamId, requirementArtifactId);
        if (existing.getSourceDocumentSection() != null) {
            Long sectionId = existing.getSourceDocumentSection().getId();
            DocumentSection section = this.documentSectionRepository.findByIdForUpdate(sectionId)
                    .orElseThrow(() -> new ObjectNotFoundException("document section", sectionId));
            section.getRequirementArtifacts().remove(existing);
        }
        this.requirementArtifactRepository.delete(existing);
    }

    /**
     * The home of {@code type} (BR-artifact-home): the one (document type, section key) pair the templates give it, read
     * by {@link DocumentTemplateRegistry#homeOf}. For example:
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
     * The generic endpoint's refusal for a type with no list home (glossary term, use case, precondition, postcondition,
     * user story): each is created through its own editor, so {@code RequirementArtifactController} refuses it there.
     * For example: "A glossary term is not created here: glossary terms are created in the glossary, and use cases and
     * their conditions in the use case editor."
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

    // FUNCTIONAL_REQUIREMENT -> "functional requirement", for messages written to the student.
    private static String label(RequirementArtifactType type) {
        return type.name().toLowerCase(Locale.ROOT).replace('_', ' ');
    }

    // "an" before a vowel sound. Every type label that starts with "u" (use case, user story, user interface) starts
    // with a "you" sound, so "u" takes "a": "a use case", not "an use case".
    private static String withArticle(String noun) {
        return ("aeio".indexOf(noun.charAt(0)) >= 0 ? "an " : "a ") + noun;
    }

    private static String capitalize(String text) {
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    public String generateNextArtifactKey(Integer teamId, RequirementArtifactType type) {
        String prefix = ArtifactKeyPrefix.of(type);

        // Lock the counter row for this (team, type)
        ArtifactKeySequence seq = artifactKeySequenceRepository.findForUpdate(teamId, type)
                .orElseGet(() -> artifactKeySequenceRepository.save(new ArtifactKeySequence(
                        teamRepository.getReferenceById(teamId), type
                )));

        long number = seq.getNextNumber();
        seq.setNextNumber(number + 1);
        artifactKeySequenceRepository.save(seq);

        return prefix + "-" + number;
    }

}
