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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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
     * Creates {@code draft} in team {@code teamId} as an item of its home document section, appended at the end
     * (UC-ART-create-artifact). The home comes from the artifact's type, and for a two-home type from
     * {@code homeDocumentType} (BR-artifact-home); nothing in the request names a document or a section. The document
     * section's own lock is not checked: the section view offers Add only to its holder, as a UI gate
     * (BR-edit-lock-required).
     * <p>
     * Concurrency: the home section's row is locked ({@link DocumentSectionRepository#findByIdForUpdate}) before its
     * list is read, so two creates in one section append one after the other instead of taking the same
     * {@code artifact_index}. The method runs at {@code READ COMMITTED} because under MySQL's default
     * {@code REPEATABLE READ} the snapshot fixed by the first plain read below would hide a create committed while this
     * one waited for the lock. The document section's version is left alone: the artifact is its own aggregate, and
     * {@code requirementArtifacts} is the {@code mappedBy} side, so the teammate editing the section's prose is not
     * made stale.
     *
     * @throws RequirementArtifactRejectedException (400) for a type with no list home, a missing or wrong home, a home
     *                                              document not created (8b), or one locked for review (8a)
     */
    @org.springframework.transaction.annotation.Transactional(isolation = Isolation.READ_COMMITTED)
    public RequirementArtifact createRequirementArtifact(Integer teamId, RequirementArtifact draft, DocumentType homeDocumentType) {
        Home home = resolveHome(draft.getType(), homeDocumentType);

        RequirementDocument document = this.documentRepository.findByTeamTeamIdAndType(teamId, home.documentType())
                .orElseThrow(() -> new RequirementArtifactRejectedException("The team's " + documentTitle(home.documentType())
                        + " has not been created yet. The course admin creates the team's documents."));
        requireNotLockedForReview(document);

        // A plain lookup that leaves requirementArtifacts unread: the list must be read only after the lock below.
        DocumentSection found = this.documentSectionRepository.findByDocumentIdAndSectionKey(document.getId(), home.sectionKey())
                .orElseThrow(() -> new RequirementArtifactRejectedException("The team's " + document.getTitle()
                        + " was created from an older template and has no list for " + withArticle(label(draft.getType())) + "."));
        DocumentSection section = this.documentSectionRepository.findByIdForUpdate(found.getId())
                .orElseThrow(() -> new ObjectNotFoundException("document section", found.getId()));

        // Appending first sets the artifact's home, so the row is inserted once, already in its list; the index is
        // written from its position at the end of the list just read under the lock.
        section.addRequirementArtifact(draft);
        return saveRequirementArtifact(teamId, draft);
    }

    /**
     * Saves {@code newRequirementArtifact} in team {@code teamId}: it always inserts a new row, never updates one. The
     * team comes from the route and the artifact key is minted here (BR-artifact-key-unique), whatever the object
     * carried.
     */
    public RequirementArtifact saveRequirementArtifact(Integer teamId, RequirementArtifact newRequirementArtifact) {
        // Every caller creates, and the object may come straight from a request body, whose id names any row in any
        // team. Saving an entity that has an id merges it over that row, so the id is cleared first: a create can
        // never overwrite another artifact, whatever the converter mapped (OI-46). Updates load the stored row through
        // a team-scoped lookup and save that row instead (updateRequirementArtifact).
        newRequirementArtifact.setId(null);

        Team team = this.teamRepository.findById(teamId)
                .orElseThrow(() -> new ObjectNotFoundException("team", teamId));

        newRequirementArtifact.setTeam(team);

        if (newRequirementArtifact.getType() == null) {
            throw new IllegalArgumentException("RequirementArtifact.type is required to generate artifactKey.");
        }

        // Generate and set the artifact key
        newRequirementArtifact.setArtifactKey(generateNextArtifactKey(teamId, newRequirementArtifact.getType()));

        return this.requirementArtifactRepository.save(newRequirementArtifact);
    }

    /**
     * Saves the editable fields of artifact {@code requirementArtifactId} of {@code teamId} at {@code expectedVersion}
     * (UC-ART-edit-artifact). Its type, key and home never change here. The save is flushed so the returned artifact
     * carries its new version, which the editor sends with its next change.
     *
     * @throws RequirementArtifactRejectedException (400) if no version is given, or the home document is locked for
     *                                              review (PRE-3); an artifact with no home skips that check
     * @throws OptimisticLockException              (409) if {@code expectedVersion} is stale (extension 10a)
     */
    public RequirementArtifact updateRequirementArtifact(Integer teamId, Long requirementArtifactId, RequirementArtifact update, Integer expectedVersion) {
        if (expectedVersion == null) {
            throw new RequirementArtifactRejectedException("The requirement artifact's version is required to save a change.");
        }
        RequirementArtifact oldRequirementArtifact = this.requirementArtifactRepository.findByIdAndTeamTeamId(requirementArtifactId, teamId)
                .orElseThrow(() -> new ObjectNotFoundException("requirement artifact", requirementArtifactId));
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

    public void deleteRequirementArtifact(Integer teamId, Long requirementArtifactId) {
        RequirementArtifact existing = this.findRequirementArtifactById(teamId, requirementArtifactId);
        this.requirementArtifactRepository.delete(existing);
    }

    /**
     * The one home {@code type} goes to: its only home, or, for a two-home type, the one in {@code homeDocumentType}.
     */
    private Home resolveHome(RequirementArtifactType type, DocumentType homeDocumentType) {
        if (!type.hasListHome()) {
            throw new RequirementArtifactRejectedException(capitalize(withArticle(label(type)))
                    + " is not created here: glossary terms are created in the glossary, and use cases and their conditions in the use case editor.");
        }
        List<Home> homes = this.documentTemplateRegistry.homesOf(type);
        String homeTitles = homes.stream().map(h -> documentTitle(h.documentType())).collect(Collectors.joining(" or the "));
        if (homeDocumentType == null) {
            if (homes.size() == 1) {
                return homes.get(0);
            }
            throw new RequirementArtifactRejectedException("Choose whether this " + label(type) + " belongs in the " + homeTitles + ".");
        }
        return homes.stream()
                .filter(h -> h.documentType() == homeDocumentType)
                .findFirst()
                .orElseThrow(() -> new RequirementArtifactRejectedException(capitalize(withArticle(label(type))) + " belongs in the "
                        + homeTitles + ", not the " + documentTitle(homeDocumentType) + "."));
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

    private static String withArticle(String noun) {
        return ("aeiou".indexOf(noun.charAt(0)) >= 0 ? "an " : "a ") + noun;
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
