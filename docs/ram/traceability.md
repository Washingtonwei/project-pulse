# Traceability Matrix

The map from **spec → code**. Each use case in `requirements/use-cases.md` gets one row linking it to the cross-cutting functional requirements it depends on and the actual frontend/backend/test artifacts that implement it. This is how Claude (and you) find the right code from a requirement, and — via the **Status** column — see at a glance **which use cases are implemented and which are not**. Note: in this project a **use case is itself a (high-level) functional requirement**, and its "The system …" steps + Associated Information are its detailed functional spec (SRS §5.1); the SRS §5.2 "shall" statements capture only the non-use-case, system-level behaviors.

> Update the relevant row at the end of every `/feature` run (Phase 5).

## How to use it

- **One row per use case**, in `UC-<AREA>-<n>` order, mirroring `requirements/use-cases.md`.
- **FR IDs** — the SRS §5.2 system-level functional requirements this use case **depends on / builds upon** (e.g. locking, autosave, validation, real-time collaboration, export, AI). The use case's own "The system …" steps are its primary acceptance criteria; this column points to the reusable cross-cutting subsystems behind it. `—` means the use case is self-contained (its steps are the whole spec). The most universal behaviors — authorship metadata (`FR-HIS-4`) and RBAC (`FR-SEC-*`) — apply to nearly every row and are omitted here to reduce noise.
- **Frontend / Backend / Tests** — concrete artifacts (`file_path` or component/class names) once built; `—` until then.
- **Status** — implementation state, from the legend below.
- When a use case is added to `requirements/use-cases.md`, add its row here (start at `❌ Not started`). When `/feature` finishes it, flip the status and fill the code columns.

## Status legend

| Symbol | Meaning |
|--------|---------|
| ❌ Not started | No code yet. |
| 🔵 Planned | Spec is ready and an implementation plan exists; not yet coded. |
| 🟡 In progress | Partially implemented. |
| ✅ Implemented | Code complete and automated tests pass. |
| 🔎 Needs verification | Built, but not yet confirmed against the use case flow (or tests incomplete). |
| 🚫 Tabled | Deliberately out of MVP scope (e.g., UC-ART-4). Not a gap to fill. |

## Matrix

> Mapped against the repository on 2026-06-08. `Frontend` cites the Vue page(s) + `apis/ram` call(s); `Backend` cites the controller (+ key method) and supporting service/entity; `Tests` cites the backend test class + methods (`—` = none yet). Frontend modules are under `frontend/src/` (`pages/ram/*.vue`, `apis/ram/`); backend modules under `backend/src/main/java/team/projectpulse/ram/`; tests under `backend/src/test/java/team/projectpulse/ram/`. No frontend automated tests exist for RAM yet. Note: the glossary endpoints (`GlossaryController`) and the requirement-artifact delete endpoint currently have **no** backend tests — see the `—` cells.

| UC | Use Case | Status | FR IDs | Frontend | Backend | Tests |
|----|----------|--------|--------|----------|---------|-------|
| UC-TPL-1 | The Course Admin regenerates team documents from built-in templates | ✅ Implemented | FR-TPL-1, FR-TPL-3 | `Teams.vue` → `apis/ram` `createDocument` | `DocumentController.createDocument` → `DocumentService.createRequirementDocument` + `DocumentTemplateRegistry` (template YAMLs) | `DocumentControllerTest.createVisionScopeDocument`, `…ByStudent` |
| UC-GLO-1 | The Student views the Project Glossary | ✅ Implemented | — | `RamGlossary.vue` → `searchRequirementArtifacts` | `RequirementArtifactController.findRequirementArtifactsByCriteria` (+ `RequirementArtifactSpecs`) | `RequirementArtifactControllerTest.findRequirementArtifactsByCriteria1..3` |
| UC-GLO-2 | The Student finds glossary terms | ✅ Implemented | — | `RamGlossary.vue` (search/filter) → `searchRequirementArtifacts` | `RequirementArtifactController.findRequirementArtifactsByCriteria` (+ `RequirementArtifactSpecs`) | `RequirementArtifactControllerTest.findRequirementArtifactsByCriteria1..3` |
| UC-GLO-3 | The Student views a glossary term | ✅ Implemented | — | `RamGlossary.vue` → `getGlossaryTermById` | `GlossaryController.findGlossaryTermById` → `GlossaryService` | — |
| UC-GLO-4 | The Student creates a glossary term | ✅ Implemented | FR-GLO-1, FR-GLO-2 | `RamGlossary.vue` → `createGlossaryTerm` | `GlossaryController.createGlossaryTerm` → `GlossaryService` | — |
| UC-GLO-5 | The Student changes a glossary term definition | ✅ Implemented | FR-GLO-1, FR-GLO-2 | `RamGlossary.vue` → `updateGlossaryTermDefinition` | `GlossaryController.updateGlossaryTermDefinition` | — |
| UC-GLO-6 | The Student renames a glossary term | ✅ Implemented | FR-VAL-4 | `RamGlossary.vue` → `renameGlossaryTerm` | `GlossaryController.renameGlossaryTerm` | — |
| UC-GLO-7 | The Student deletes a glossary term | 🟡 In progress | — | — (no delete control in `RamGlossary.vue`) | `RequirementArtifactController.deleteRequirementArtifact` (generic artifact delete; no glossary-specific endpoint) | — |
| UC-DOC-1 | The Student views a section-based requirement document | ✅ Implemented | — | `RamDocuments.vue` → `searchDocuments`; `RamDocumentEditor.vue` → `findDocumentById` | `DocumentController.findDocumentById` / `findDocumentsByCriteria` → `DocumentService.findDocumentByIdWithFullGraph` | `DocumentControllerTest.findDocumentById*`, `findDocumentsByCriteria*` |
| UC-DOC-2 | The Student edits a section-based requirement document | ✅ Implemented | FR-LOCK-1..5, FR-SAVE-1, FR-SAVE-2, FR-VAL-1 | `RamDocumentEditor.vue` → `findDocumentSectionById`, `updateDocumentSection`, `getDocumentSectionLock`, `lock`/`unlockDocumentSection` | `DocumentSectionController` → `DocumentSectionService` (+ `DocumentSectionLock`) | `DocumentSectionControllerTest.*` (update / lock / unlock / `*_LockedByAnotherUser`, `*_NotLocked`) |
| UC-DOC-3 | The Student views the Use Cases document | ✅ Implemented | — | `RamUseCases.vue` (sidebar) → `searchRequirementArtifacts` | `RequirementArtifactController.findRequirementArtifactsByCriteria`; `UseCaseController` | `RequirementArtifactControllerTest.findRequirementArtifactsByCriteria1..3` |
| UC-DOC-4 | The Student views a use case | ✅ Implemented | — | `RamUseCases.vue` → `getUseCaseById` | `UseCaseController.findUseCaseById` → `UseCaseService` | `UseCaseControllerTest.findUseCaseById` |
| UC-DOC-5 | The Student creates a use case | ✅ Implemented | FR-VAL-3 | `RamUseCases.vue` → `createUseCase` | `UseCaseController.addUseCase` → `UseCaseService` | `UseCaseControllerTest.addUseCase`, `addUseCase_NotSameTeam` |
| UC-DOC-6 | The Student edits a use case | ✅ Implemented | FR-LOCK-1..5, FR-SAVE-1, FR-SAVE-2, FR-VAL-1 | `RamUseCases.vue` → `updateUseCase`, `getUseCaseLock`, `lock`/`unlockUseCase` | `UseCaseController.updateUseCase` / lock endpoints (+ `UseCaseLock`) | `UseCaseControllerTest.updateUseCaseWith*`, `lockUseCase*`, `unlockUseCase*` |
| UC-ART-1 | The Student finds requirement artifacts | 🟡 In progress | — | — (no standalone artifact browser; artifacts surface embedded in `RamDocumentEditor.vue` / `RamGlossary.vue` / `RamUseCases.vue` via `searchRequirementArtifacts`) | `RequirementArtifactController.findRequirementArtifactsByCriteria` (+ `RequirementArtifactSpecs`) | `RequirementArtifactControllerTest.findRequirementArtifactsByCriteria1..3` |
| UC-ART-2 | The Student views a requirement artifact | 🟡 In progress | — | — | `RequirementArtifactController.findRequirementArtifactById` | `RequirementArtifactControllerTest.findRequirementArtifactById_Admin`, `_SameTeam`, `_NotSameTeam` |
| UC-ART-3 | The Student creates a requirement artifact | 🟡 In progress | FR-VAL-3 | — | `RequirementArtifactController.addRequirementArtifact` (+ `ArtifactKeySequence`) | `RequirementArtifactControllerTest.addRequirementArtifact_SameTeam`, `_NotSameTeam` |
| UC-ART-4 | The Student creates a requirement artifact from document content (promote selection) | 🚫 Tabled | — | — | — | — |
| UC-ART-5 | The Student edits a requirement artifact | 🟡 In progress | — | — | `RequirementArtifactController.updateRequirementArtifact` | `RequirementArtifactControllerTest.updateRequirementArtifact` |
| UC-ART-6 | The Student deletes a requirement artifact | 🟡 In progress | — | — | `RequirementArtifactController.deleteRequirementArtifact` | — |
| UC-LNK-1 | The Student views requirement artifact links | 🟡 In progress | — | — | `ArtifactLinkController.findArtifactLinksByCriteria` (+ `ArtifactLinkSpecs`) | `ArtifactLinkControllerTest.findArtifactLinksByCriteria` |
| UC-LNK-2 | The Student views a requirement artifact link | 🟡 In progress | — | — | `ArtifactLinkController.getArtifactLinkById` | `ArtifactLinkControllerTest.findArtifactLinkById`, `_NotSameTeam` |
| UC-LNK-3 | The Student creates a requirement artifact link | 🟡 In progress | — | — | `ArtifactLinkController.addArtifactLink` | `ArtifactLinkControllerTest.addArtifactLink`, `_NotSameTeam` |
| UC-LNK-4 | The Student edits a requirement artifact link | 🟡 In progress | — | — | `ArtifactLinkController.updateArtifactLink` | `ArtifactLinkControllerTest.updateArtifactLink` |
| UC-LNK-5 | The Student deletes a requirement artifact link | 🟡 In progress | — | — | `ArtifactLinkController.deleteArtifactLink` | `ArtifactLinkControllerTest.deleteArtifactLink` |
| UC-LNK-6 | The Student traces a requirement across levels | 🟡 In progress | — | — | `ArtifactLinkController.getArtifactTraceabilityByRequirementArtifactId` → `ArtifactTraceability` DTO (upstream/downstream link views) | `ArtifactLinkControllerTest.findArtifactLinksForOneArtifact`, `_NotSameTeam` |
| UC-VAL-1 | The Student runs validation (ReqLint) on the current document | ❌ Not started | FR-VAL-1, FR-VAL-5, FR-VAL-6 | — | — | — |
| UC-COL-1 | The Student collaboratively edits a requirement document | ❌ Not started | FR-COL-1, FR-COL-2, FR-COL-3, FR-COL-4 | — | — | — |
| UC-COL-2 | The Student comments on a requirement document | 🟡 In progress | — | — | `CommentController` (`createCommentThreadForDocument` / `…ForDocumentSection` / `…ForRequirementArtifact`, `addCommentToCommentThread`, list/get) → `CommentService` | `CommentControllerTest.*` (list/create/get/add/update/delete comment & thread) |
| UC-COL-3 | The Student resolves a comment | 🟡 In progress | — | — | `CommentController.updateCommentThreadStatus` (+ `CommentThreadStatus`) | `CommentControllerTest.resolveACommentThread` |
| UC-REV-1 | The Student submits requirements for review | 🟡 In progress | FR-NOT-1 | — (no `apis/ram` status call) | `DocumentController.updateDocumentStatus` → `DocumentStatus.SUBMITTED` (email notification FR-NOT-1 not built) | `DocumentControllerTest.updateDocumentStatus`, `_NotSameTeam` |
| UC-REV-2 | The Instructor reviews a team's requirement documents | 🟡 In progress | FR-NOT-1 | — | `DocumentController.updateDocumentStatus` → `DocumentStatus.RETURNED`, `findDocumentById` (email notification FR-NOT-1 not built) | `DocumentControllerTest.updateDocumentStatus`, `_NotSameTeam` |
| UC-EXP-1 | The Student exports a requirement document | ❌ Not started | FR-EXP-1, FR-EXP-2 | — | — | — |
| UC-EXP-2 | The Student exports all the requirement documents as a bundle | ❌ Not started | FR-EXP-1, FR-EXP-2 | — | — | — |
| UC-CFG-1 | The Instructor configures the teaching context for a course section | ❌ Not started | FR-AI-6 | — | — | — |
| UC-CFG-2 | The Instructor enables or disables AI assistants for a course section | ❌ Not started | FR-AI-7 | — | — | — |
| UC-CFG-3 | The Instructor configures the assistant instructions for a course section | ❌ Not started | FR-AI-16 | — | — | — |
| UC-CFG-4 | The Instructor configures the cross-document review criteria for a course section | ❌ Not started | FR-AI-22 | — | — | — |
| UC-AI-1 | The Student imports client pitch materials as project source | ❌ Not started | FR-IMP-1, FR-IMP-2 | — | — | — |
| UC-AI-2 | The Student elicits requirements with the elicitation assistant | ❌ Not started | FR-AI-1, FR-AI-11, FR-AI-15, FR-AI-17 | — | — | — |
| UC-AI-3 | The Student practices a client interview with a role-playing client assistant | ❌ Not started | FR-AI-10 | — | — | — |
| UC-AI-4 | The Student turns meeting notes into structured requirements | ❌ Not started | FR-AI-12, FR-AI-9 | — | — | — |
| UC-AI-5 | The Student requests a critique from the critique assistant | ❌ Not started | FR-AI-2, FR-AI-9 | — | — | — |
| UC-AI-6 | The Student asks an assistant to explain a concept (Tutor Mode) | ❌ Not started | FR-AI-4 | — | — | — |
| UC-AI-7 | The Student generates a draft requirement skeleton with an assistant | ❌ Not started | FR-AI-8, FR-AI-20 | — | — | — |
| UC-AI-8 | The Student reviews and accepts or rejects an assistant proposal | ❌ Not started | FR-AI-8 | — | — | — |
| UC-AI-9 | The Student consults the project assistant | ❌ Not started | FR-AI-18, FR-AI-19 | — | — | — |
| UC-AI-10 | The Student requests a whole-project review from the critique assistant | ❌ Not started | FR-AI-21, FR-AI-22, FR-AI-23, FR-AI-9 | — | — | — |

> The FR IDs column lists the **SRS §5.2 cross-cutting subsystems** each use case depends on; `—` marks a self-contained use case whose own steps are its full spec (the common case for CRUD use cases — per the project's model, the use case *is* the functional requirement). The universal behaviors — authorship metadata (`FR-HIS-4`) and RBAC (`FR-SEC-*`) — apply almost everywhere and are omitted per row. When planning a use case, treat its "The system …" steps + Associated Information as the acceptance criteria, and flag any step with no backing behavior.

> **UC-COL-1 note:** section/use-case **locking and unlocking is implemented** — but that scope belongs to UC-DOC-2 and UC-DOC-6 (both ✅), which UC-COL-1 references rather than owns. UC-COL-1's own scope is the real-time collaboration layer (teammate presence, join/leave notification, live broadcast of saved changes and lock state — `FR-COL-1..4`), and **none of that is built**, so UC-COL-1 is ❌.
