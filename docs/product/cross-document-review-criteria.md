# Cross-Document Review Criteria & Critique-Assistant System Prompt

> The criteria a reviewer (human or the **critique assistant**) applies when reviewing a team's **whole requirement set** — the team's requirement documents together — for gaps, inconsistencies, conflicts, and broken traceability. Part 1 is the methodology (human-readable checklist). Part 2 is a paste-ready **system prompt** that operationalizes it for the assistant. Part 3 is build guidance, consistent with [`ai-implementation-notes.md`](../guides/ai-implementation-notes.md).

## Scope note — this is the *project-wide* review mode

The spec's critique assistant (**UC-AI-5**, FR-AI-2/FR-AI-9) reviews **one authoring destination** (a Document Section or a use case) for clarity/ambiguity/consistency/completeness/testability. The review described here is **broader**: it reads the team's requirement documents together and hunts for *cross-document* problems — drift, conflicts, coverage holes, broken traceability — that are invisible when you look at one section at a time. It is closest in spirit to the elicitation assistant's broad-mode gap analysis (FR-AI-15), but aimed at *critique/consistency* rather than elicitation.

It complements, and does not replace:
- **ReqLint** (deterministic, single-document, rule-based — missing sections, vague verbs, ID format).
- **Per-destination critique** (UC-AI-5 — deep review of one section/use case).

This whole-project mode is now a **distinct use case and FR** (resolved as OI-16): **UC-AI-10** (Request a whole-project review from the critique assistant), realized by **FR-AI-21** (the review behavior) and **FR-AI-22** (include the criteria in context), and reachable both via a "Review whole project" action and via the project assistant (UC-AI-9). The criteria the review applies are **instructor/admin-configured per course section** as the **Cross-Document Review Criteria** (glossary term), authored through **UC-CFG-4**; they must be defined before the review is available (**FR-AI-23**). Part 1 below is the methodology / default criteria set, and Part 2 the critique-assistant system prompt that operationalizes it.

---

## Part 1 — Review criteria (the methodology)

The current core document types and their roles (documents are **views over one shared graph of typed artifacts and links** — they are supposed to agree because they describe the same model):

1. **Glossary** — canonical domain vocabulary.
2. **Vision and Scope** — business objectives (BO-*), risks, assumptions, features, MVP scope.
3. **Use Cases** — behavioral specs; each carries a unique ID (e.g., `UC-1`).
4. **Business Rules** — cross-cutting policies/access rules `BR-*`.
5. **SRS** — architecture, functional requirements `FR-*`, quality attributes, data model. Its **Non-Use Case Functional Requirements** section holds the system-level "shall" statements that complement the use cases.

Review across **seven dimensions**. For each finding, capture *where*, *what*, *why it matters*, and *the fix or the question*.

### D1 — Completeness & gaps (within a document)
- Required sections present **and filled** — flag placeholder/boilerplate/`TBD`/`TODO`/template-example text left in place.
- Every **Vision feature / BO / RI / AS** has downstream realization (a use case and/or an FR). A feature with no UC/FR is a gap.
- **Quality attributes** are specified and measurable (not empty, not someone else's example).
- The **data model** exists and is concrete (entities, fields, relationships) — not an external link or a stub.
- **External interfaces** that the system depends on (LLM, email, SSO, export) are actually described.

### D2 — Coverage & traceability (across documents)
- **Every use case cites FRs that exist**, and (where a traceability matrix exists) has **exactly one** matrix row.
- **Every FR is realized or honored by some use case** — or is justified as system-driven/background. Flag **orphan FRs** (defined, never referenced) — but an FR explicitly marked **_(Deferred — future release)_** is a parked future requirement, **not** an orphan; don't flag it for lacking a use case.
- **The Non-Use Case Functional Requirements complement the use cases; they must not duplicate them.** A use case is itself a high-level functional requirement (its "system shall" steps + associated information are its spec). Flag any non-use-case FR that merely restates a use case's CRUD flow instead of capturing genuinely non-use-case behavior (system-driven, event-driven, global, or background).
- **Every glossary term is used consistently** in docs 2–5; flag **undefined terms** (used as a domain term but never defined) and **orphan terms** (defined, never used).
- **Every cited business rule resolves** to the BR catalog; flag **orphan rules**.
- **Actors** named in use cases are defined in the glossary and used consistently everywhere — including their **scope and role hierarchy** (e.g., the Course Admin is course-scoped and also holds every Instructor capability). Flag an action attributed to **different actors in different docs** (e.g., document provisioning assigned to the Instructor in Vision but the Course Admin in the use case / business rule).

### D3 — Consistency (the same thing said the same way)
- **Terminology:** one defined term per concept — **no synonyms** for an already-defined term. Watch overloaded words (e.g., qualify "Section" as *Course Section* vs *Document Section*).
- **Scope statements agree:** what is "MVP" vs "post-MVP" must be stated the **same way** across glossary, Vision, Use Cases, Business Rules, and SRS. A **deferred capability** should be labeled in **all three** of its homes: a post-MVP note on the Vision feature/BO, an entry in the Vision MVP-exclusion list, and (if it has an FR) the FR marked **_(Deferred — future release)_**. Flag a capability that is committed in one doc but deferred in another (e.g., a Vision feature still reading as in-scope while its FR is deferred).
- **Lists agree:** the same enumeration (e.g., supported export formats, the set of AI assistants, document types) should match wherever it appears.
- **ID schemes** are used consistently, and IDs stay stable as documents evolve.

### D4 — Conflicts & contradictions (documents assert *opposing* things)
- **Rule conflicts** — e.g., a glossary example says "only the author may delete X" while a use case lets any teammate delete X.
- **Taxonomy/enum vs glossary** divergence — an implementation enum that omits or adds types the glossary defines differently.
- **Duplicate ID spaces with different meanings** — e.g., `AS-1` meaning one thing in Vision and another in the SRS.
- **In-scope here / out-of-scope there** — a capability called MVP in one doc and excluded in another.

### D5 — Atomicity, testability & writing quality (ReqLint-style; applies to FR "shall" statements and use cases)
- FRs: use **"shall"**, are **atomic** (one behavior each), and are **testable/measurable**. Flag **vague verbs** (`manage`, `support`, `handle`, `process`, `deal with`), **subjective adjectives** (`fast`, `user-friendly`, `intuitive`, `easy`, `quick`, `appropriate`, `seamless`), and ambiguity.
- Use cases: have a **trigger**, **preconditions/postconditions**, a **numbered main flow** alternating actor action / system response, and **extensions**; each carries a unique ID.

### D6 — ID & structure hygiene
- ID spaces (`FR-*`, `BR-*`, `UC-*`, `BO-*`, …) are **stable and unique**, with **no duplicates** within a category, and **every cross-reference resolves**.
- TOCs, headings, and anchors are internally consistent (deterministic tools own this, but flag obvious drift).

### D7 — Scope discipline (don't cry wolf)
- Distinguish a **genuine gap** from a **deliberate MVP exclusion**. Before reporting "X is missing," check the MVP/scope statements. When it's unclear whether an omission is intentional, **ask** rather than assert.

### The cross-document coupling map (what to check when something changes)
| When this changes… | …re-check |
|---|---|
| a use case (add/rename/remove) | its cited FRs exist; the traceability row; glossary terms used; the Vision feature/actor it realizes |
| an FR | use cases that cite it; the traceability FR column; Vision features; glossary terms it leans on |
| a business rule (`BR-*`) | use cases that cite it; SRS requirements that enforce it |
| a glossary term | every use across docs 2–5 (no synonyms, no dangling refs) |
| a feature / BO / RI / AS | the use cases and FRs that implement it; the glossary if it introduces a term |
| a deferred / post-MVP capability | all three must agree: the Vision post-MVP label, the Vision MVP-exclusion list, and the FR marked *(Deferred — future release)* (if one exists) |

### Output discipline (for every review)
- One finding = **location(s)** + **category (D1–D7)** + **severity** (`blocker` / `major` / `minor`) + **the problem** (one line) + **why it matters** (the rationale — this is the teaching) + **a suggested fix _or_ a clarifying question** when it's a judgment call.
- **Group by category, order by severity.** Separate "I'm confident this is wrong" from "this needs a human decision."
- **Never fabricate** an ID, citation, or requirement. **Never author** replacement content silently — propose, with rationale, for the author to accept.

---

## Part 2 — System prompt for the critique assistant (whole-project review mode)

> Paste this as the assistant's system prompt. The server prepends the course section's **Teaching Context** and this assistant's **Assistant Instructions**, and the context builder supplies the document content (see Part 3).

```
You are the Critique Assistant for RAM, a requirements-authoring tool used by
software-engineering students. You are an experienced, exacting requirements
engineer and educator. In this mode you review a team's ENTIRE requirement set —
the team's requirement documents as views over one shared model — and find
cross-document gaps, inconsistencies, conflicts, and broken traceability.

THE DOCUMENT MODEL
Requirements are atomic artifacts (graph nodes) connected by typed links (edges).
The current core document types and their roles:
- Glossary — canonical domain terms.
- Vision and Scope — objectives (BO-*), risks (RI-*), assumptions (AS-*), features, MVP scope.
- Use Cases — behavioral specs; each has a unique ID (e.g., UC-1).
- Business Rules — cross-cutting policies/access rules (BR-*).
- SRS — architecture, functional requirements (FR-*), quality attributes, data model;
  its Non-Use Case Functional Requirements section holds the system-level "shall" statements.
Because the documents describe the same model, they must agree.

YOUR JOB — review across these dimensions:
1. Completeness — required content present and actually filled (flag TBD/placeholder/
   boilerplate); every Vision feature/BO/RI/AS has a downstream use case and/or FR;
   quality attributes are specified and measurable; the data model is concrete.
2. Coverage & traceability — every use case cites FRs that exist; every FR is realized
   or honored by some use case (flag orphans — but an FR marked "(Deferred — future
   release)" is a parked future requirement, not an orphan); a non-use-case (system-level)
   FR must complement the use cases, not merely restate a use case's flow; every glossary term
   is used consistently (flag undefined and orphan terms); every cited BR resolves;
   actors are consistent in name AND scope (e.g., the Course Admin is course-scoped and
   also an Instructor; flag an action assigned to different actors in different docs).
3. Consistency — one term per concept (no synonyms; watch overloaded words like
   "Section"); MVP vs post-MVP stated the same way everywhere; repeated lists (export
   formats, assistant set, document types) match; ID schemes used consistently.
4. Conflicts — two documents asserting opposing things (a rule vs a use-case step; an
   enum vs the glossary; the same ID meaning two things; in-scope here / out there).
5. Writing quality — FRs use "shall", are atomic and testable; flag vague verbs
   (manage, support, handle, process), subjective adjectives (fast, user-friendly,
   intuitive, easy, appropriate), and ambiguity. Use cases have a trigger,
   pre/postconditions, a numbered flow, and extensions.
6. ID & structure hygiene — IDs stable and unique, no duplicates within a category;
   every cross-reference resolves.
7. Scope discipline — distinguish a real gap from a deliberate MVP exclusion; if you
   can't tell, ASK rather than assert. A deferred capability should be labeled
   consistently in every home (Vision post-MVP note, MVP-exclusion list, and the FR
   marked "(Deferred — future release)"); flag a capability committed in one doc but
   deferred in another.

HARD RULES
- Ground every finding in the provided documents. NEVER invent an ID, a citation, or a
  requirement. If you're inferring, say so and lower your confidence.
- Cite specific locations (document + section/ID) for every finding.
- You may propose a fix, but you do NOT rewrite or author content — the student decides.
  Frame fixes as suggestions, and where a choice is genuinely the team's, ask a question
  instead of asserting an answer.
- Separate findings you are confident are wrong from issues that need a human decision.
- Rationale is the lesson: every finding must explain WHY it weakens the requirements,
  in terms a student learns from — not just WHAT is wrong.
- Be specific and concise. No flattery, no filler. If a document is genuinely clean on a
  dimension, say so briefly rather than inventing problems.

OUTPUT — return JSON only, matching this shape:
{
  "summary": "2-3 sentences: overall state and the most important themes.",
  "findings": [
    {
      "id": "F1",
      "category": "completeness | coverage | consistency | conflict | writing | hygiene | scope",
      "severity": "blocker | major | minor",
      "locations": ["SRS / Quality Attributes", "Use Cases / UC-2"],
      "problem": "One sentence: what is wrong.",
      "rationale": "Why it weakens the requirements / what the student should learn.",
      "suggestion": "A concrete fix, OR null if this needs a decision.",
      "question": "A clarifying question if this is a judgment call, else null",
      "confidence": "high | medium | low"
    }
  ],
  "open_questions": ["Decisions the team must make that you can't resolve from the docs."]
}
Order findings by severity (blocker → minor), then group mentally by category.
```

---

## Part 3 — Implementation notes

Consistent with [`ai-implementation-notes.md`](../guides/ai-implementation-notes.md) (Assistant = system prompt + context builder + one Claude call, server-side, behind the AI proxy):

- **Context assembly is the hard part, not the prompt.** A whole-project review needs the *content* of the team's requirement documents. Don't blindly dump everything — assemble a structured payload: each document's sections/items, the artifact/link graph (for traceability checks), the glossary term list, the BR catalog, the FR list, and the **template definitions** (for required-section checks). For large projects, send a structured coverage map plus the slices most likely to conflict, and let the model request more.
- **Deterministic checks first.** Resolve the mechanical couplings in code before calling the model — does every cited `FR-*`/`BR-*`/`UC-*` exist? every glossary term used? one traceability row per UC? Feed the model the *results* (e.g., "these FRs are orphaned, these terms undefined") so it spends its reasoning on judgment calls (conflicts, drift, scope) rather than rediscovering broken links. This is faster, cheaper, and more reliable.
- **Structured output.** The JSON-findings shape above is unit-testable; use tool/JSON mode.
- **Prompt caching.** The requirement documents (or their coverage map) and the Teaching Context are large and stable within a review session — cache them.
- **Read-only.** Like UC-AI-5, requesting a whole-project review neither locks nor modifies anything. Acting on a finding goes through the normal authoring path (a manual edit, or an accepted rewrite via UC-AI-8) — never a direct write from the assistant.
- **Authority in code.** Enablement (FR-AI-7), the never-author-without-accept gate (FR-AI-3/FR-AI-8), and target validation stay server-side; Project Source Material and student content are untrusted input (prompt-injection surface).
