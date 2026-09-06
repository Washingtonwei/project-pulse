package team.projectpulse.ram.usecase;

import jakarta.persistence.OptimisticLockException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.requirement.RequirementArtifactService;
import team.projectpulse.system.UserUtils;
import team.projectpulse.system.exception.ObjectNotFoundException;
import team.projectpulse.system.exception.SectionAlreadyLockedException;
import team.projectpulse.system.exception.SectionLockRequiredException;
import team.projectpulse.system.exception.SectionUnlockNotAllowedException;
import team.projectpulse.user.PeerEvaluationUser;
import team.projectpulse.user.UserRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@Service
@Transactional
public class UseCaseService {

    private final Duration defaultLockTtl;
    private final UseCaseRepository useCaseRepository;
    private final RequirementArtifactService requirementArtifactService;
    private final UserUtils userUtils;
    private final UserRepository userRepository;


    public UseCaseService(UseCaseRepository useCaseRepository, RequirementArtifactService requirementArtifactService, UserUtils userUtils, UserRepository userRepository,
                          @Value("${ram.lock.default-lock-ttl:PT15M}") Duration defaultLockTtl) {
        this.useCaseRepository = useCaseRepository;
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

    public UseCase saveUseCase(Integer teamId, UseCase useCase) {
        useCase.initLockIfMissing(); // Ensure lock is initialized for new use cases
        requireActorsInTeam(teamId, useCase);
        this.requirementArtifactService.saveRequirementArtifact(teamId, useCase.getArtifact());
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

    public UseCase updateUseCase(Integer teamId, Long useCaseId, UseCase update, Integer expectedVersion) {
        UseCase oldUseCase = this.useCaseRepository.findByIdAndArtifactTeamTeamId(useCaseId, teamId)
                .orElseThrow(() -> new ObjectNotFoundException("use case", useCaseId));
        oldUseCase.initLockIfMissing();

        // Validate the incoming references before doing any version or lock work.
        requireActorsInTeam(teamId, update);

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

        oldUseCase.getPreconditions().clear();
        oldUseCase.getPreconditions().addAll(update.getPreconditions());

        oldUseCase.getPostconditions().clear();
        oldUseCase.getPostconditions().addAll(update.getPostconditions());

        // Update main steps
        oldUseCase.replaceMainSteps(update.getMainSteps());

        return this.useCaseRepository.save(oldUseCase);
    }

    /**
     * Re-resolves the use case's actors against {@code teamId}, so a caller cannot point their own
     * use case at another team's artifacts and surface that team's content inside it.
     * <p>
     * This has to happen here rather than at the point of load. The actor ids arrive in the request
     * body, and {@code UseCaseDtoToUseCaseConverter} has already turned them into entities before the
     * service is called, with no team context of any kind. By the time this method runs there is no
     * id left to scope a query by, only an object that was loaded unscoped.
     * <p>
     * <strong>The returned artifact is discarded on purpose: these calls are made for their exception,
     * not for their value.</strong> {@code findRequirementArtifactById} is team-scoped and throws
     * {@link ObjectNotFoundException} when the id does not belong to {@code teamId}, which is the
     * entire reason to call it. Reusing that finder keeps exactly one definition of "does this
     * artifact belong to this team" instead of a second copy here, at the cost of a statement that
     * looks discardable to a reader who does not know that. An {@code existsByIdAndTeamTeamId}
     * returning a boolean would read more plainly, and would be the thing to change to.
     * <p>
     * Covers the primary and secondary actors only. Preconditions and postconditions are rebuilt from
     * the body by {@code ConditionDtoToRequirementArtifactConverter}, which copies {@code id} straight
     * from the payload; that is a separate defect class, tracked as OI-46, and is not handled here.
     */
    private void requireActorsInTeam(Integer teamId, UseCase useCase) {
        RequirementArtifact primaryActor = useCase.getPrimaryActor();
        if (primaryActor != null && primaryActor.getId() != null) {
            this.requirementArtifactService.findRequirementArtifactById(teamId, primaryActor.getId());
        }

        Set<RequirementArtifact> secondaryActors = useCase.getSecondaryActors();
        if (secondaryActors != null) {
            for (RequirementArtifact secondaryActor : secondaryActors) {
                if (secondaryActor != null && secondaryActor.getId() != null) {
                    this.requirementArtifactService.findRequirementArtifactById(teamId, secondaryActor.getId());
                }
            }
        }
    }

    private <T> void updateCollection(Set<T> oldCollection, Set<T> newCollection) {
        oldCollection.clear();
        if (newCollection != null) {
            oldCollection.addAll(newCollection);
        }
    }

}

