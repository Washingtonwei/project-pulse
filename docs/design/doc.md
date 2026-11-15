# Requirement Documents (DOC) Design

> Realizes: UC-DOC-create-use-case, UC-DOC-edit-use-case
> Depends on: BR-use-case-actor-types, BR-use-case-condition-shared, BR-shared-condition-wording, BR-team-scoped-access, BR-edit-lock-required, BR-lock-expiry, OI-46 (request-body ids), OI-51 (optimistic locking)
> See: [use cases](../requirements/use-cases.md#requirement-documents), [architectural-design.md](architectural-design.md#domain--aggregate-model) (Domain & aggregate model, Transactions & consistency, Authorization)
> History: the step tree was written up after the code was built (2026-11-13). The rest of create and edit, including the actor rules and the shared conditions, was designed through `/design` on 2026-11-14; where this doc describes code that already existed, it records the design as built.

## Scope

This doc is the DOC area's design-of-record. It covers creating and editing a use case: the stored aggregate, its actors, its shared and ordered preconditions and postconditions, its ordered step tree, the use case lock, the version check, and the API the use case editor calls. Rewording a shared condition for every use case ("Edit for all", extension 6b2) is not designed yet; see Open questions. The section-based documents, document-section locking and the Use Cases document were built before the design gate existed and are not written up here yet. Add them as sections of this same doc when they are next designed, rather than starting a second file.

This doc is also the **API contract of record** for these routes. The older SwaggerHub definition is not kept in step with it.

## Overview

A use case is an aggregate, and `UseCase` is its root. It holds:

- **Its artifact.** A `RequirementArtifact` of type `USE_CASE`, shared by id through `@OneToOne @MapsId`, that carries the title, description, priority, notes and artifact key (`UC-n`). This is how a use case takes part in artifact links like any other requirement artifact.
- **The trigger**, a plain field.
- **Actors.** A primary actor (`@ManyToOne`) and secondary actors (`@ManyToMany`), both references to team artifacts of type `STAKEHOLDER` or `EXTERNAL_SYSTEM` (BR-use-case-actor-types). The use case never owns or cascades to an actor.
- **Preconditions and postconditions.** Ordered references to team artifacts of type `PRECONDITION` and `POSTCONDITION`: a `@ManyToMany` `List` each, with an `@OrderColumn` on the join table and no cascade. A condition is shared: any number of the team's use cases may reference it, each at its own position (BR-use-case-condition-shared). See [Preconditions and postconditions](#preconditions-and-postconditions).
- **The step tree.** Three levels of ordered children (main steps, their extensions, and the extensions' steps), each an inverse `@OneToMany` list with `@OrderColumn`, `cascade = ALL` and `orphanRemoval = true`.
- **The lock.** A `UseCaseLock` that says who may save the use case and until when.
- **A `@Version`**, which the editor sends back on every save.

The editor always submits the whole use case and expects the stored use case to match the submission exactly. The server takes the team only from the route and resolves every reference against it (OI-46). The DTO converter maps fields and resolves nothing.

The save does not replace the stored step tree with the submitted one. It **revises** it: every submitted child is paired, by id, with an existing child of the same parent; a paired child is updated in place, and anything unpaired is stored as a new row. One small generic class does this for all three levels, so the rule is written once.

## Components & classes

```mermaid
classDiagram
    direction LR
    class UseCaseController {
        +findUseCaseById(teamId, useCaseId)
        +addUseCase(teamId, dto)
        +updateUseCase(teamId, useCaseId, dto)
        +getUseCaseLockStatus / lockUseCase / unlockUseCase
    }
    class UseCaseReferences {
        <<record>>
        primaryActorId
        secondaryActorIds
        preconditions : List~SubmittedCondition~
        postconditions : List~SubmittedCondition~
    }
    class SubmittedCondition {
        <<record>>
        id
        wording
    }
    class UseCaseDtoToUseCaseConverter {
        <<changed: fields only>>
        +convert(dto)
    }
    class UseCaseService {
        <<changed>>
        +saveUseCase(teamId, useCase, references)
        +updateUseCase(teamId, useCaseId, update, references, expectedVersion)
        +otherUseCasesReferencing(useCase)
        +findUseCaseLock / lockUseCase / unlockUseCase
        -resolveActors(teamId, references, useCase)
        -requireActor(teamId, actorId, role)
        -resolveConditions(teamId, useCase, references)
    }
    class UseCaseRepository {
        +findOtherUseCasesReferencingPreconditions(ids, useCaseId)
        +findOtherUseCasesReferencingPostconditions(ids, useCaseId)
    }
    class RequirementArtifactService {
        +findRequirementArtifactById(teamId, id)
        +saveRequirementArtifact(teamId, artifact)
    }
    class UseCase {
        +reviseMainSteps(submitted)
        +replaceMainSteps(steps)
        +replaceSecondaryActors(actors)
    }
    class RequirementArtifact
    class UseCaseLock {
        +lock / extend / unlock
        +isLocked(now) / isExpired(now)
    }
    class UseCaseMainStep {
        +reviseExtensions(submitted)
        ~reviseFrom(submitted)
        +copyOf(step)$
    }
    class UseCaseExtension {
        +reviseSteps(submitted)
        ~reviseFrom(submitted)
        +copyOf(extension)$
    }
    class UseCaseExtensionStep {
        ~reviseFrom(submitted)
        +copyOf(step)$
    }
    class OwnedChildren {
        <<package-private>>
        +revise(own, submitted, idOf, updateInPlace, freshCopy, attach)$
    }
    UseCaseController --> UseCaseDtoToUseCaseConverter
    UseCaseController --> UseCaseReferences : from the DTO
    UseCaseReferences --> SubmittedCondition
    UseCaseController --> UseCaseService
    UseCaseService --> RequirementArtifactService : team-scoped lookups, key minting
    UseCaseService --> UseCase
    UseCaseService --> UseCaseRepository : which other use cases share a condition
    UseCase "1" *-- "1" RequirementArtifact : artifact (MapsId)
    UseCase "*" --> "1" RequirementArtifact : primaryActor
    UseCase "*" --> "*" RequirementArtifact : secondaryActors
    UseCase "*" --> "*" RequirementArtifact : preconditions, postconditions (ordered List)
    UseCase "1" *-- "0..1" UseCaseLock : lock
    UseCase "1" *-- "*" UseCaseMainStep : mainSteps
    UseCaseMainStep "1" *-- "*" UseCaseExtension : extensions
    UseCaseExtension "1" *-- "*" UseCaseExtensionStep : steps
    UseCase ..> OwnedChildren
    UseCaseMainStep ..> OwnedChildren
    UseCaseExtension ..> OwnedChildren
```

- [`UseCaseController`](../../backend/src/main/java/team/projectpulse/ram/usecase/UseCaseController.java): builds a `UseCaseReferences` from the DTO and passes it to the service with the converted use case. It puts each use case's other-use-case lists (`otherUseCasesReferencing`) into the response. It holds no logic.
- `UseCaseReferences` and `SubmittedCondition` (`ram/usecase/`): carry every reference the request names (the actor ids, and each condition as an optional id plus its wording, in order) from the request to the service, so the converter never produces a reference at all. `UseCaseReferences` replaces the `UseCaseActorIds` of the actors slice.
- [`UseCaseDtoToUseCaseConverter`](../../backend/src/main/java/team/projectpulse/ram/usecase/converter/UseCaseDtoToUseCaseConverter.java): field mapping only. It injects no repository, never reads the body's `teamId`, and maps no condition. `ConditionDtoToRequirementArtifactConverter` is deleted.
- [`UseCaseService`](../../backend/src/main/java/team/projectpulse/ram/usecase/UseCaseService.java): resolves the actors (`resolveActors` and `requireActor`, where the team-scoped load is itself the check) and the conditions (`resolveConditions`); runs the version and lock checks; calls the domain methods. It holds no step logic.
- [`UseCaseRepository`](../../backend/src/main/java/team/projectpulse/ram/usecase/UseCaseRepository.java): two JPQL queries that return, for a set of condition ids, every other use case that references each one (condition id, use case id, artifact key, title), excluding the use case being saved.
- [`OwnedChildren`](../../backend/src/main/java/team/projectpulse/ram/usecase/OwnedChildren.java): the pairing algorithm. Its comments walk one worked example ([A, B, C] becomes [C, N, A]) through every phase; read them before changing it.
- `reviseMainSteps`, `reviseExtensions`, `reviseSteps`: each passes its own list and its own level's `reviseFrom`, `copyOf` and back-reference setter to `OwnedChildren`.
- `reviseFrom`: copies a submitted child's fields onto the paired managed child, then revises that child's own list. Package-private, because only the level above calls it.
- `copyOf`: a deep copy without any id, at every level. Used for unpaired children and for every child on create.
- [`UseCaseLock`](../../backend/src/main/java/team/projectpulse/ram/usecase/UseCaseLock.java): its own entity and its own `@Version`, created on first use by `initLockIfMissing`.
- [`RequirementArtifactType`](../../backend/src/main/java/team/projectpulse/ram/requirement/RequirementArtifactType.java) gains `EXTERNAL_SYSTEM`, and [`ArtifactKeyPrefix`](../../backend/src/main/java/team/projectpulse/ram/requirement/ArtifactKeyPrefix.java) maps it to `SYS`, `PRECONDITION` to `PRC` and `POSTCONDITION` to `PSC` (they were `PRE` and `POST`).
- Frontend: [`RamUseCases.vue`](../../frontend/src/pages/ram/RamUseCases.vue) offers only stakeholders and external systems in the actor pickers, and [`types.ts`](../../frontend/src/apis/ram/types.ts) lists `EXTERNAL_SYSTEM`, so a student can create one in any LIST document section, as stakeholders are created today. Both condition lists are one component, [`UseCaseConditionList.vue`](../../frontend/src/components/UseCaseConditionList.vue), and the page sends only each condition's `id` and wording. Each condition row has three parts:
  - A select-or-type picker over the team's conditions of that type, loaded by `POST /teams/{teamId}/requirement-artifacts/search` with `type`. It hides conditions already in this list. Picking sets the entry's `id`; typing sets its wording with no `id`. The search returns no `otherUseCases`, so a row picked in this session stays read-only until the next save reports whether it is shared.
  - Up and down buttons to reorder.
  - Its display number (`PRE-n` or `POST-n`, from its position) with its artifact key beside it.

  A row whose `otherUseCases` is not empty is read-only, shows "Also used by" with those use cases' keys and titles (extension 6b1), and has a **Make a separate copy** button (6b3). There is no "Edit for all" button yet.

## Sequence

### UC-DOC-create-use-case: saving a new use case

```mermaid
sequenceDiagram
    participant E as Use case editor (SPA)
    participant C as UseCaseController
    participant S as UseCaseService
    participant R as RequirementArtifactService
    participant DB as MySQL
    E->>C: POST /teams/{teamId}/use-cases (whole use case)
    C->>C: @Valid: required fields and all four lists present, else 400
    C->>C: convert (fields only) and build UseCaseReferences
    C->>S: saveUseCase(teamId, useCase, references)
    S->>S: replace submitted steps with copyOf copies (no ids)
    S->>R: findRequirementArtifactById(teamId, each actor id)
    R-->>E: 404 if an actor is not in this team (step 4, BR-team-scoped-access)
    S->>S: requireActor type check, else 400 (extension 4a)
    S->>S: resolveConditions: check every entry (see Preconditions and postconditions), else 400 or 404
    S->>R: saveRequirementArtifact(teamId, artifact): team from route, key UC-n
    S->>R: saveRequirementArtifact for each new condition: type from its list, key PRC-n or PSC-n
    S->>DB: save use case (steps cascade, conditions are links only)
    C-->>E: Result with the use case, its step ids and version
```

Every step, extension and extension step is saved as a new row whatever ids the body carried.

### UC-DOC-edit-use-case: saving the edited use case

```mermaid
sequenceDiagram
    participant E as Use case editor (SPA)
    participant C as UseCaseController
    participant S as UseCaseService
    participant R as RequirementArtifactService
    participant U as UseCase (managed)
    participant DB as MySQL
    E->>C: PUT /teams/{teamId}/use-cases/{useCaseId} (whole use case, version)
    C->>C: @Valid, convert (fields only), build UseCaseReferences
    C->>S: updateUseCase(teamId, useCaseId, update, references, version)
    S->>DB: find by id and team (scoped query), else 404
    S->>DB: findByIdWithStepGraph (whole step tree, one query)
    S->>R: resolve each actor id in this team, else 404
    S->>S: requireActor type check, else 400 (extension 7a)
    S->>S: resolveConditions: check every entry, else 400 (7a) or 404
    S->>S: version present (400) and current (409)
    S->>S: lock held by the caller (409 not locked, 423 someone else)
    S->>DB: lock(useCase, PESSIMISTIC_FORCE_INCREMENT)
    S->>U: artifact fields, trigger, actors
    S->>S: reword unshared conditions in place, then save new ones (PRC-n, PSC-n)
    S->>U: replace the ordered condition lists (links only)
    S->>U: reviseMainSteps(submitted steps)
    U->>U: OwnedChildren.revise, recursively per level
    S->>DB: save, flush (child inserts, updates, deletes, index updates)
    C-->>E: Result with the use case and its new version
```

The checks run in this order: route rule, body validation, use case in team, actors in team, actor type, conditions, version, lock. Nothing is written until all of them pass. After the save, `otherUseCasesReferencing` runs once more for the response.

### Preconditions and postconditions

Each condition entry in a request is `{ id?, condition }`, in list order, and `resolveConditions` applies one rule to it (BR-use-case-condition-shared, BR-shared-condition-wording):

```mermaid
flowchart TD
    A[Submitted entry, in its list] --> B{Has an id?}
    B -- no --> N[New condition: saveRequirementArtifact<br/>type from the list, team from the route,<br/>key PRC-n or PSC-n]
    B -- yes --> L[findRequirementArtifactById in the route's team]
    L -- not found --> X404[404, same message for another team's id<br/>and for an id that names nothing]
    L -- found --> T{Type matches the list?}
    T -- no --> X400a[400]
    T -- yes --> W{Wording changed?}
    W -- no --> K[Link as is]
    W -- yes --> S{Referenced by another use case?}
    S -- no --> R[Reword in place, covered by the use case lock]
    S -- yes --> X400b[400: names the key, says to make a separate copy<br/>extension 7a, 4a on create]
```

- The same `id` twice in one list, or an entry with empty wording, is a 400 for the whole request.
- "Make a separate copy" (extension 6b3) needs no endpoint of its own. The editor replaces the entry, at the same position, with the same wording and no `id`, and the rule stores it as a new condition. This is why the server never matches conditions on wording.
- Removing an entry removes only the link. The condition stays in the team's requirements graph, and is deleted only through UC-ART-delete-artifact.
- On create, every use case is "another" use case, since the new one references nothing yet. So an existing condition picked on create may be reworded only while no use case references it at all. Nothing else is affected, which is why no lock is needed.

**Details of the rule:**
- **Wording is the artifact's `content`.** A condition's `title` stays empty, as the seed already stores it. The request's `condition`, `SubmittedCondition.wording`, the response's `condition` and the picker's label all mean `content`.
- **"Changed" means not exactly equal.** The comparison is plain string equality, with no trimming or case folding. The editor sends a condition's wording back exactly as it received it, so an untouched shared condition is never refused.
- **The order of checks:** actors first, then preconditions in order, then postconditions in order. For each entry, the steps are: a repeated id, the scoped lookup (404), the type, then the wording. The first failure is reported. Empty wording is caught earlier, by `@NotBlank` on `ConditionDto.condition`, as a body validation 400 with its field path.
- **Error messages** name the list and the 1-based position, which is the display number. Examples:
  - "Precondition 2 is not a precondition."
  - "Postcondition 3 repeats a condition already in this list."
  - "PRC-7 is also used by other use cases; make a separate copy to change its wording here."
- **A new condition** gets a null `priority`, null `notes`, no document section (`sourceDocumentSection` stays null, since a condition belongs to use cases, not to a document section), and `createdBy` and `createdAt` from JPA auditing.
- **An in-place reword** sets `content` on the managed artifact. JPA auditing then updates its `updatedBy` and `updatedAt`.
- **`otherUseCases`** is filled on GET, POST and PUT alike, sorted by use case id. It shows each use case's artifact key as stored.
- **The artifact route is unchanged.** `PUT /requirement-artifacts/{id}` (UC-ART-edit-artifact) copies only title, content, priority and notes, so it cannot change a condition's type. Rewording through it is the "Edit for all" path of extension 6b2: it is allowed for every use case, and has no stale-save guard until OI-72.

**Keys and display numbers are different things.** A condition's artifact key is `PRC-n` for a precondition and `PSC-n` for a postcondition, minted once per team from that type's counter. It never changes, and it's what links, comments and search show. The display number, `PRE-1`, `PRE-2`, … or `POST-1`, …, is the condition's position in one use case only. A shared condition `PRC-7` can be PRE-1 in UC-3 and PRE-4 in UC-9.

**The condition types, and where each lives.** Five small types carry a condition through a request and back, each at one boundary:

```mermaid
flowchart TD
    J["JSON body"] --> D["ConditionDto<br/>API shape, validated by @Valid"]
    D -- "controller: submittedConditions" --> S["SubmittedCondition<br/>id or null, wording<br/>inside UseCaseReferences"]
    S -- "service: planConditions, checks only" --> P["PlannedCondition<br/>stored artifact or null, wording<br/>private to UseCaseService"]
    P -- "service: applyConditions, after the version and lock checks" --> A["RequirementArtifact<br/>the stored condition"]
    Q["UseCaseRepository sharing queries"] --> U["ConditionUse<br/>use case Y also references condition X"]
    U -- "service: otherUseCasesReferencing, grouped by condition id" --> O["OtherUseCaseDto<br/>id, artifactKey, title"]
    O --> R["ConditionDto.otherUseCases in the response"]
```

| Type | Layer | Why it is not one of the others |
|---|---|---|
| `ConditionDto` | API | The JSON contract, in both directions |
| `SubmittedCondition` | Controller to service | The service takes no API type; this is the conditions' share of `UseCaseReferences`, as the actors travel as ids |
| `PlannedCondition` | Inside `UseCaseService` | A checked result that must not be written yet: setting a new wording on the stored artifact early would be flushed before the version and lock checks, the same reason the actors are resolved onto the detached `update` |
| `ConditionUse` | Repository | A query row needs the condition id to group by, and a repository builds no API type |
| `OtherUseCaseDto` | API | The JSON shape of one entry of `otherUseCases`, without the internal condition id |

Two could be dropped, and both were kept on purpose: passing `ConditionDto` to the service instead of `SubmittedCondition` would make the service depend on an API type, and returning `ConditionUse` in the response instead of `OtherUseCaseDto` would leak a repository type and a redundant `conditionId` into the JSON.

### Pairing, at any one level

```mermaid
flowchart TD
    S[Submitted children, in order] --> P{"Id matches an unpaired child<br/>of this same parent?"}
    P -- yes --> U[Update that child in place.<br/>It keeps its id.]
    P -- "no: no id, unknown or foreign id,<br/>or an id already used" --> F[Fresh copy, no id]
    U --> R[revised list]
    F --> R
    R --> D[Remove own children not in revised.<br/>Orphan removal deletes them.]
    D --> M[Insert and move until own equals revised.<br/>Children already in place are not touched.]
    M --> O[At flush, @OrderColumn writes positions 0..n-1]
```

### UC-DOC-edit-use-case: the use case lock

```mermaid
stateDiagram-v2
    [*] --> Unlocked
    Unlocked --> Locked : PUT lock (expiresAt = now + TTL)
    Locked --> Locked : PUT lock by the holder (extended to now + TTL)
    Locked --> Locked : PUT lock by anyone else (423, no change)
    Locked --> Unlocked : DELETE lock by the holder or an instructor
    Locked --> Unlocked : expiresAt passes (cleared on the next read or save)
```

The TTL is `ram.lock.default-lock-ttl` (15 minutes by default). Expiry is lazy: nothing runs on a timer, and every read of the lock clears an expired one first. A save does not extend the lock; the editor extends it by sending `PUT lock` again.

## API contract

All routes are under the API base URL and return the `Result` envelope ([Architectural conventions](architectural-design.md#architectural-conventions)). "Team member" means the route rule's `TeamSecurityService.canAccessTeam`: a student on that team, or an instructor of the team's course section. A caller who fails it gets 403 before the service runs, and so does an id that names no team.

| Endpoint | Caller (who may) | Request | Success | Errors |
|---|---|---|---|---|
| `GET /teams/{teamId}/use-cases/{useCaseId}` | team member | none | The use case (fields below) | 404 use case not in this team |
| `POST /teams/{teamId}/use-cases` | team member | The use case fields below; `version` is not sent | The stored use case, with its artifact key, step ids, condition ids and keys, and `version` | 400 a required field or list missing; 400 an actor not a stakeholder or external system (4a); 400 a condition entry with empty wording, a repeated id, an id of the other list's type, or a changed wording of a condition another use case references (4a); 404 an actor or a condition not in this team |
| `PUT /teams/{teamId}/use-cases/{useCaseId}` | team member holding the use case lock | The use case fields below and its current `version`; each step, extension and extension step sends back its `id` to keep that row | The stored use case with the **new** `version`, which the next save must send | 400 a required field or list missing, or no `version`; 400 an actor not a stakeholder or external system (7a); 400 a condition entry as on POST, including a changed wording of a shared condition (7a); 404 the use case, an actor or a condition not in this team; 409 stale `version`; 409 not locked by anyone; 423 locked by someone else |
| `GET /teams/{teamId}/use-cases/{useCaseId}/lock` | team member | none | The lock (fields below); an expired lock reads as unlocked | 404 use case not in this team |
| `PUT /teams/{teamId}/use-cases/{useCaseId}/lock` | team member | `reason` (optional) | The lock, held by the caller until `expiresAt`; the holder sending it again extends it | 404 use case not in this team; 423 locked by someone else (the message names the holder and the expiry) |
| `DELETE /teams/{teamId}/use-cases/{useCaseId}/lock` | the lock holder, or any instructor who passes the route rule | none | No data | 403 the caller is a student who does not hold the lock (including when no one does); 404 use case not in this team |

The editor lists a team's use cases through the requirement artifact search, `POST /teams/{teamId}/requirement-artifacts/search` with `type` `USE_CASE`, which the ART area owns.

**Use case fields (request and response).**

- `title`, `description`, `trigger`: required, not empty.
- `primaryActorId`: required; a stakeholder or external system of this team.
- `secondaryActorIds`: required, may be empty; each a stakeholder or external system of this team. The ids form a set: a repeated id is stored once, the order is not kept, and the primary actor may also appear here.
- `preconditions`, `postconditions`: required, may be empty, in order. In a request, each entry is `id` (optional: an existing condition of this team, of the list's type) and `condition` (the wording, required, not empty); `type`, `priority` and `notes` are ignored. In a response, each entry also has `artifactKey`, `type`, `priority`, `notes` and `otherUseCases`, the other use cases that reference it, each as `id`, `artifactKey` and `title`.
- `mainSteps`: required, may be empty, in order; each has `id`, `actor`, `actionText` and `extensions` (in order). A step's `actor` is free text, not an artifact reference, so BR-use-case-actor-types does not apply to it. Each extension has `id`, `conditionText`, `kind`, `exit` and `steps` (in order), and each extension step has `id`, `actor` and `actionText`.
- `priority`, `notes`: optional.
- `teamId`: required by validation for compatibility, and **ignored**: the team is always the route's.
- `version`: required on PUT, ignored on POST.
- Response only (ignored in a request): `id`, `artifactKey`, `createdAt`, `updatedAt`, `createdBy`, `updatedBy`, `lock`.

**Actor error messages.** Actors are checked primary first, then each secondary actor, and the first failure is reported. An actor outside the team gives `ObjectNotFoundException("requirement artifact", id)`, the same text the artifact routes give for an id that names nothing. An actor of the wrong type gives a 400 naming the field and the id, for example "Secondary actor 12 must be a stakeholder or an external system.", which is the location extensions 4a1 and 7a1 ask for. The two 409s on PUT differ only in their message: a stale version means reload, and "not locked" means take the lock again.

**Condition error messages.** A condition outside the team gives the same `ObjectNotFoundException("requirement artifact", id)` as an actor. Rewording a shared condition gives a 400 naming its key, for example "PRC-7 is also used by other use cases; make a separate copy to change its wording here.", the location extensions 4a1 and 7a1 ask for.

**Lock fields (response).** `locked`, `useCaseId`, `lockedBy`, `lockedAt`, `expiresAt`, `reason`.

## Key decisions

| Decision | Chosen | Rejected, and why |
|---|---|---|
| How a save sends the use case | The whole use case on every PUT, guarded by one version | Per-field or per-step PATCH routes: the ordered step tree would need insert, move and delete operations of its own, and each would need its own concurrency guard |
| How actor ids reach the service | A `UseCaseReferences` parameter beside the converted use case (`UseCaseActorIds` in the actors slice) | Placeholder artifacts carrying only an id, swapped by the service: fails open, because a path that skipped the swap would let Hibernate link whatever id it carried, from any team |
| What checks that an actor is in the team | The team-scoped load is the check, in one pass | Loading in the converter and re-checking in the service: two lookups, two error messages, and the converter's message showed which ids exist in other teams |
| Order of the actor refusals | Team scope first (404), then type (400) | Type first: a 400 against a 404 would reveal the type of another team's artifact |
| Which artifacts can be actors | `STAKEHOLDER` and `EXTERNAL_SYSTEM` only (BR-use-case-actor-types) | Also `EXTERNAL_INTERFACE_REQUIREMENT`, which the editor used to offer for systems such as an email service: an interface requirement describes an interface, not a participant |
| A list missing from the body | 400 by `@NotNull` | Treating it as empty: on a PUT that would silently remove every secondary actor, condition or step |
| The body's `teamId` | Kept in the DTO and ignored by the server | Removing it: reaches into the frontend for no behavior change; it can go when the DTO is next reshaped |
| How a condition is named in a request | `{ id?, condition }` per entry, resolved by the service in the route's team | Conditions as entities built by the converter (as before): every save inserted new rows, and a converter can't scope ids |
| How a new condition is stored | `saveRequirementArtifact`, then linked; the cascade is removed | Cascade PERSIST from `UseCase` (as before): skipped the key and let the body choose the type. It is also why the actors slice needed the `giveConditionsTheTeam` stopgap, which is deleted. |
| Same wording as an existing condition | Always a new condition | Reuse by wording: makes "Make a separate copy" impossible, and needs a rule for how exact a match must be |
| Who may reword a condition through a use case | Only while no other use case references it (BR-shared-condition-wording) | Always: would silently change the other use cases |
| "Edit for all" (extension 6b2) | Not built yet | Now: it needs the stale-save guard of UC-ART-edit-artifact extension 10a, a `@Version` on `RequirementArtifact` (OI-51), which affects every artifact route |
| How the order of conditions is stored | A `List` with `@OrderColumn` on each join table. Allowed because `UseCase` owns both associations (it declares the `@JoinTable`, and nothing maps them back with `mappedBy`). The Hibernate 7.4 User Guide, §3.9.2 Mapping Lists, requires `@OrderColumn` to keep a list's order and forbids it only on a `@ManyToMany(mappedBy = …)` side; §3.9.12 Ordered Lists puts the order column "in the collection link table". | A `Set` (as before): no order at all, and the request's `Set<ConditionDto>` merged two new entries with the same wording. A position field on the condition: the position belongs to the link, since one condition sits at different positions in different use cases. |
| Join table primary key | `(*_index, use_case_id)`, exactly what Hibernate generates for these `@ManyToMany` lists; a repeated condition is refused by the service. The guide shows such a key only for a `@OneToMany` link table (§3.9.12, "PRIMARY KEY ( Person_id, order_id )"), so `/implement` read the key Hibernate generates in dev before writing V9. It has the two expected columns, with the index column first, and the artifact column gets an index of its own. V9 creates exactly that, so production matches what the tests run against. | Keeping `(artifact_id, use_case_id)`: if Hibernate reorders by updating the artifact at each index, a swap would briefly put one artifact on two rows, a primary key collision. The guide doesn't show the SQL Hibernate runs to reorder an indexed list, so this is inferred, and the reorder test below checks it. |
| Where "shared" is computed | Two JPQL queries in `UseCaseRepository`, run during the save and for every response | A reference count stored on the artifact: one more value to keep in sync, for no gain at use case size |
| Condition key prefixes | `PRC` and `PSC`, one counter per type as for every artifact | `PRE` and `POST` (as before): read exactly like the display numbers PRE-1 and POST-1. `CND` for both: would need the two types to share one counter, the only exception in key generation, and the key would not say pre or post. |
| Condition rows already stored | No delete and no key backfill; existing join rows are numbered once by V9 | A cleanup migration: it can't tell an orphan from a condition kept for reuse, which is legitimate from this slice on |
| Key prefix for external systems | `SYS` | `EXT`: reads as "extension" inside a use case |
| How the order of steps is stored | Keep `@OrderColumn` on the inverse lists; Hibernate writes and renumbers the position column | `@OrderBy` on a position field: the lists become bags, and the nested fetch joins (`findByIdWithStepGraph`) then fail with `MultipleBagFetchException`. Hibernate's boot warning HHH160246 about this mapping is accepted; the 7.4 User Guide (Mapping Lists) says the order column must be applied explicitly on the inverse side. |
| Direction of the step associations | Bidirectional (`mappedBy` plus a back-reference on each child) | Unidirectional `@OneToMany` with `@JoinColumn`: workable, but it changes the mapping of all three levels and gains nothing for this flow. |
| How a submitted child finds its stored row | By id, among **this parent's own children only** | By position: inserting one step would rewrite the text of every step after it and lose their ids. Global lookup by id: a request-body id must never select a row outside the parent named by the route (OI-46). |
| An id the parent does not own (stale, foreign, or repeated) | Treated as new: stored as a fresh row | Refuse with 400: a stale id after a concurrent delete is normal editor behavior, not an attack, and refusing would lose the user's text. |
| How the stored step list is edited | Explicit remove, insert and move; never `clear()` and refill | Clear and refill: every save looks like a total rewrite, and keeping ids then depends on JPA `merge` semantics rather than on code we can read. |
| Create path for steps | Every child saved as a `copyOf`, no ids | Saving the submitted objects: a body-supplied child id would reach `save()`. |
| Fields no DTO carries (`label`, `resumeStepNo` on an extension) | Left untouched by `reviseFrom` | Copying them from the submission: it would null them on every save. |
| Version on every accepted save | `entityManager.lock(useCase, PESSIMISTIC_FORCE_INCREMENT)` after the version and lock checks | Rely on Hibernate's dirty check: it bumps the version only when a `UseCase` field or collection changes, and most edits change the artifact or child rows, so stale saves were accepted. `OPTIMISTIC_FORCE_INCREMENT`: writes just before commit, so the response carries the old version (and rolled-back tests cannot see it). |
| Version check and lock check together | Both, version first | The lock alone: a lock that expired, was taken by a teammate who saved, and was then retaken would let the first student's stale form overwrite the teammate's save |
| Where the lock lives | A separate `UseCaseLock` entity with its own version | Lock columns on `use_case`: taking or extending a lock would increment the use case's version and make the holder's own form stale |
| How a lock ends | A TTL with lazy expiry, plus release by the holder or an instructor | No TTL: a closed browser tab would hold the use case forever. A scheduled sweeper: an extra moving part for something every read can check |
| Where the step logic lives | Domain methods on the aggregate plus one generic helper | Three copies of the algorithm, one per level, or the logic in the service: it needs no repository, so it belongs in the domain. |

Two consequences worth knowing:

- The version can go up by 2 in one save, once for the forced increment and once more if a `UseCase` collection (secondary actors, condition lists) is also dirty. Only "it changed" matters to the stale-save check.
- `replaceMainSteps` still exists: the DTO converters use it to build the detached submitted tree. Step ids still travel through the converters on purpose, as pairing keys.

## Data model

Three migrations, and no table added:

- `V7__add_external_system_artifact_type.sql` adds `EXTERNAL_SYSTEM` to the `type` enum column of `requirement_artifact` and `artifact_key_sequence`, keeping the value list alphabetical to match Hibernate's generated definition (the V4 pattern).
- `V8__rename_secondary_actor_column.sql` renames `use_case_secondary_actor.stakeholder_artifact_id` to `actor_artifact_id`, since a secondary actor may now be an external system. MySQL 8 keeps the foreign key and the composite primary key across `RENAME COLUMN`. Added during implementation, with the developer's agreement; it was not in the approved design.
- `V9__order_use_case_conditions.sql`, for each of `use_case_precondition` and `use_case_postcondition`:
  1. Adds `precondition_index` (or `postcondition_index`).
  2. Numbers the existing rows per use case from 0, with `ROW_NUMBER() - 1` (`@OrderColumn` is 0-based), ordered by artifact id, since the old order was undefined.
  3. Makes the column `NOT NULL`.
  4. Adds an index on the artifact column, so its foreign key keeps one.
  5. Replaces the primary key with `(precondition_index, use_case_id)` (or the postcondition pair), the key Hibernate generates for the mapping, read from the dev schema in `/implement` (see Key decisions).

  V9 was checked by applying V1 to V9 to a scratch MySQL 8 database with sample rows: each use case's rows were numbered from 0 by artifact id, and both tables then matched Hibernate's generated schema in columns, primary key, key names and foreign keys.

```mermaid
erDiagram
    use_case ||--o{ use_case_precondition : "ordered by precondition_index"
    requirement_artifact ||--o{ use_case_precondition : "precondition_artifact_id"
    use_case ||--o{ use_case_postcondition : "ordered by postcondition_index"
    requirement_artifact ||--o{ use_case_postcondition : "postcondition_artifact_id"
```

The ER delta only; the tables themselves are the SRS's Business Domain Model.

The step tables and their `*_index` order columns are unchanged. The dev seed adds one team-1 external system, "Email service", as the last artifact it saves, so no seeded id shifts. For the same reason it is not attached to the seeded use case, whose save comes much earlier; tests that need it as an actor attach it themselves. The seeded use case's conditions are passed as `SubmittedCondition`s and saved after its artifact, so they get keys and no seeded id shifts.

## Reuse & cross-cutting

- Authorization: the route rule (`TeamMembershipAuthorizationManager`) and the team-scoped query, as the architecture-of-record's [Authorization](architectural-design.md#authorization) requires; `RequirementArtifactService.findRequirementArtifactById` is the scoped finder for actors and conditions.
- Use case locking (BR-edit-lock-required, BR-lock-expiry): the save is refused unless the caller holds the lock.
- Optimistic locking (OI-51): `UseCase` is the aggregate root, so its `@Version` covers all three child levels.
- Artifact keys: `RequirementArtifactService.saveRequirementArtifact` mints the keys of the use case and of every new condition, as it does for every artifact.
- Error mapping: `ExceptionHandlerAdvice` already maps `MethodArgumentNotValidException` and `IllegalArgumentException` to 400, `ObjectNotFoundException` to 404, the two lock exceptions to 409 and 423, and `OptimisticLockException` to 409.
- The same pairing rule (ids only select among the parent's own children) is used by `DocumentSectionService.mergeSubmittedArtifacts` for a document section's requirement artifacts.

## Tests

| Use case, flow | Level | Asserts |
|---|---|---|
| UC-DOC-create-use-case, main flow | integration, `UseCaseControllerTest` | A stakeholder primary actor and an external-system secondary actor are stored; the response shows both |
| UC-DOC-create-use-case, extension 4a (primary) | integration, `UseCaseControllerTest` | A primary actor of another type in the caller's own team (for example a feature) is refused with 400; no use case is stored |
| UC-DOC-create-use-case, extension 4a (secondary) | integration, `UseCaseControllerTest` | One secondary actor of the wrong type is refused with 400, and the message names `Secondary actor` and its id |
| UC-DOC-edit-use-case, extension 7a | integration, `UseCaseControllerTest` | The same two refusals on PUT; the stored actors and version are unchanged |
| Actor from another team (create and edit) | integration, `UseCaseControllerTest` | Refused with 404, with the same message as an id that names nothing |
| Missing fields (create and edit) | integration, `UseCaseControllerTest` | Each of `primaryActorId`, `secondaryActorIds`, `preconditions`, `postconditions` and `mainSteps` omitted gives 400, not 500 |
| Body `teamId` is ignored (OI-46) | integration, `RequestBodyIdIntegrationTest` | With another team's id in the body, every stored condition belongs to the route's team and the other team gains no rows. Written first and run against the unchanged code; still holds with new conditions saved through `saveRequirementArtifact` |
| A condition id from another team (OI-46) | integration, `RequestBodyIdIntegrationTest` | The update probe naming team 2's artifact as a precondition now gets 404, and team 2's artifact is unchanged |
| External system key | unit, `ArtifactKeyPrefixTest`; integration | `ArtifactKeyPrefix.of(EXTERNAL_SYSTEM)` is `SYS`, and no artifact type falls back to `UNK`; the seeded external system has the key `SYS-1` (asserted in the create main-flow test) |
| Pairing rules | unit, `UseCaseStepRevisionTest` | Paired child updated in place; unpaired, foreign and repeated ids become fresh copies; omitted children removed; order follows the submission; extension ids pair only within their own step; `label` and `resumeStepNo` survive |
| How order is stored | integration, `UseCaseStepOrderingIntegrationTest` | Contiguous indexes on create, insert, remove and move; nested lists numbered per parent; the step-graph fetch has no duplicates |
| UC-DOC-edit-use-case, editing scenarios | integration, `UseCaseStepOrderingIntegrationTest` | Two saves in a row; remove an extension; edit an extension's kind and exit; insert an extension step; edit actors; remove every step |
| UC-DOC-edit-use-case, version | integration, `UseCaseStepOrderingIntegrationTest` | A text-only edit increments the stored version; a second save with the returned version succeeds |
| UC-DOC-create-use-case, ids in the body | integration, `UseCaseStepOrderingIntegrationTest` | Steps submitted with existing ids are stored as new rows |
| UC-DOC-create-use-case, new conditions | integration, `UseCaseConditionIntegrationTest` | Stored with the list's type (a body `type` of `STAKEHOLDER` is ignored), the route's team, keys `PRC-n` and `PSC-n`, in the submitted order |
| UC-DOC-create-use-case, an existing condition picked | integration, `UseCaseConditionIntegrationTest` | Linked by id; no new row |
| Two new entries with the same wording | integration, `UseCaseConditionIntegrationTest` | Both stored as separate conditions, in order |
| UC-DOC-edit-use-case, reorder | integration, `UseCaseConditionIntegrationTest` | The order follows the submission and the condition ids are unchanged. Two cases: swap two conditions, and move the last one to the front. Both must save with no constraint error, on MySQL with the primary key V9 creates. |
| UC-DOC-edit-use-case, remove | integration, `UseCaseConditionIntegrationTest` | The link is dropped; the condition row still exists |
| UC-DOC-edit-use-case, reword an unshared condition | integration, `UseCaseConditionIntegrationTest` | Same id, new wording |
| UC-DOC-edit-use-case, extension 7a, reword a shared condition | integration, `UseCaseConditionIntegrationTest` | 400 naming the key; the stored wording and the version are unchanged |
| UC-DOC-edit-use-case, extension 6b3, separate copy | integration, `UseCaseConditionIntegrationTest` | The shared wording with no id becomes a new condition at the same position; the other use case still has the original |
| `otherUseCases` in the response | integration, `UseCaseConditionIntegrationTest` | Lists every other use case referencing a condition; empty when unshared |
| Condition refusals | integration, `UseCaseConditionIntegrationTest` | Another team's or an unknown condition id gives 404 with the same message. A repeated id, a precondition id in the postcondition list, and empty wording each give 400, with the messages under "Details of the rule". |
| Wording comparison | integration, `UseCaseConditionIntegrationTest` | Sending a shared condition back with its exact stored wording is accepted; adding one trailing space is refused (7a) |
| Condition key prefixes | unit, `ArtifactKeyPrefixTest` | `ArtifactKeyPrefix.of` gives `PRC` and `PSC` |
| Seed | integration, `UseCaseConditionIntegrationTest` | The seeded use case keeps id 3. Its three preconditions and two postconditions have `PRC-n` and `PSC-n` keys, in the order `DataInitializer` lists them. |
| Migration V9 | manual, scratch MySQL (done) | V1 to V9 apply in order; existing join rows are numbered 0..n-1 per use case; the primary key is `(use_case_id, *_index)` |
| Request-body step ids (OI-46) | integration, `RequestBodyIdIntegrationTest` | A step id from another team's use case is stored as a new step; the other team's step is unchanged |
| Regression | existing | `UseCaseControllerTest` (including `addUseCase_NotSameTeam`) stays green |

All integration tests flush, clear, and read the database with native SQL, because the test transaction never commits.

## Open questions / risks

- **"Edit for all" (extension 6b2) is not built.** It waits for the stale-save guard of UC-ART-edit-artifact extension 10a, which needs OI-51's decision on `RequirementArtifact`. Tracked as OI-72.
- **`UseCaseService.findUseCaseByIdBasic` has no callers.** It predates this slice and its Javadoc is wrong (it loads the primary actor and the lock, not only the artifact). Proposed for deletion as a follow-up.
- **Deleting a condition that is still referenced** through UC-ART-delete-artifact hits a foreign key and gives a 500, since BR-deletion-integrity is not built (OI-26). This slice doesn't change that.
- **A race between two use case saves.** One rewording a condition while another links it can both pass the "not shared" check. It's rare at use case scale, and accepted; an artifact `@Version` (OI-51) would close it.
- **Condition rows from before this slice** stay as they are: keyless, and unlinked where a save replaced them. No one had used the editor in production, so there should be few or none. Check with a read-only count before deploying V9.
- **`ENABLES` links** (postcondition to precondition) are in the spec but not in the backend link-type enum or its validator. That's a separate small slice under the UC-LNK use cases.
- `SecurityConfiguration` has route rules for `POST /teams/{teamId}/use-cases/search` and `DELETE /teams/{teamId}/use-cases/{useCaseId}`, but `UseCaseController` handles neither, so both answer 404. UC-DOC-delete-use-case will need the delete handler.
- **Gaps between the spec and the code as built, outside this slice.** The questions test on 2026-11-14 surfaced these; none is built today, and each is tracked as OI-66 to OI-71 in the order listed:
  - BR-use-case-name-unique (create 5a): nothing checks that a use case name is unique in the team, on create or on rename.
  - Edit step 12 and extension 6a: the system is meant to release the lock after a save, and to save and release when the student navigates away. The editor instead has separate Lock and Unlock buttons, and a save leaves the lock held until the TTL.
  - BR-lock-expiry says a lock expires an interval "after it is acquired", but the holder can extend it indefinitely by locking again. Either the rule should say "after it is acquired or last extended", or extending should go.
  - PRE-3 of both use cases (the document is not locked for review, BR-review-lock): not checked, because review and submission are not built yet.
  - Both use cases name only the student as the actor, but the route rule also lets an instructor of the course section create, edit and lock (BR-role-based-access). Decide whether that's intended.
  - POST-2 (authorship metadata): `updatedAt` and `updatedBy` live on the artifact and change only when an artifact field changes, so an edit to steps alone records no editor.
- The SwaggerHub definition of these routes is stale from this change on, until it is reconciled with the contract above.
- `label` and `resumeStepNo` are stored but no DTO or screen uses them.
- With `@OrderColumn`, Hibernate rewrites every child's position on any insert, remove or move. That is fine at use case size; it would matter for long lists such as comment threads.
