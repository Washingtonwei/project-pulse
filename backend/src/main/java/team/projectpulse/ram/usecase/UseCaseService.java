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
import team.projectpulse.team.Team;
import team.projectpulse.user.PeerEvaluationUser;
import team.projectpulse.user.UserRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.HashSet;
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
     * The actors come from {@code actorIds} alone, resolved in {@code teamId}; any actor already set on
     * {@code useCase} is replaced. See {@link #resolveActors}.
     *
     * @throws ObjectNotFoundException  if an actor id names no artifact of this team
     * @throws IllegalArgumentException if an actor is not a stakeholder or an external system
     */
    public UseCase saveUseCase(Integer teamId, UseCase useCase, UseCaseActorIds actorIds) {
        useCase.initLockIfMissing(); // Ensure lock is initialized for new use cases

        // Fresh copies drop any step, extension or extension-step id the request body carried, which would
        // otherwise reach save() on a child entity (OI-46).
        useCase.replaceMainSteps(useCase.getMainSteps().stream().map(UseCaseMainStep::copyOf).toList());

        resolveActors(teamId, actorIds, useCase);
        // Sets the artifact's team from the route and mints its key, so the team read below is the route's.
        this.requirementArtifactService.saveRequirementArtifact(teamId, useCase.getArtifact());
        giveConditionsTheTeam(useCase, useCase.getArtifact().getTeam());
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
     * must hold the use case lock. The actors come from {@code actorIds} alone; see {@link #resolveActors}.
     *
     * @throws ObjectNotFoundException  if the use case, or an actor id, names nothing in this team
     * @throws IllegalArgumentException if an actor is not a stakeholder or an external system, or no version is given
     * @throws OptimisticLockException  if {@code expectedVersion} is stale
     */
    public UseCase updateUseCase(Integer teamId, Long useCaseId, UseCase update, UseCaseActorIds actorIds, Integer expectedVersion) {
        UseCase oldUseCase = this.useCaseRepository.findByIdAndArtifactTeamTeamId(useCaseId, teamId)
                .orElseThrow(() -> new ObjectNotFoundException("use case", useCaseId));
        // Load the whole step tree in one query, rather than one query per step and per extension once the steps
        // are revised below. The team-scoped query above has just authorized this id in this transaction, which is
        // the condition findByIdWithStepGraph requires, and nothing is modified yet, so the query flushes nothing.
        this.useCaseRepository.findByIdWithStepGraph(useCaseId);
        oldUseCase.initLockIfMissing();

        // Resolve the incoming references before doing any version or lock work.
        resolveActors(teamId, actorIds, update);

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

        giveConditionsTheTeam(update, oldUseCase.getArtifact().getTeam());
        oldUseCase.getPreconditions().clear();
        oldUseCase.getPreconditions().addAll(update.getPreconditions());

        oldUseCase.getPostconditions().clear();
        oldUseCase.getPostconditions().addAll(update.getPostconditions());

        // Revise the main steps in place: submitted ids pair only with this use case's own steps, extensions and
        // extension steps, and anything unpaired becomes a new row (see UseCase.reviseMainSteps).
        oldUseCase.reviseMainSteps(update.getMainSteps());

        return this.useCaseRepository.save(oldUseCase);
    }

    /**
     * Sets {@code useCase}'s actors from {@code actorIds}, each resolved in {@code teamId}, the team named in the
     * route. The team-scoped load is itself the check: an id outside the team, or naming nothing at all, throws the
     * same {@link ObjectNotFoundException}, so a refusal does not reveal which ids exist in other teams
     * (BR-team-scoped-access, OI-46). Only then is the type checked (BR-use-case-actor-types), so the type of
     * another team's artifact cannot leak through a 400 either.
     * <p>
     * The primary actor is checked first, then each secondary actor, and the first failure is reported.
     */
    private void resolveActors(Integer teamId, UseCaseActorIds actorIds, UseCase useCase) {
        RequirementArtifact primaryActor = actorIds.primaryActorId() == null ? null
                : requireActor(teamId, actorIds.primaryActorId(), "Primary actor");

        Set<RequirementArtifact> secondaryActors = new HashSet<>();
        for (Long secondaryActorId : actorIds.secondaryActorIds()) {
            secondaryActors.add(requireActor(teamId, secondaryActorId, "Secondary actor"));
        }

        useCase.setPrimaryActor(primaryActor);
        useCase.replaceSecondaryActors(secondaryActors);
    }

    private RequirementArtifact requireActor(Integer teamId, Long actorId, String role) {
        RequirementArtifact actor = this.requirementArtifactService.findRequirementArtifactById(teamId, actorId);
        if (!ACTOR_TYPES.contains(actor.getType())) {
            throw new IllegalArgumentException(role + " " + actorId + " must be a stakeholder or an external system.");
        }
        return actor;
    }

    /**
     * Gives every precondition and postcondition of {@code useCase} the team named in the route. The converter sets
     * none, since the body's {@code teamId} is never trusted (OI-46).
     * <p>
     * A stopgap at this layer: making conditions shared references of the team (BR-use-case-condition-shared) is the
     * next design slice, and it replaces this method.
     */
    private static void giveConditionsTheTeam(UseCase useCase, Team team) {
        useCase.getPreconditions().forEach(condition -> condition.setTeam(team));
        useCase.getPostconditions().forEach(condition -> condition.setTeam(team));
    }

}

