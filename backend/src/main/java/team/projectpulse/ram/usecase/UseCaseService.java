package team.projectpulse.ram.usecase;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.OptimisticLockException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.projectpulse.ram.requirement.ArtifactLinkService;
import team.projectpulse.ram.requirement.ArtifactLinkType;
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
import java.util.Set;

/**
 * Creating, reading, editing and locking use cases (UC-DOC-create-use-case, UC-DOC-edit-use-case). The design of
 * record is {@code docs/design/doc.md}.
 * <p>
 * Every reference a request names (the actors, and each existing precondition and postcondition) arrives as a plain
 * id in {@link UseCaseReferences} and is resolved here, through a lookup scoped to the team in the route, never by the
 * DTO converter (OI-46, BR-team-scoped-access); a new condition arrives as its wording alone. Every check runs before
 * any write, so a refused request changes nothing.
 */
@Service
@Transactional
public class UseCaseService {

    /** The artifact types that may serve as a use case's actor (BR-use-case-actor-types). */
    private static final Set<RequirementArtifactType> ACTOR_TYPES = EnumSet.of(RequirementArtifactType.STAKEHOLDER, RequirementArtifactType.EXTERNAL_SYSTEM);

    private final Duration defaultLockTtl;
    private final UseCaseRepository useCaseRepository;
    private final RequirementArtifactService requirementArtifactService;
    private final ArtifactLinkService artifactLinkService;
    private final UserUtils userUtils;
    private final UserRepository userRepository;
    private final EntityManager entityManager;


    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "The EntityManager is Spring's shared, transaction-scoped proxy; holding a reference to it is how it is meant to be used.")
    public UseCaseService(UseCaseRepository useCaseRepository, RequirementArtifactService requirementArtifactService, ArtifactLinkService artifactLinkService, UserUtils userUtils, UserRepository userRepository,
                          EntityManager entityManager, @Value("${ram.lock.default-lock-ttl:PT15M}") Duration defaultLockTtl) {
        this.useCaseRepository = useCaseRepository;
        this.entityManager = entityManager;
        this.requirementArtifactService = requirementArtifactService;
        this.artifactLinkService = artifactLinkService;
        this.userUtils = userUtils;
        this.userRepository = userRepository;
        this.defaultLockTtl = defaultLockTtl;
    }

    /**
     * Finds a use case by ID with all related entities fully loaded.
     * Uses the 3-query approach to avoid big Cartesian products:
     * 1. Load use case with scalar relationships (artifact, lock and its holder)
     * 2. Load the many-valued associations (the artifact's outgoing links with their targets, which carry the actors,
     *    and the ordered preconditions and postconditions)
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
        // This populates the artifact's outgoing links (the actors), its incoming links with their sources (the
        // business rules, CONSTRAINS links), preconditions, postconditions
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
     * The actors, the business rules and the conditions come from {@code references} alone, resolved in
     * {@code teamId}; any actor, business rule or condition already set on {@code useCase} is replaced. A new condition is saved as a team artifact with its key
     * (PRC-n or PSC-n); an existing one is linked as it is. See {@link #resolveActors}, {@link #resolveBusinessRules} and
     * {@link #resolveConditions}.
     *
     * @throws ObjectNotFoundException  if an actor, business rule or condition id names no artifact of this team
     * @throws IllegalArgumentException if the primary actor is missing, an actor is not a stakeholder or an external
     *                                  system, the primary actor is also a secondary actor, a business rule id is not a business rule, or a condition
     *                                  entry is refused (a repeated id, or the other list's type); each is an input
     *                                  validation refusal, extension 4a
     */
    public UseCase saveUseCase(Integer teamId, UseCase useCase, UseCaseReferences references) {
        useCase.initLockIfMissing(); // Ensure lock is initialized for new use cases

        // Fresh copies drop any step, extension or extension-step id the request body carried, which would
        // otherwise reach save() on a child entity (OI-46).
        useCase.replaceMainSteps(useCase.getMainSteps().stream().map(UseCaseMainStep::copyOf).toList());

        // Check every reference first, writing nothing, as in updateUseCase; the writes follow.
        ResolvedActors actors = resolveActors(teamId, references);
        Set<RequirementArtifact> businessRules = resolveBusinessRules(teamId, references);
        List<RequirementArtifact> preconditions = resolveConditions(teamId, references.preconditions(), RequirementArtifactType.PRECONDITION);
        List<RequirementArtifact> postconditions = resolveConditions(teamId, references.postconditions(), RequirementArtifactType.POSTCONDITION);

        // Every check has passed; the writes start here.
        // The use case's own artifact (title, description, priority, notes, type USE_CASE from the converter):
        // createRequirementArtifact sets its team from the route and mints its key (UC-n). The use case shares its id
        // (@MapsId). Saved before the new conditions, so the use case takes the lower id, as it did when the
        // conditions were cascaded.
        this.requirementArtifactService.createRequirementArtifact(teamId, useCase.getArtifact());
        // The actor links start from the artifact, so they follow it. They link the team-checked artifacts
        // resolveActors returned, never what the request sent.
        linkActors(teamId, useCase, actors);
        linkBusinessRules(teamId, useCase, businessRules);
        useCase.replacePreconditions(saveNewConditions(teamId, preconditions));
        useCase.replacePostconditions(saveNewConditions(teamId, postconditions));
        return this.useCaseRepository.save(useCase);
    }

    /**
     * The lock of use case {@code useCaseId} of {@code teamId}, created on first use. An expired lock is cleared here,
     * so expiry needs no timer: it takes effect the next time anyone reads the lock (BR-lock-expiry).
     *
     * @throws ObjectNotFoundException if the use case is not found, or belongs to another team
     */
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

    /**
     * Gives the caller the use case lock until now plus the TTL ({@code ram.lock.default-lock-ttl}), or extends it to
     * that time if the caller already holds it (BR-edit-lock-required). The holder can extend it again and again; see
     * OI-68.
     *
     * @throws SectionAlreadyLockedException (423) if someone else holds an unexpired lock; the message names them
     *                                       and the expiry
     * @throws ObjectNotFoundException       if the use case is not found, or belongs to another team
     */
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

    /**
     * Releases the use case lock. An instructor may release anyone's lock, for example one a student left behind; a
     * student only their own.
     *
     * @throws SectionUnlockNotAllowedException (403) if the caller is a student who does not hold the lock, including
     *                                          when no one holds it
     * @throws ObjectNotFoundException          if the use case is not found, or belongs to another team
     */
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
     * must hold the use case lock. The actors, the business rules and the conditions come from {@code references}
     * alone; see {@link #resolveActors}, {@link #resolveBusinessRules} and {@link #resolveConditions}. The checks run in
     * this order, and nothing is written until all of them pass: use case in team, actors, business rules, conditions,
     * version, lock.
     *
     * @throws ObjectNotFoundException       if the use case, an actor id, a business rule id or a condition id names
     *                                       nothing in this team
     * @throws IllegalArgumentException      if no version is given, the primary actor is missing, an actor is not a
     *                                       stakeholder or an external system, the primary actor is also a
     *                                       secondary actor, a business rule id is not a business rule, or a
     *                                       condition entry is refused (a
     *                                       repeated id, or the other list's type); each is an input validation
     *                                       refusal, extension 7a
     * @throws OptimisticLockException       (409) if {@code expectedVersion} is stale
     * @throws SectionLockRequiredException  (409) if no one holds the lock
     * @throws SectionAlreadyLockedException (423) if someone else holds it
     */
    public UseCase updateUseCase(Integer teamId, Long useCaseId, UseCase update, UseCaseReferences references, Integer expectedVersion) {
        UseCase oldUseCase = this.useCaseRepository.findByIdAndArtifactTeamTeamId(useCaseId, teamId)
                .orElseThrow(() -> new ObjectNotFoundException("use case", useCaseId));
        // Load the whole step tree in one query, rather than one query per step and per extension once the steps
        // are revised below. The team-scoped query above has just authorized this id in this transaction, which is
        // the condition findByIdWithStepGraph requires, and nothing is modified yet, so the query flushes nothing.
        this.useCaseRepository.findByIdWithStepGraph(useCaseId);
        // A use case that has never been locked has no lock row yet, and this creates one. Nobody holds it, so the lock
        // check below refuses the save, and the refusal rolls the new row back with everything else.
        oldUseCase.initLockIfMissing();

        // Resolve and check the incoming references before doing any version or lock work, and before any write. Both
        // resolve methods only read and return what they found, so a refused request leaves the database untouched;
        // the results are written onto oldUseCase after the version and lock checks, further down.
        ResolvedActors actors = resolveActors(teamId, references);
        Set<RequirementArtifact> businessRules = resolveBusinessRules(teamId, references);
        List<RequirementArtifact> preconditions = resolveConditions(teamId, references.preconditions(), RequirementArtifactType.PRECONDITION);
        List<RequirementArtifact> postconditions = resolveConditions(teamId, references.postconditions(), RequirementArtifactType.POSTCONDITION);

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

        // The plain fields, copied from the detached update: title, description (the artifact's content), priority and
        // notes onto the stored artifact, and the trigger and the template's four text fields onto the use case. A text
        // field the request left out is null here and clears the stored value, as priority and notes do. JPA auditing stamps the artifact's
        // updatedBy and updatedAt when any of them changed.
        oldUseCase.getArtifact().setTitle(update.getArtifact().getTitle());
        oldUseCase.getArtifact().setContent(update.getArtifact().getContent());
        oldUseCase.getArtifact().setPriority(update.getArtifact().getPriority());
        oldUseCase.getArtifact().setNotes(update.getArtifact().getNotes());
        oldUseCase.setUseCaseTrigger(update.getUseCaseTrigger());
        oldUseCase.setFrequencyOfUse(update.getFrequencyOfUse());
        oldUseCase.setAssociatedInformation(update.getAssociatedInformation());
        oldUseCase.setAssumptions(update.getAssumptions());
        oldUseCase.setOpenIssues(update.getOpenIssues());

        // The actors are the team-checked artifacts resolveActors returned, never what the request sent. Only the links
        // that changed are written: a kept actor's link keeps its id and created-by.
        linkActors(teamId, oldUseCase, actors);

        // The business rules likewise: CONSTRAINS links from each team-checked rule, revised, never recreated.
        linkBusinessRules(teamId, oldUseCase, businessRules);

        // Conditions are links to shared artifacts: save the new ones, then replace this use case's ordered lists.
        // Removing an entry drops only the link.
        oldUseCase.replacePreconditions(saveNewConditions(teamId, preconditions));
        oldUseCase.replacePostconditions(saveNewConditions(teamId, postconditions));

        // Revise the main steps in place: submitted ids pair only with this use case's own steps, extensions and
        // extension steps, and anything unpaired becomes a new row (see UseCase.reviseMainSteps).
        oldUseCase.reviseMainSteps(update.getMainSteps());

        return this.useCaseRepository.save(oldUseCase);
    }

    /** A use case's actors as {@link #resolveActors} found them: team-checked artifacts, not yet set on any use case. */
    private record ResolvedActors(RequirementArtifact primary, Set<RequirementArtifact> secondaries) {
    }

    /**
     * Resolves the actors {@code references} names, each in {@code teamId}, the team named in the route, and returns
     * them without setting them on any use case, so it writes nothing. The team-scoped load is itself the check: an
     * id outside the team, or naming nothing at all, throws the same {@link ObjectNotFoundException}, so a refusal does
     * not reveal which ids exist in other teams (BR-team-scoped-access, OI-46). Only then is the type checked
     * (BR-use-case-actor-types), so the type of another team's artifact cannot leak through a 400 either.
     * <p>
     * The primary actor is checked first, then each secondary actor, and the first failure is reported. Every use
     * case has a primary actor: the DTO requires one, and this refuses a missing one from a caller inside the server
     * too, so no path can store a use case without one. Last, once every id is known to be this team's actor, the
     * primary actor may not also be a secondary actor (BR-use-case-actor-types; 4a on create, 7a on edit).
     */
    private ResolvedActors resolveActors(Integer teamId, UseCaseReferences references) {
        if (references.primaryActorId() == null) {
            throw new IllegalArgumentException("Primary actor is required.");
        }
        RequirementArtifact primaryActor = requireActor(teamId, references.primaryActorId(), "Primary actor");

        Set<RequirementArtifact> secondaryActors = new HashSet<>();
        for (Long secondaryActorId : references.secondaryActorIds()) {
            secondaryActors.add(requireActor(teamId, secondaryActorId, "Secondary actor"));
        }
        if (references.secondaryActorIds().contains(references.primaryActorId())) {
            throw new IllegalArgumentException("Actor " + references.primaryActorId() + " cannot be both the primary actor and a secondary actor.");
        }

        return new ResolvedActors(primaryActor, secondaryActors);
    }

    /**
     * Records {@code actors} as {@code useCase}'s actor links, one {@code PRIMARY_ACTOR} and one {@code SECONDARY_ACTOR}
     * per secondary actor, from the use case's own artifact (BR-use-case-actor-types, BR-link-constraints). Each call
     * to {@code reviseOutgoingLinks} keeps the links that still hold, deletes the ones that no longer do, and adds the
     * missing ones. {@code actors} must come from {@link #resolveActors}, which checked every one against
     * {@code teamId}.
     */
    private void linkActors(Integer teamId, UseCase useCase, ResolvedActors actors) {
        RequirementArtifact source = useCase.getArtifact();
        this.artifactLinkService.reviseOutgoingLinks(teamId, source, ArtifactLinkType.PRIMARY_ACTOR, Set.of(actors.primary()));
        this.artifactLinkService.reviseOutgoingLinks(teamId, source, ArtifactLinkType.SECONDARY_ACTOR, actors.secondaries());
    }

    /**
     * Resolves the business rules {@code references} names, each in {@code teamId}, and returns them without linking
     * them, so it writes nothing. As for actors, the team-scoped load is itself the check (404 for another team's id and
     * for an unknown id alike), and only then is the type checked, so a 400 never reveals the type of another team's
     * artifact (BR-use-case-business-rules; 4a on create, 7a on edit). The ids are checked in ascending order, since the
     * request's set has none of its own, so the first failure reported is the same from run to run.
     */
    private Set<RequirementArtifact> resolveBusinessRules(Integer teamId, UseCaseReferences references) {
        Set<RequirementArtifact> businessRules = new HashSet<>();
        for (Long businessRuleId : references.businessRuleIds().stream().sorted().toList()) {
            RequirementArtifact businessRule = this.requirementArtifactService.findRequirementArtifactById(teamId, businessRuleId);
            if (businessRule.getType() != RequirementArtifactType.BUSINESS_RULE) {
                throw new IllegalArgumentException("Business rule " + businessRuleId + " must be a business rule.");
            }
            businessRules.add(businessRule);
        }
        return businessRules;
    }

    /**
     * Records {@code businessRules} as {@code useCase}'s business rules: one {@code CONSTRAINS} link from each rule to
     * the use case's own artifact (BR-use-case-business-rules, BR-link-constraints). {@code reviseIncomingLinks} keeps
     * the links that still hold, deletes the ones that no longer do, including a stray {@code CONSTRAINS} link from a
     * non-rule that the link routes let through (OI-27), and adds the missing ones. {@code businessRules} must come
     * from {@link #resolveBusinessRules}.
     */
    private void linkBusinessRules(Integer teamId, UseCase useCase, Set<RequirementArtifact> businessRules) {
        this.artifactLinkService.reviseIncomingLinks(teamId, useCase.getArtifact(), ArtifactLinkType.CONSTRAINS, businessRules);
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
     * @return the managed artifact, safe to link as an actor of a use case of {@code teamId}
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
     * For each condition of {@code useCase}, the other use cases that reference it, in use case id order, keyed by
     * condition id. A condition no other use case references has no entry. The controller calls this for every GET,
     * POST and PUT response, and the editor lists the result as "Also used by" (extension 6b1 of
     * UC-DOC-edit-use-case); no save depends on it, since a stored condition is read-only whether or not it is shared.
     * Two queries in all, one per list, however many conditions there are; see {@link ConditionUse} and
     * {@link UseCaseRepository#findOtherUseCasesReferencingPreconditions}.
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

    /**
     * Checks every entry of one condition list and resolves it, writing nothing, so that a refusal leaves the use case
     * and its conditions untouched (BR-use-case-condition-shared, BR-shared-condition-wording). An entry with an id
     * links that condition as it is; an entry without one becomes a new condition, built here but saved only by
     * {@link #saveNewConditions}, after the version and lock checks. For each entry with an id, in order: a repeated
     * id, then the lookup scoped to {@code teamId} (404 for another team's id and for an unknown id alike), then the
     * type. The first failure is reported.
     * <p>
     * A use case never rewords a condition, shared or not, so the wording sent with an id is ignored and the stored
     * wording stands. Rewording for one use case is a separate copy (extension 6b3): an entry with no id.
     *
     * @return the conditions in list order: the team's stored ones, and new unsaved ones (no id yet)
     */
    private List<RequirementArtifact> resolveConditions(Integer teamId, List<SubmittedCondition> submitted, RequirementArtifactType type) {
        // "Precondition" or "Postcondition", used only in error messages so they name the list.
        String label = type == RequirementArtifactType.PRECONDITION ? "Precondition" : "Postcondition";
        // One condition per submitted entry, in the same order: this order becomes the use case's order.
        List<RequirementArtifact> conditions = new ArrayList<>(submitted.size());
        // The ids already met in this list, to refuse a condition listed twice.
        Set<Long> seen = new HashSet<>();

        for (int i = 0; i < submitted.size(); i++) {
            SubmittedCondition entry = submitted.get(i);
            // The 1-based position is the display number the student sees (PRE-2, POST-1), so errors use it.
            int position = i + 1;

            // (a) No id: the student typed a new condition, or asked for a separate copy (extension 6b3). The type
            // comes from the list it was entered in, never from the request, and the wording is the artifact's
            // content, stored exactly as sent; a condition has no title of its own.
            if (entry.id() == null) {
                RequirementArtifact condition = new RequirementArtifact();
                condition.setType(type);
                condition.setTitle("");
                condition.setContent(entry.wording());
                conditions.add(condition);
                continue;
            }

            // (b) The same condition twice in one list is a mistake, refused rather than silently merged.
            if (!seen.add(entry.id())) {
                throw new IllegalArgumentException(label + " " + position + " repeats a condition already in this list.");
            }

            // (c) Look the id up within the route's team only. Another team's id and an id that names nothing both
            // throw the same 404, so the refusal reveals nothing about other teams (BR-team-scoped-access).
            RequirementArtifact condition = this.requirementArtifactService.findRequirementArtifactById(teamId, entry.id());

            // (d) A precondition's id may not appear in the postcondition list, or the other way round. Checked only
            // after (c), so the type of another team's artifact can't leak through a 400.
            if (condition.getType() != type) {
                throw new IllegalArgumentException(label + " " + position + " is not a " + label.toLowerCase(Locale.ROOT) + ".");
            }

            // All checks passed: link this existing condition as it is. The wording sent with it is ignored, so its
            // stored wording stands: a use case never rewords a condition, shared or not (BR-shared-condition-wording).
            conditions.add(condition);
        }
        return conditions;
    }

    /**
     * Saves the new conditions of a list that {@link #resolveConditions} checked, through
     * {@link RequirementArtifactService#createRequirementArtifact}, which sets the route's team and mints the key (PRC-n
     * or PSC-n), as it does for every artifact. Existing conditions pass through unchanged. Returns the list in the
     * same order, ready to become the use case's list; a condition left out of it loses only its link to this use case.
     */
    private List<RequirementArtifact> saveNewConditions(Integer teamId, List<RequirementArtifact> conditions) {
        return conditions.stream()
                .map(condition -> condition.getId() == null ? this.requirementArtifactService.createRequirementArtifact(teamId, condition) : condition)
                .toList();
    }

}

