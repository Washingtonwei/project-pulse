package team.projectpulse.ram.usecase;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.OptimisticLockException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.requirement.RequirementArtifactService;
import team.projectpulse.ram.requirement.RequirementArtifactType;
import team.projectpulse.system.UserUtils;
import team.projectpulse.system.exception.ObjectNotFoundException;
import team.projectpulse.system.exception.SectionAlreadyLockedException;
import team.projectpulse.system.exception.SectionLockRequiredException;
import team.projectpulse.system.exception.SectionUnlockNotAllowedException;
import team.projectpulse.user.PeerEvaluationUser;
import team.projectpulse.user.UserRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional
public class UseCaseService {

    /** The artifact types that may serve as a use case's actor (BR-use-case-actor-types). */
    private static final Set<RequirementArtifactType> ACTOR_TYPES = EnumSet.of(RequirementArtifactType.STAKEHOLDER, RequirementArtifactType.EXTERNAL_SYSTEM);

    private final Duration defaultLockTtl;
    private final UseCaseRepository useCaseRepository;
    private final RequirementArtifactService requirementArtifactService;
    private final UserUtils userUtils;
    private final UserRepository userRepository;
    private final EntityManager entityManager;


    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "The EntityManager is Spring's shared, transaction-scoped proxy; holding a reference to it is how it is meant to be used.")
    public UseCaseService(UseCaseRepository useCaseRepository, RequirementArtifactService requirementArtifactService, UserUtils userUtils, UserRepository userRepository,
                          EntityManager entityManager, @Value("${ram.lock.default-lock-ttl:PT15M}") Duration defaultLockTtl) {
        this.useCaseRepository = useCaseRepository;
        this.entityManager = entityManager;
        this.requirementArtifactService = requirementArtifactService;
        this.userUtils = userUtils;
        this.userRepository = userRepository;
        this.defaultLockTtl = defaultLockTtl;
    }

    /**
     * Finds a use case by ID with all related entities fully loaded.
     * Uses the 3-query approach to avoid big Cartesian products:
     * 1. Load use case with scalar relationships (artifact, primaryActor)
     * 2. Load ManyToMany collections (secondaryActors, preconditions, postconditions)
     * 3. Load the main steps graph (mainSteps -> extensions -> extension steps)
     * <p>
     * All queries execute within the same transaction/persistence context,
     * so Hibernate automatically merges them into a single use case entity.
     *
     * @param teamId the owning team ID, used to scope the entry query
     * @param id the use case ID
     * @return the fully loaded use case
     * @throws ObjectNotFoundException if the use case is not found, or belongs to another team
     */
    public UseCase findUseCaseByIdWithFullGraph(Integer teamId, Long id) {
        // Query 1: Load UseCase with scalar relationships, scoped to the owning team.
        // Queries 2 and 3 only hydrate collections on an entity this query already
        // authorized, so they stay keyed on id alone.
        UseCase useCase = this.useCaseRepository.findByIdWithScalars(id, teamId)
                .orElseThrow(() -> new ObjectNotFoundException("use case", id));

        // Query 2: Load ManyToMany collections
        // This populates secondaryActors, preconditions, postconditions
        this.useCaseRepository.findByIdWithManyToMany(id);

        // Query 3: Load the step hierarchy
        // This populates mainSteps -> extensions -> steps
        this.useCaseRepository.findByIdWithStepGraph(id);

        // Return the fully loaded entity
        // All collections are now populated in the same instance
        useCase.initLockIfMissing();
        return useCase;
    }

    /**
     * Finds a UseCase by ID with only basic information (no collections loaded).
     * Useful for listing or when you don't need the full graph.
     *
     * @param teamId the owning team ID, used to scope the query
     * @param id the UseCase ID
     * @return the UseCase with only artifact loaded
     * @throws ObjectNotFoundException if the UseCase is not found, or belongs to another team
     */
    public UseCase findUseCaseByIdBasic(Integer teamId, Long id) {
        UseCase useCase = this.useCaseRepository.findByIdWithScalars(id, teamId)
                .orElseThrow(() -> new ObjectNotFoundException("use case", id));
        useCase.initLockIfMissing();
        return useCase;
    }

    /**
     * Saves a new use case for {@code teamId}. Its steps, extensions and extension steps are saved as fresh copies of
     * the ones passed in, so the caller's own step objects are not the ones persisted: read saved steps from the
     * returned use case.
     * <p>
     * The actors come from {@code references} alone, resolved in {@code teamId}; any actor already set on
     * {@code useCase} is replaced. See {@link #resolveActors}.
     *
     * @throws ObjectNotFoundException  if an actor id names no artifact of this team
     * @throws IllegalArgumentException if an actor is not a stakeholder or an external system
     */
    public UseCase saveUseCase(Integer teamId, UseCase useCase, UseCaseReferences references) {
        useCase.initLockIfMissing(); // Ensure lock is initialized for new use cases

        // Fresh copies drop any step, extension or extension-step id the request body carried, which would
        // otherwise reach save() on a child entity (OI-46).
        useCase.replaceMainSteps(useCase.getMainSteps().stream().map(UseCaseMainStep::copyOf).toList());

        resolveActors(teamId, references, useCase);
        // A new use case references nothing yet, so every other use case counts as "another" one.
        List<PlannedCondition> preconditions = planConditions(teamId, references.preconditions(), RequirementArtifactType.PRECONDITION, NO_USE_CASE);
        List<PlannedCondition> postconditions = planConditions(teamId, references.postconditions(), RequirementArtifactType.POSTCONDITION, NO_USE_CASE);

        // Sets the artifact's team from the route and mints its key. Saved before the new conditions, so the use
        // case takes the lower id, as it did when the conditions were cascaded.
        this.requirementArtifactService.saveRequirementArtifact(teamId, useCase.getArtifact());
        useCase.replacePreconditions(applyConditions(teamId, preconditions, RequirementArtifactType.PRECONDITION));
        useCase.replacePostconditions(applyConditions(teamId, postconditions, RequirementArtifactType.POSTCONDITION));
        return this.useCaseRepository.save(useCase);
    }

    public UseCaseLock findUseCaseLock(Integer teamId, Long useCaseId) {
        UseCase useCase = this.useCaseRepository.findByIdWithScalars(useCaseId, teamId)
                .orElseThrow(() -> new ObjectNotFoundException("use case", useCaseId));

        useCase.initLockIfMissing();
        UseCaseLock lock = useCase.getLock();

        Instant now = Instant.now();
        if (lock.isExpired(now)) {
            lock.unlock();
        }

        return lock;
    }

    public UseCaseLock lockUseCase(Integer teamId, Long useCaseId, String reason) {
        UseCaseLock useCaseLock = findUseCaseLock(teamId, useCaseId);
        Integer currentUserId = this.userUtils.getUserId();

        PeerEvaluationUser currentUser = this.userRepository.findById(currentUserId)
                .orElseThrow(() -> new ObjectNotFoundException("user", currentUserId));

        Instant now = Instant.now();
        Instant expiresAt = now.plus(defaultLockTtl);

        if (useCaseLock.isExpired(now)) {
            useCaseLock.unlock();
        }

        if (useCaseLock.isLocked(now)) {
            if (useCaseLock.getLockedBy() != null && useCaseLock.getLockedBy().getId().equals(currentUserId)) {
                useCaseLock.extend(expiresAt, now);
                return useCaseLock;
            }

            String lockerName = useCaseLock.getLockedBy() != null ? useCaseLock.getLockedBy().getFirstName() + " " + useCaseLock.getLockedBy().getLastName() : "another user";
            String until = useCaseLock.getExpiresAt() != null ? useCaseLock.getExpiresAt().toString() : "(no expiry)";
            throw new SectionAlreadyLockedException("Use case is locked by " + lockerName + " until " + until + ".");
        }

        useCaseLock.lock(currentUser, now, expiresAt, reason);
        return useCaseLock;
    }

    public void unlockUseCase(Integer teamId, Long useCaseId) {
        UseCaseLock useCaseLock = findUseCaseLock(teamId, useCaseId);
        Integer currentUserId = this.userUtils.getUserId();

        if (this.userUtils.hasRole("ROLE_instructor")) {
            useCaseLock.unlock();
            return;
        }

        Integer ownerId = useCaseLock.getLockedBy() != null ? useCaseLock.getLockedBy().getId() : null;
        if (ownerId == null || !ownerId.equals(currentUserId)) {
            throw new SectionUnlockNotAllowedException("Only the lock owner or an instructor can unlock this use case.");
        }

        useCaseLock.unlock();
    }

    /**
     * Updates use case {@code useCaseId} of {@code teamId} from {@code update} at {@code expectedVersion}; the caller
     * must hold the use case lock. The actors come from {@code references} alone; see {@link #resolveActors}.
     *
     * @throws ObjectNotFoundException  if the use case, or an actor id, names nothing in this team
     * @throws IllegalArgumentException if an actor is not a stakeholder or an external system, or no version is given
     * @throws OptimisticLockException  if {@code expectedVersion} is stale
     */
    public UseCase updateUseCase(Integer teamId, Long useCaseId, UseCase update, UseCaseReferences references, Integer expectedVersion) {
        UseCase oldUseCase = this.useCaseRepository.findByIdAndArtifactTeamTeamId(useCaseId, teamId)
                .orElseThrow(() -> new ObjectNotFoundException("use case", useCaseId));
        // Load the whole step tree in one query, rather than one query per step and per extension once the steps
        // are revised below. The team-scoped query above has just authorized this id in this transaction, which is
        // the condition findByIdWithStepGraph requires, and nothing is modified yet, so the query flushes nothing.
        this.useCaseRepository.findByIdWithStepGraph(useCaseId);
        oldUseCase.initLockIfMissing();

        // Resolve and check the incoming references before doing any version or lock work, and before any write.
        resolveActors(teamId, references, update);
        List<PlannedCondition> preconditions = planConditions(teamId, references.preconditions(), RequirementArtifactType.PRECONDITION, useCaseId);
        List<PlannedCondition> postconditions = planConditions(teamId, references.postconditions(), RequirementArtifactType.POSTCONDITION, useCaseId);

        if (expectedVersion == null) {
            throw new IllegalArgumentException("Use case version is required for update.");
        }

        Integer currentVersion = oldUseCase.getVersion();
        if (!expectedVersion.equals(currentVersion)) {
            throw new OptimisticLockException("Use case has been updated by another user. Please refresh and try again.");
        }

        Instant now = Instant.now();
        UseCaseLock lock = oldUseCase.getLock();
        if (lock.isExpired(now)) {
            lock.unlock();
        }

        Integer currentUserId = this.userUtils.getUserId();
        if (!lock.isLocked(now)) {
            throw new SectionLockRequiredException("You must first lock this use case before updating it.");
        }

        Integer ownerId = lock.getLockedBy() != null ? lock.getLockedBy().getId() : null;
        if (ownerId == null || !ownerId.equals(currentUserId)) {
            throw new SectionAlreadyLockedException("Use case is locked by another user. You cannot update it.");
        }

        // Every accepted update increments the version, so the next save from a stale form is refused by the check
        // above. Hibernate only increments it by itself when a UseCase field or collection is dirty, and most of a
        // use case lives elsewhere: the title and description on its artifact, and the step text on child rows that
        // revising updates in place. PESSIMISTIC_FORCE_INCREMENT writes the new version now, guarded by the old one,
        // rather than just before commit (as OPTIMISTIC_FORCE_INCREMENT would), and sets it on this entity, so the
        // response carries the version the next save must send.
        this.entityManager.lock(oldUseCase, LockModeType.PESSIMISTIC_FORCE_INCREMENT);

        // Update artifact fields
        oldUseCase.getArtifact().setTitle(update.getArtifact().getTitle());
        oldUseCase.getArtifact().setContent(update.getArtifact().getContent());
        oldUseCase.getArtifact().setPriority(update.getArtifact().getPriority());
        oldUseCase.getArtifact().setNotes(update.getArtifact().getNotes());

        // Update scalar relationships
        oldUseCase.setPrimaryActor(update.getPrimaryActor());
        oldUseCase.setUseCaseTrigger(update.getUseCaseTrigger());

        // Update ManyToMany collections
        oldUseCase.getSecondaryActors().clear();
        oldUseCase.getSecondaryActors().addAll(update.getSecondaryActors());

        // Conditions are links to shared artifacts: reword the unshared ones in place, save the new ones, and
        // replace this use case's ordered lists. Removing an entry drops only the link.
        oldUseCase.replacePreconditions(applyConditions(teamId, preconditions, RequirementArtifactType.PRECONDITION));
        oldUseCase.replacePostconditions(applyConditions(teamId, postconditions, RequirementArtifactType.POSTCONDITION));

        // Revise the main steps in place: submitted ids pair only with this use case's own steps, extensions and
        // extension steps, and anything unpaired becomes a new row (see UseCase.reviseMainSteps).
        oldUseCase.reviseMainSteps(update.getMainSteps());

        return this.useCaseRepository.save(oldUseCase);
    }

    /**
     * Sets {@code useCase}'s actors from {@code references}, each resolved in {@code teamId}, the team named in the
     * route. The team-scoped load is itself the check: an id outside the team, or naming nothing at all, throws the
     * same {@link ObjectNotFoundException}, so a refusal does not reveal which ids exist in other teams
     * (BR-team-scoped-access, OI-46). Only then is the type checked (BR-use-case-actor-types), so the type of
     * another team's artifact cannot leak through a 400 either.
     * <p>
     * The primary actor is checked first, then each secondary actor, and the first failure is reported. Every use
     * case has a primary actor: the DTO requires one, and this refuses a missing one from a caller inside the server
     * too, so no path can store a use case without one.
     */
    private void resolveActors(Integer teamId, UseCaseReferences references, UseCase useCase) {
        if (references.primaryActorId() == null) {
            throw new IllegalArgumentException("Primary actor is required.");
        }
        RequirementArtifact primaryActor = requireActor(teamId, references.primaryActorId(), "Primary actor");

        Set<RequirementArtifact> secondaryActors = new HashSet<>();
        for (Long secondaryActorId : references.secondaryActorIds()) {
            secondaryActors.add(requireActor(teamId, secondaryActorId, "Secondary actor"));
        }

        useCase.setPrimaryActor(primaryActor);
        useCase.replaceSecondaryActors(secondaryActors);
    }

    /**
     * Loads one actor and checks that it may be an actor, in two steps whose order matters:
     * <ol>
     *   <li><strong>Team first.</strong> {@code findRequirementArtifactById} looks the id up within {@code teamId}
     *       only, and throws {@link ObjectNotFoundException} (404) when the artifact belongs to another team or does
     *       not exist. Both cases give the same message, so the caller cannot tell them apart.</li>
     *   <li><strong>Type second.</strong> Only an artifact already known to be this team's is checked against
     *       {@link #ACTOR_TYPES} (BR-use-case-actor-types), and a wrong type is an {@link IllegalArgumentException}
     *       (400, extension 4a on create, 7a on edit). Checking the type first would let a 400 versus 404 reveal the
     *       type of another team's artifact.</li>
     * </ol>
     *
     * @param teamId  the team named in the route, never one from the request body
     * @param actorId the id the request named for this actor
     * @param role    "Primary actor" or "Secondary actor", used only in the 400 message, so that the message says
     *                which field and which id are wrong (the location extensions 4a1 and 7a1 ask for)
     * @return the managed artifact, safe to set as an actor of a use case of {@code teamId}
     */
    private RequirementArtifact requireActor(Integer teamId, Long actorId, String role) {
        // Step 1: scoped to the route's team; throws 404 for another team's id and for an unknown id alike.
        RequirementArtifact actor = this.requirementArtifactService.findRequirementArtifactById(teamId, actorId);
        // Step 2: the artifact is this team's; now check it is a stakeholder or an external system.
        if (!ACTOR_TYPES.contains(actor.getType())) {
            throw new IllegalArgumentException(role + " " + actorId + " must be a stakeholder or an external system.");
        }
        return actor;
    }

    /**
     * For each condition of {@code useCase}, the other use cases that reference it, in use case id order. The use case
     * editor shows these so that a shared condition is read-only (extension 6b1 of UC-DOC-edit-use-case).
     */
    public Map<Long, List<ConditionUse>> otherUseCasesReferencing(UseCase useCase) {
        Map<Long, List<ConditionUse>> uses = new HashMap<>();
        List<Long> preconditionIds = useCase.getPreconditions().stream().map(RequirementArtifact::getId).toList();
        List<Long> postconditionIds = useCase.getPostconditions().stream().map(RequirementArtifact::getId).toList();
        if (!preconditionIds.isEmpty()) {
            this.useCaseRepository.findOtherUseCasesReferencingPreconditions(preconditionIds, useCase.getId())
                    .forEach(use -> uses.computeIfAbsent(use.conditionId(), id -> new ArrayList<>()).add(use));
        }
        if (!postconditionIds.isEmpty()) {
            this.useCaseRepository.findOtherUseCasesReferencingPostconditions(postconditionIds, useCase.getId())
                    .forEach(use -> uses.computeIfAbsent(use.conditionId(), id -> new ArrayList<>()).add(use));
        }
        return uses;
    }

    /** Stands for "no use case" in the sharing queries, which exclude the use case being saved. Ids start at 1. */
    private static final Long NO_USE_CASE = 0L;

    /**
     * One submitted condition, checked and resolved but not yet written: {@code existing} is the team's condition the
     * entry named, or null for a new one, and {@code wording} is what it should say.
     */
    private record PlannedCondition(RequirementArtifact existing, String wording) {
    }

    /**
     * Checks every entry of one condition list and resolves it, writing nothing, so that a refusal leaves the use case
     * and its conditions untouched (BR-use-case-condition-shared, BR-shared-condition-wording). For each entry, in
     * order: a repeated id, then the lookup scoped to {@code teamId} (404 for another team's id and for an unknown id
     * alike), then the type, then the wording. The first failure is reported.
     * <p>
     * A condition may be reworded through a use case only while no use case other than {@code useCaseId} references
     * it, because a shared condition's wording is changed only on the condition itself, for every use case at once.
     *
     * @param useCaseId the use case being saved, or {@link #NO_USE_CASE} on create
     */
    private List<PlannedCondition> planConditions(Integer teamId, List<SubmittedCondition> submitted, RequirementArtifactType type, Long useCaseId) {
        String label = type == RequirementArtifactType.PRECONDITION ? "Precondition" : "Postcondition";
        List<PlannedCondition> plan = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (int i = 0; i < submitted.size(); i++) {
            SubmittedCondition entry = submitted.get(i);
            int position = i + 1; // the display number, PRE-n or POST-n
            if (entry.id() == null) {
                plan.add(new PlannedCondition(null, entry.wording()));
                continue;
            }
            if (!seen.add(entry.id())) {
                throw new IllegalArgumentException(label + " " + position + " repeats a condition already in this list.");
            }
            RequirementArtifact condition = this.requirementArtifactService.findRequirementArtifactById(teamId, entry.id());
            if (condition.getType() != type) {
                throw new IllegalArgumentException(label + " " + position + " is not a " + label.toLowerCase(Locale.ROOT) + ".");
            }
            if (!Objects.equals(condition.getContent(), entry.wording()) && isReferencedElsewhere(condition, type, useCaseId)) {
                String name = condition.getArtifactKey() != null ? condition.getArtifactKey() : label + " " + position;
                throw new IllegalArgumentException(name + " is also used by other use cases; make a separate copy to change its wording here.");
            }
            plan.add(new PlannedCondition(condition, entry.wording()));
        }
        return plan;
    }

    private boolean isReferencedElsewhere(RequirementArtifact condition, RequirementArtifactType type, Long useCaseId) {
        List<Long> ids = List.of(condition.getId());
        List<ConditionUse> uses = type == RequirementArtifactType.PRECONDITION
                ? this.useCaseRepository.findOtherUseCasesReferencingPreconditions(ids, useCaseId)
                : this.useCaseRepository.findOtherUseCasesReferencingPostconditions(ids, useCaseId);
        return !uses.isEmpty();
    }

    /**
     * Writes a checked plan: a new condition is saved through {@link RequirementArtifactService#saveRequirementArtifact},
     * which sets the team from the route and mints its key (PRC-n or PSC-n), with the type taken from the list rather
     * than the request; an existing one is reworded in place when its wording changed. Returns the conditions in
     * order, ready to become the use case's list.
     */
    private List<RequirementArtifact> applyConditions(Integer teamId, List<PlannedCondition> plan, RequirementArtifactType type) {
        List<RequirementArtifact> conditions = new ArrayList<>(plan.size());
        for (PlannedCondition planned : plan) {
            if (planned.existing() == null) {
                RequirementArtifact condition = new RequirementArtifact();
                condition.setType(type);
                condition.setTitle("");
                condition.setContent(planned.wording());
                conditions.add(this.requirementArtifactService.saveRequirementArtifact(teamId, condition));
            } else {
                if (!Objects.equals(planned.existing().getContent(), planned.wording())) {
                    planned.existing().setContent(planned.wording());
                }
                conditions.add(planned.existing());
            }
        }
        return conditions;
    }

}

