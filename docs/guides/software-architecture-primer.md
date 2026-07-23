# Software Architecture & Quality Attributes — A Primer

*A teaching primer for senior-design students. It explains what software architecture really is, why **quality attributes** (not features) drive it, and the small vocabulary — quality scenarios, ASRs, utility trees, architecture decisions — you need to reason about it. Every concept is grounded in a worked example from Project Pulse's own architecture-of-record ([`../design/architectural-design.md`](../design/architectural-design.md)), so you can see the idea and then go read the real artifact that uses it.*

---

## 1. What "software architecture" actually means

"Architecture" is an overloaded word — ask ten engineers and you'll get ten answers. Two senses matter, and you need **both**:

**(a) Architecture as structure.** The big-picture shapes: which components exist, how they're grouped, how they depend on each other, how they're deployed. C4 diagrams (context / container / component) and style choices — *layered*, *pipes-and-filters*, *microservices*, *domain partitioning* — capture this. It's what most people picture when they hear "architecture."

**(b) Architecture as significant decisions.** The choices that are *expensive to change* and that *determine how well the system works*: "use one database, not two"; "authenticate with self-issued tokens, not an external identity provider"; "lock a document section instead of merging concurrent edits." None of these is visible in a component diagram, yet they define the system more than the boxes do.

The test that separates architecture from ordinary design (Grady Booch, Philippe Kruchten): **a decision is architectural if changing it later is costly and its effect is system-wide.** Renaming a variable is design. Switching from one database to two is architecture.

> **Why the second sense matters.** In Project Pulse, the decision that "the JWT signing key is generated fresh at every startup" (decision **KD-4**) single-handedly caps the whole platform at one server instance and forces every user to re-login on each deploy. That fact appears in *no* diagram — but it's one of the most consequential architectural truths about the system. If you believed "architecture = the diagrams," you'd miss it entirely.

**Architecture vs. design, in one line:** architecture is the subset of design decisions that are hard to reverse and felt system-wide. Everything else is design.

---

## 2. Why quality attributes — not features — drive architecture

Here is the central idea, from Bass, Clements & Kazman (*Software Architecture in Practice*):

> **The same features can be built on many different architectures. What separates a *good* architecture from a *bad* one — given both do the job — is how well it meets its quality attributes.**

There are two kinds of requirement:

- **Functional requirements** — *what* the system does: submit a report, run validation, export a document. You satisfy these by assigning responsibilities to components. Almost any reasonable structure can deliver them.
- **Quality attributes** (often called non-functional requirements) — *how well* it does it: secure, fast, available, maintainable, usable. You satisfy these through the *shape* of the architecture — and they conflict with each other, so meeting them is a balancing act, not a checklist.

This is the inversion beginners miss: **you don't derive the architecture from the feature list.** You derive it from the quality attributes and the hard constraints, then let the features populate the resulting structure. Project Pulse's architecture-of-record does exactly this — it defers every functional requirement to the requirements specs and *leads* with a Quality Goals table.

Functionality tells you *what components you need*. Quality attributes tell you *how to arrange them*.

---

## 3. Making a quality attribute testable: the quality scenario

A quality attribute written as "the system shall be modifiable" or "the system shall be fast" is useless — you cannot test it, so you cannot tell whether you achieved it. The fix (from the SEI, used by the arc42 template) is the **quality scenario**: a six-part template that turns a vague wish into a testable situation.

| Part | Question it answers |
|---|---|
| **Source** | Who or what triggers it? |
| **Stimulus** | What happens? |
| **Artifact** | What part of the system is hit? |
| **Environment** | Under what conditions (normal, peak load, degraded)? |
| **Response** | How should the system react? |
| **Response measure** | How do we *measure* success? (a number!) |

**Worked example — Project Pulse `QS-3` (maintainability):**

- *Source:* a contributor · *Stimulus:* adds a new bounded context (a feature) · *Artifact:* the backend codebase · *Environment:* development · *Response:* it's added as a self-contained vertical slice, no edits to existing slices · *Measure:* **zero changes to other packages; delivered in ≤ 2 person-days.**

Now "maintainable" is something you can actually check. Compare the two ways to write the same concern:

- ❌ "The system shall be modifiable." — untestable wish.
- ✅ `MNT-feature-locality`: "adding or modifying one feature shall require no edits to unrelated bounded-context packages." — a dependency-analysis test can verify this.

**That difference — from a wish to a measurable statement — is the single most important skill in this primer.**

---

## 4. Which requirements shape the architecture? ASRs and the utility tree

Your system will have dozens of requirements. Most do **not** shape the architecture (the functional ones are satisfiable many ways). The few that do are the **architecturally significant requirements (ASRs)** — mostly quality attributes, plus a few hard constraints — whose cost of getting wrong is *system-wide*.

How do you find the ASRs among all your quality attributes? You build a **utility tree**: list the quality attributes and rank each on two axes —

- **Importance** — how much does the business/stakeholders care?
- **Difficulty / risk** — how hard or uncertain is it to achieve?

The attributes that score **high on both** are your ASRs. They earn the most design attention because they're both critical *and* not free.

**Worked example — Project Pulse's ASR table** (at the head of the Architecture Decisions section in the architecture-of-record) is precisely a utility tree — seven drivers ranked by importance × difficulty, each reusing an existing requirement ID:

| ASR (driver) | Significance | Drives which decision |
|---|---|---|
| Confidentiality of FERPA-regulated student records (`SEC-authorization`, `CO-ferpa`) | **High × High** | the two-layer authorization; KD-2, KD-4 |
| Maintainability & learnability — students extend the code (`MNT-feature-locality`) | High × Medium | **KD-7** (domain-partitioned vertical slices) |
| Responsive graph & validation at cohort scale (`PER-graph-load`) | Medium × Medium | KD-3 (relational store) |

Notice ASRs are not a new kind of requirement — they're a *lens* that says "these existing quality attributes are the ones that move the architecture."

---

## 5. From ASR to decision: architecture decisions (ADRs / KDs)

Once you know your ASRs, you make the decisions that satisfy them — and you **record each decision with its reasoning** so a future reader (or contributor, or you in six months) understands *why*. The standard format is an **Architecture Decision Record (ADR)**; Project Pulse calls them **KDs** ("key decisions") and writes each as:

- **Context** — the forces, including the *driving ASR*.
- **Decision** — what you chose.
- **Consequences** — what it buys you, and the trade-off you accept.
- **Rejected alternatives** — what you did *not* pick, and why.

**Worked example — `KD-7` (the decision this session added):**

- *Driving ASR:* maintainability & learnability (`MNT-feature-locality`); verified by `QS-3`.
- *Decision:* partition the backend by **domain** — one bounded context per package, each a full vertical slice (entity → repository → service → controller) — and layer *within* each slice.
- *Rejected:* package-by-layer (all controllers together, all services together) — it optimizes for the rare "swap a technical layer" change over the common "change one feature" change.
- *Trade-off:* cross-cutting concerns (auth, auditing, email) must be deliberately centralized so they aren't duplicated in every slice.

**Tactics vs. patterns (a common confusion).** A *tactic* is a single design move that controls one quality-attribute response — e.g. "authorize at a single checkpoint" for security, or "introduce concurrency" for performance. A *pattern* bundles tactics into a reusable structure — e.g. *layered architecture*. You choose tactics and patterns **in service of your ASRs**, never for their own sake. "We used microservices" is not an architecture; "we needed independent deployability (an ASR), so we used microservices" is.

**The whole chain, end to end:**

```
quality attribute  →  (utility tree)  →  ASR  →  architecture decision (ADR/KD)  →  structure  →  verified by a quality scenario's test
```

In Project Pulse, fully worked: `MNT-feature-locality` → ASR "maintainability" → **KD-7** → the DDD package structure → **QS-3** (and, eventually, an automated dependency test).

---

## 6. Requirements and architecture grow together (Twin Peaks)

Beginners assume a waterfall: finish the requirements, *then* design the architecture. Bashar Nuseibeh's **Twin Peaks** model (2001) says they **co-evolve**. Architecture pushes back on requirements: "we already have component X, so requirement Y is cheap — but Z would mean a rewrite, is it really needed?" You refine both peaks in parallel, moving from vague to detailed together.

Project Pulse practices this deliberately: component boundaries for not-yet-built areas are marked **provisional**, and the first real design of an area — done against actual code — is allowed to revise the diagram. Architecture proven by a working slice beats architecture proven by staring at a diagram.

---

## 7. Architecting continuously (agile architecture)

You should not design everything up front (you'll guess wrong), and it will not "emerge" correctly by accident either. Bellomo, Kruchten, Nord & Ozkaya name the middle path:

- **Architectural runway** — build *just enough* structure ahead of the features that will need it. Not more.
- **Reversibility / last responsible moment** — decide the **hard-to-reverse** things early; defer the reversible ones until you must. (In Project Pulse, platform-wide decisions are confirmed up front; a single area's internal design is deferred to when it's built.)
- **Technical debt as a managed artifact** — when you defer or compromise a decision, *record it* and pay it down deliberately. Project Pulse keeps a prioritized Risks & Technical Debt backlog (`TD-1`…`TD-11`, P0→P3) for exactly this.

---

## 8. The through-line — and why this matters in the AI era

Architecture is **not** the diagrams, and it's **not** a menu of patterns. It's the set of **reasoned, quality-attribute-driven decisions** that shape a system whose structure is expensive to change. Choosing which quality attributes matter, scoring the trade-offs between them, and picking tactics to satisfy them — that is *engineering judgment*.

This is exactly what an AI coding agent **cannot** do for you. AI can generate code quickly and even draw the component boxes, but it can't tell you that *maintainability beats independent-scalability* for your student team, or that *FERPA confidentiality is your #1 ASR*. Worse: if you never make a quality attribute explicit, the agent will silently trade it away for whatever is easiest to code. So the arrival of fast code generation **raises** the value of architectural thinking, it doesn't lower it. Your job is the ASRs, the trade-offs, and the decisions; the agent's job is to build *within the guardrails you set*.

---

## Cheat-sheet (the vocabulary)

- **Quality attribute** — a "how well" requirement (secure, fast, maintainable), stated with a testable threshold.
- **Quality scenario** — a six-part testable story (*source · stimulus · artifact · environment · response · measure*) that operationalizes a quality attribute.
- **Architecturally significant requirement (ASR)** — a requirement (usually a quality attribute or a hard constraint) whose cost of getting wrong is system-wide; it *drives* an architectural decision.
- **Utility tree** — quality attributes ranked by *importance × difficulty*; the high-high ones are your ASRs.
- **Architecture decision (ADR / KD)** — a recorded significant decision: context + driving ASR → decision → consequences → rejected alternatives.
- **Tactic vs. pattern** — a tactic is one design move controlling a quality-attribute response; a pattern bundles tactics. Both are chosen to serve ASRs.
- **Twin Peaks** — requirements and architecture co-evolve, not sequentially.
- **Architectural runway / reversibility / technical debt** — build just-enough ahead; decide irreversible things early; track compromises explicitly.

---

## Try it yourself

Pick one quality attribute for your capstone system (e.g. "the app stays responsive as the data grows"). Then:

1. **Write it as a six-part quality scenario**, with a concrete number in the response measure.
2. **Decide whether it's an ASR** — is it *important* **and** *hard/risky*? (Put it on a utility tree with your other quality attributes.)
3. If it is, **draft one architecture decision (ADR)** that addresses it: context + driving attribute → decision → one rejected alternative → the trade-off you accept.
4. **State how you'd test it** — which measure, run how.

Do that for your top 3–5 quality attributes and you have done the core of architecting your system.

---

## Further reading

- Bass, Clements & Kazman — *Software Architecture in Practice* (quality attributes, scenarios, Attribute-Driven Design, utility tree, ATAM).
- Nuseibeh — "Weaving Together Requirements and Architectures" (the Twin Peaks model), *IEEE Computer*, 2001.
- Bellomo, Kruchten, Nord & Ozkaya — on agile architecture and architectural technical debt, 2014.
- **Project Pulse's own architecture-of-record**, [`../design/architectural-design.md`](../design/architectural-design.md) — read the *Quality Goals* table, the *Architecturally significant requirements* table, the *Architecture Decisions* (KD-1…7), and the *Quality Requirements* scenarios as live examples of everything above.
- The development method that produces these documents: [`../methodology.md`](../methodology.md).
