# **Project Pulse**

# **Use Cases**

# **Version 1.0**

# **Revision History**

| Date | Version | Description | Author |
| ----- | ----- | ----- | ----- |
| \<dd/mmm/yy\> | \<x.x\> | \<details\> | \<name\> |
|  |  |  |  |
|  |  |  |  |
|  |  |  |  |

***Use Case ID and Name***
*Give each use case a unique integer sequence number identifier. State a concise name for the use case that indicates the value the use case would provide to some user. Begin with an action verb, followed by an object.*

***Author and Date Created***
*Enter the name of the person who initially wrote this use case and the date it was written.*

***Primary and Secondary Actors***
*An actor is a person or other entity external to the software system being specified who interacts with the system and performs use cases to accomplish tasks. Different actors often correspond to different user classes, or roles, identified from the customer community that will use the product. Name the primary actor that will be initiating this use case and any other secondary actors who will participate in completing execution of the use case.*

***Trigger***
*Identify the business event, system event, or user action that initiates the use case. This trigger alerts the system that it should begin testing the preconditions for the use case so it can judge whether to proceed with execution.*

***Description***
*Provide a brief description of the reason for and outcome of this use case, or a high-level description of the sequence of actions and the outcome of executing the use case.*

***Preconditions***
*List any activities that must take place, or any conditions that must be true, before the use case can be started. The system must be able to test each precondition. Number each precondition. Example: PRE-1: User's identity has been authenticated.*

***Postconditions***
*Describe the state of the system at the successful conclusion of the use case execution. Label each postcondition in the form POST-X, where X is a sequence number. Example: POST-1: Price of item in the database has been updated with the new value.*

***Main Success Scenario/Normal Flow***
*Provide a description of the user actions and corresponding system responses that will take place during execution of the use case under normal, expected conditions. This dialog sequence will ultimately lead to accomplishing the goal stated in the use case name and description. Show a numbered list of actions performed by the actor, alternating with responses provided by the system. The normal flow is numbered "X.0", where "X" is the use case ID.*

***Extensions:***

- ***Alternative Flows***
  *Document other successful usage scenarios that can take place within this use case. State the alternative flow, and describe any differences in the sequence of steps that take place. Number each alternative flow in the form "X.Y", where "X" is the use case ID and Y is a sequence number for the alternative flow. For example, "5.3" would indicate the third alternative flow for use case number 5. Indicate where each alternative flow would branch off from the normal flow, and if pertinent, where it would rejoin the normal flow.*

- ***Exceptions***
  *Describe any anticipated error conditions that could occur during execution of the use case and how the system is to respond to those conditions. Number each alternative flow in the form "X.Y.EZ", where "X" is the use case ID, Y indicates the normal (0) or alternative (>0) flow during which this exception could take place, "E" indicates an exception, and "Z" is a sequence number for the exceptions. For example "5.0.E2" would indicate the second exception for the normal flow for use case number 5. Indicate where in the normal (or an alternative) flow each exception could occur.*

***Priority***
*Indicate the relative priority of implementing the functionality required to allow this use case to be executed. Use the same priority scheme as that used for the functional requirements.*

***Frequency of Use***
*Estimate the number of times this use case will be performed per some appropriate unit of time. This gives an early indicator of throughput, concurrent usage loads, and transaction capacity.*

***Business Rules***
*List any business rules that influence this use case. Don't include the business rule text here, just its identifier so the reader can find it in another repository when needed.*

***Associated Information***
*Identify any additional requirements, such as quality attributes, for the use case that may need to be addressed during design or implementation. Also list any associated functional requirements that aren't a direct part of the use case flows but which a developer needs to know about. Describe what should happen if the use case execution fails for some unanticipated or systemic reason (e.g., loss of network connectivity, timeout). If the use case results in a durable state change in a database or the outside world, state whether the change is rolled back, completed correctly, partially completed with a known state, or left in an undetermined state as a result of the exception.*

***Assumptions***
*List any assumptions that were made regarding this use case or how it might execute.*

# **Use Case List**

The catalog of use cases begins below, grouped by area. Each area is an unnumbered H2 sub-heading and each use case an H3 carrying an area-prefixed ID (`UC-<AREA>-<n>`). The catalog spans both the foundation and performance-tracking areas — Rubric, Course Section, Team, Student, Instructor, Account, Weekly Activity Report, and Peer Evaluation — and the requirements-authoring areas that follow them (Templates and Provisioning, Glossary, Requirement Documents, Requirement Artifacts, Artifact Links and Tracing, Validation, Collaboration, Review and Submission, Export, AI Configuration, and AI Assistants).

The note below is **not** the catalog. It records scope context only: a few Project Pulse capabilities not yet specified as use cases, and work that is **deferred or on hold**. Anything that has been specified appears as a `UC-<AREA>-<n>` below — not in this note.

**Existing Project Pulse capabilities** (used but not yet specified as use cases):
- The course admin creates a course (course sections are created in the Course Section area below)
- The course admin transfers teams or students between course sections
- The course admin selects built-in templates for a team (hardcoded for the MVP; document creation is specified as UC-TPL-1)

**Deferred or on hold** (not yet specified as use cases):
- Instructor Dashboard, Feedback, and Grading:
  - View course and team dashboard
  - Open a team requirement document
  - Provide inline feedback and comments
  - Grade a document using a rubric
- Template Management (on hold):
  - Create a custom template
  - Customize a requirement template — document section structure / headings / ordering, required vs. optional vs. conditional document sections, per-template validation rules, document-section-level AI prompts, and per-document-section rubrics

## **Rubric**

### **UC-RUB-1: The Course Admin creates a rubric**

**UC ID and Name:** UC-RUB-1: Create a rubric
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:**
**Trigger:** The course admin indicates to create a new rubric.
**Description:** The course admin wants to create a new rubric, so that the students can use it for assessing peer performance effectively.

**Preconditions:**
- PRE-1. The course admin is logged into the system.

**Postconditions:**
- POST-1. The new rubric is stored in the system.

**Main Success Scenario:**
1. The course admin indicates to create a new rubric.
2. The system asks the course admin to enter the details of this new rubric according to the "Details" defined in the Associated Information of this use case.
3. The course admin enters the details of this new rubric and confirms that she has finished.
4. The system validates the course admin's inputs according to the "Details" defined in the Associated Information of this use case.
5. The system displays the details of the new rubric and asks the course admin to confirm the creation.
6. The course admin either confirms the creation (continues the normal flow) or chooses to modify the details (return to step 3).
7. The system saves the new rubric and informs the course admin that this rubric has been created.
8. Use case ends.

**Extensions:**
- **4a. Input validation rule violation:**
  - 4a1. The system alerts the course admin that an input validation rule is violated and displays the nature and location of the error.
  - 4a2. The course admin corrects the mistake and returns to step 4 of the normal flow.

**Priority:** High
**Frequency of Use:** 1 user, 1 usage per year.
**Business Rules:**

**Associated Information:**
- Details:  Rubric name (must be unique): E.g., Peer Eval Rubric v1 Several criteria: each criterion has a name, a description, and a max score (must be positive and can be a decimal number). For example: Criterion 1: Criterion: Quality of work Description: How do you rate the quality of this teammate's work? (1-10)  Max score: 10 Criterion 2: Criterion: Productivity Description: How productive is this teammate? (1-10)  Max score: 10 Criterion 3: Criterion: Initiative Description: How proactive is this teammate? (1-10)  Max score: 10 Criterion 4: Criterion: Courtesy Description: Does this teammate treat others with respect? (1-10)  Max score: 10 Criterion 5: Criterion: Open-mindedness Description: How well does this teammate handle criticism of their work? (1-10)  Max score: 10 Criterion 6: Criterion: Engagement in meetings Description: How is this teammate's performance during meetings? (1-10) Max score: 10 The course admin shall be able to cancel the use case at any time prior to submitting it.

**Assumptions:**
**Open Issues:**

### **UC-RUB-2: The Course Admin finds rubrics**

**UC ID and Name:** UC-RUB-2: Find rubrics
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:**
**Trigger:** The course admin indicates to find rubrics.
**Description:** The course admin wants to find the rubrics defined in her course so that she can review, edit, delete, or assign one.

**Preconditions:**
- PRE-1. The course admin is logged into the system.

**Postconditions:**
- POST-1. A list of matching rubrics in the course admin's course is returned and displayed. It is possible that the list is empty.

**Main Success Scenario:**
1. The course admin indicates to find rubrics.
2. The system asks the course admin to enter search values according to the "Search criteria" defined in the Associated Information of this use case.
3. The course admin enters one or more search values and confirms that she has finished entering.
4. The system finds all rubrics in the course admin's course that match the provided search criteria.
5. The system displays the matching rubrics according to the "Search results display strategy" and the "Sort criteria" defined in the Associated Information of this use case.
6. Use case ends.

**Extensions:**
- **4a. No matching rubrics are found:**
  - 4a1. The system alerts the course admin that no matching rubrics are found.
  - 4a2. The course admin either chooses to UC-RUB-1: Create a rubric, or terminates the use case, or returns to step 2 of the normal flow.

**Priority:** High
**Frequency of Use:** Occasional; mostly at course setup and around evaluation milestones.
**Business Rules:** BR-2 — rubrics are scoped to the course admin's course.

**Associated Information:**

Search criteria:

| Search property name | Data type | Validation rule | Security/access concerns | Reference to glossary |
| ---- | ---- | ---- | ---- | ---- |
| rubric name | String | Optional | Course-scoped to the course admin's course |  |

Search results display strategy (specify which properties to display for each matching rubric): rubric name, criteria

Sort criteria: rubric name in ascending order

**Related Use Cases:** UC-RUB-1: Create a rubric.
**Assumptions:**
**Open Issues:**

### **UC-RUB-3: The Course Admin views a rubric**

**UC ID and Name:** UC-RUB-3: View a rubric
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:**
**Trigger:** The course admin indicates to view the details of a rubric.
**Description:** The course admin wants to view the details of a rubric, including its criteria, so that she can decide what to do next.

**Preconditions:**
- PRE-1. The course admin is logged into the system.
- PRE-2. The rubric belongs to the course admin's course.

**Postconditions:**
- POST-1. The details of the specified rubric are displayed to the course admin.

**Main Success Scenario:**
1. The course admin finds a list of rubrics through UC-RUB-2: Find rubrics.
2. The course admin chooses to view the details of one specific rubric.
3. The system retrieves and displays the details of this rubric according to the "Details" defined in the Associated Information of this use case.
4. Use case ends.

**Extensions:**
**Priority:** High
**Frequency of Use:** Occasional.
**Business Rules:** BR-2 — a course admin may view only rubrics in her own course.

**Associated Information:**

Details:

| Property name | Data type | Editability | Security/access concerns | Reference to glossary |
| ---- | ---- | ---- | ---- | ---- |
| rubric name | String |  |  |  |
| criteria (each: criterion name, description, max score) |  |  |  |  |

**Related Use Cases:** UC-RUB-2: Find rubrics; UC-RUB-4: Edit a rubric.
**Assumptions:**
**Open Issues:**

### **UC-RUB-4: The Course Admin edits a rubric**

**UC ID and Name:** UC-RUB-4: Edit a rubric
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:**
**Trigger:** The course admin indicates to edit a rubric.
**Description:** The course admin wants to edit a rubric (for example, rename it) so that it stays accurate for peer assessment.

**Preconditions:**
- PRE-1. The course admin is logged into the system.
- PRE-2. The rubric belongs to the course admin's course.

**Postconditions:**
- POST-1. The updated rubric is stored in the system.

**Main Success Scenario:**
1. The course admin selects a rubric to edit (found through UC-RUB-2: Find rubrics).
2. The system displays the current details of the rubric for editing according to the "Details" defined in the Associated Information of this use case.
3. The course admin changes the details and confirms that she has finished.
4. The system validates the course admin's inputs.
5. The system saves the updated rubric and informs the course admin that the rubric has been updated.
6. Use case ends.

**Extensions:**
- **4a. Input validation rule violation:**
  - 4a1. The system alerts the course admin that an input validation rule is violated and displays the nature and location of the error.
  - 4a2. The course admin corrects the mistake and returns to step 4 of the normal flow.

**Priority:** Medium
**Frequency of Use:** Rare; rubrics are stable once defined.
**Business Rules:** BR-2 — only a course admin may edit a rubric in her own course. To change a rubric's criteria membership, see UC-RUB-6 and UC-RUB-7.

**Associated Information:**

Details:

| Property name | Data type | Editability | Security/access concerns | Reference to glossary |
| ---- | ---- | ---- | ---- | ---- |
| rubric name | String | Editable; must be unique |  |  |

**Related Use Cases:** UC-RUB-2: Find rubrics; UC-RUB-6: Add a criterion to a rubric; UC-RUB-7: Remove a criterion from a rubric.
**Assumptions:**
**Open Issues:**

### **UC-RUB-5: The Course Admin deletes a rubric**

**UC ID and Name:** UC-RUB-5: Delete a rubric
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:**
**Trigger:** The course admin indicates to delete a rubric.
**Description:** The course admin wants to delete a rubric that is no longer needed.

**Preconditions:**
- PRE-1. The course admin is logged into the system.
- PRE-2. The rubric belongs to the course admin's course.

**Postconditions:**
- POST-1. The rubric is removed from the system.

**Main Success Scenario:**
1. The course admin selects a rubric to delete (found through UC-RUB-2: Find rubrics).
2. The system asks the course admin to confirm the deletion.
3. The course admin confirms the deletion.
4. The system deletes the rubric and informs the course admin that the rubric has been deleted.
5. Use case ends.

**Extensions:**
- **3a. The course admin cancels the deletion:**
  - 3a1. The system keeps the rubric and the use case ends.

**Priority:** Medium
**Frequency of Use:** Rare.
**Business Rules:** BR-2 — only a course admin may delete a rubric in her own course.

**Associated Information:**

**Related Use Cases:** UC-RUB-2: Find rubrics.
**Assumptions:**
**Open Issues:**

### **UC-RUB-6: The Course Admin adds a criterion to a rubric**

**UC ID and Name:** UC-RUB-6: Add a criterion to a rubric
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:**
**Trigger:** The course admin indicates to add a criterion to a rubric.
**Description:** The course admin wants to add an existing criterion to a rubric so that the rubric assesses that dimension of peer performance.

**Preconditions:**
- PRE-1. The course admin is logged into the system.
- PRE-2. Both the rubric and the criterion belong to the course admin's course.

**Postconditions:**
- POST-1. The criterion is part of the rubric's criteria.

**Main Success Scenario:**
1. The course admin selects a rubric (found through UC-RUB-2: Find rubrics).
2. The course admin selects a criterion to add (found through UC-RUB-9: Find criteria).
3. The system adds the criterion to the rubric and confirms that the criterion has been added.
4. Use case ends.

**Extensions:**
- **3a. The criterion is already part of the rubric:**
  - 3a1. The system leaves the rubric unchanged and informs the course admin; the use case ends.

**Priority:** Medium
**Frequency of Use:** At rubric setup and occasional revision.
**Business Rules:** BR-2 — the rubric and criterion must be in the course admin's own course.

**Associated Information:**

**Related Use Cases:** UC-RUB-7: Remove a criterion from a rubric; UC-RUB-9: Find criteria; UC-RUB-11: Create a criterion.
**Assumptions:**
**Open Issues:**

### **UC-RUB-7: The Course Admin removes a criterion from a rubric**

**UC ID and Name:** UC-RUB-7: Remove a criterion from a rubric
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:**
**Trigger:** The course admin indicates to remove a criterion from a rubric.
**Description:** The course admin wants to remove a criterion from a rubric so that the rubric no longer assesses that dimension.

**Preconditions:**
- PRE-1. The course admin is logged into the system.
- PRE-2. The rubric belongs to the course admin's course and currently includes the criterion.

**Postconditions:**
- POST-1. The criterion is no longer part of the rubric's criteria. The criterion itself continues to exist in the course.

**Main Success Scenario:**
1. The course admin selects a rubric (found through UC-RUB-2: Find rubrics).
2. The course admin selects one of the rubric's criteria to remove.
3. The system removes the criterion from the rubric and confirms that the criterion has been removed.
4. Use case ends.

**Extensions:**
**Priority:** Medium
**Frequency of Use:** At rubric setup and occasional revision.
**Business Rules:** BR-2 — only a course admin may modify a rubric in her own course.

**Associated Information:**

**Related Use Cases:** UC-RUB-6: Add a criterion to a rubric.
**Assumptions:**
**Open Issues:**

### **UC-RUB-8: The Course Admin assigns a rubric to a course section**

**UC ID and Name:** UC-RUB-8: Assign a rubric to a course section
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:**
**Trigger:** The course admin indicates to assign a rubric to a course section.
**Description:** The course admin wants to set the rubric a course section uses so that the section's students evaluate their peers with that rubric.

**Preconditions:**
- PRE-1. The course admin is logged into the system.
- PRE-2. Both the course section and the rubric belong to the course admin's course.

**Postconditions:**
- POST-1. The course section's rubric is set to the chosen rubric; peer evaluations in that course section use it.

**Main Success Scenario:**
1. The course admin selects a course section (found through UC-SEC-1: Find course sections).
2. The course admin selects a rubric to assign (found through UC-RUB-2: Find rubrics).
3. The system assigns the rubric to the course section and confirms that the rubric has been assigned.
4. Use case ends.

**Extensions:**
**Priority:** High
**Frequency of Use:** Once per course section at setup; rarely changed.
**Business Rules:** BR-2 — the course section and rubric must be in the course admin's own course.

**Associated Information:**
- A course section uses one rubric at a time; assigning a new rubric replaces the previous assignment. The currently assigned rubric is shown when viewing the course section (UC-SEC-2).

**Related Use Cases:** UC-SEC-1: Find course sections; UC-SEC-2: View a course section; UC-RUB-2: Find rubrics.
**Assumptions:**
**Open Issues:**

### **UC-RUB-9: The Course Admin finds criteria**

**UC ID and Name:** UC-RUB-9: Find criteria
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:**
**Trigger:** The course admin indicates to find criteria.
**Description:** The course admin wants to find the peer evaluation criteria defined in her course so that she can review, edit, delete, or add one to a rubric.

**Preconditions:**
- PRE-1. The course admin is logged into the system.

**Postconditions:**
- POST-1. A list of matching criteria in the course admin's course is returned and displayed. It is possible that the list is empty.

**Main Success Scenario:**
1. The course admin indicates to find criteria.
2. The system asks the course admin to enter search values according to the "Search criteria" defined in the Associated Information of this use case.
3. The course admin enters one or more search values and confirms that she has finished entering.
4. The system finds all criteria in the course admin's course that match the provided search criteria.
5. The system displays the matching criteria according to the "Search results display strategy" and the "Sort criteria" defined in the Associated Information of this use case.
6. Use case ends.

**Extensions:**
- **4a. No matching criteria are found:**
  - 4a1. The system alerts the course admin that no matching criteria are found.
  - 4a2. The course admin either chooses to UC-RUB-11: Create a criterion, or terminates the use case, or returns to step 2 of the normal flow.

**Priority:** High
**Frequency of Use:** Occasional; mostly at course setup and rubric revision.
**Business Rules:** BR-2 — criteria are scoped to the course admin's course.

**Associated Information:**

Search criteria:

| Search property name | Data type | Validation rule | Security/access concerns | Reference to glossary |
| ---- | ---- | ---- | ---- | ---- |
| criterion name | String | Optional | Course-scoped to the course admin's course |  |

Search results display strategy (specify which properties to display for each matching criterion): criterion name, description, max score

Sort criteria: criterion name in ascending order

**Related Use Cases:** UC-RUB-11: Create a criterion.
**Assumptions:**
**Open Issues:**

### **UC-RUB-10: The Course Admin views a criterion**

**UC ID and Name:** UC-RUB-10: View a criterion
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:**
**Trigger:** The course admin indicates to view the details of a criterion.
**Description:** The course admin wants to view the details of a peer evaluation criterion so that she can decide what to do next.

**Preconditions:**
- PRE-1. The course admin is logged into the system.
- PRE-2. The criterion belongs to the course admin's course.

**Postconditions:**
- POST-1. The details of the specified criterion are displayed to the course admin.

**Main Success Scenario:**
1. The course admin finds a list of criteria through UC-RUB-9: Find criteria.
2. The course admin chooses to view the details of one specific criterion.
3. The system retrieves and displays the details of this criterion according to the "Details" defined in the Associated Information of this use case.
4. Use case ends.

**Extensions:**
**Priority:** Medium
**Frequency of Use:** Occasional.
**Business Rules:** BR-2 — a course admin may view only criteria in her own course.

**Associated Information:**

Details:

| Property name | Data type | Editability | Security/access concerns | Reference to glossary |
| ---- | ---- | ---- | ---- | ---- |
| criterion name | String |  |  |  |
| description | String |  |  |  |
| max score | Decimal |  |  |  |

**Related Use Cases:** UC-RUB-9: Find criteria; UC-RUB-12: Edit a criterion.
**Assumptions:**
**Open Issues:**

### **UC-RUB-11: The Course Admin creates a criterion**

**UC ID and Name:** UC-RUB-11: Create a criterion
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:**
**Trigger:** The course admin indicates to create a new criterion.
**Description:** The course admin wants to create a new peer evaluation criterion so that it can be added to one or more rubrics.

**Preconditions:**
- PRE-1. The course admin is logged into the system.

**Postconditions:**
- POST-1. The new criterion is stored in the course admin's course.

**Main Success Scenario:**
1. The course admin indicates to create a new criterion.
2. The system asks the course admin to enter the details of this new criterion according to the "Details" defined in the Associated Information of this use case.
3. The course admin enters the details and confirms that she has finished.
4. The system validates the course admin's inputs.
5. The system saves the new criterion and informs the course admin that the criterion has been created.
6. Use case ends.

**Extensions:**
- **4a. Input validation rule violation:**
  - 4a1. The system alerts the course admin that an input validation rule is violated and displays the nature and location of the error.
  - 4a2. The course admin corrects the mistake and returns to step 4 of the normal flow.

**Priority:** High
**Frequency of Use:** Occasional; mostly at course setup.
**Business Rules:** BR-2 — criteria are created within the course admin's own course.

**Associated Information:**

Details:

| Property name | Data type | Validation rule | Security/access concerns | Reference to glossary |
| ---- | ---- | ---- | ---- | ---- |
| criterion name | String | Required |  |  |
| description | String | Required |  |  |
| max score | Decimal | Required; must be positive (may be a decimal) |  |  |

**Related Use Cases:** UC-RUB-6: Add a criterion to a rubric.
**Assumptions:**
**Open Issues:**

### **UC-RUB-12: The Course Admin edits a criterion**

**UC ID and Name:** UC-RUB-12: Edit a criterion
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:**
**Trigger:** The course admin indicates to edit a criterion.
**Description:** The course admin wants to edit a peer evaluation criterion (its name, description, or max score) so that it stays accurate.

**Preconditions:**
- PRE-1. The course admin is logged into the system.
- PRE-2. The criterion belongs to the course admin's course.

**Postconditions:**
- POST-1. The updated criterion is stored in the system.

**Main Success Scenario:**
1. The course admin selects a criterion to edit (found through UC-RUB-9: Find criteria).
2. The system displays the current details of the criterion for editing.
3. The course admin changes the details and confirms that she has finished.
4. The system validates the course admin's inputs.
5. The system saves the updated criterion and informs the course admin that the criterion has been updated.
6. Use case ends.

**Extensions:**
- **4a. Input validation rule violation:**
  - 4a1. The system alerts the course admin that an input validation rule is violated and displays the nature and location of the error.
  - 4a2. The course admin corrects the mistake and returns to step 4 of the normal flow.

**Priority:** Medium
**Frequency of Use:** Rare.
**Business Rules:** BR-2 — only a course admin may edit a criterion in her own course.

**Associated Information:**

Details:

| Property name | Data type | Editability | Security/access concerns | Reference to glossary |
| ---- | ---- | ---- | ---- | ---- |
| criterion name | String | Editable |  |  |
| description | String | Editable |  |  |
| max score | Decimal | Editable; must be positive |  |  |

**Related Use Cases:** UC-RUB-9: Find criteria.
**Assumptions:**
**Open Issues:**

### **UC-RUB-13: The Course Admin deletes a criterion**

**UC ID and Name:** UC-RUB-13: Delete a criterion
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:**
**Trigger:** The course admin indicates to delete a criterion.
**Description:** The course admin wants to delete a peer evaluation criterion that is no longer needed.

**Preconditions:**
- PRE-1. The course admin is logged into the system.
- PRE-2. The criterion belongs to the course admin's course.

**Postconditions:**
- POST-1. The criterion is removed from the system.

**Main Success Scenario:**
1. The course admin selects a criterion to delete (found through UC-RUB-9: Find criteria).
2. The system asks the course admin to confirm the deletion.
3. The course admin confirms the deletion.
4. The system deletes the criterion and informs the course admin that the criterion has been deleted.
5. Use case ends.

**Extensions:**
- **3a. The course admin cancels the deletion:**
  - 3a1. The system keeps the criterion and the use case ends.

**Priority:** Medium
**Frequency of Use:** Rare.
**Business Rules:** BR-2 — only a course admin may delete a criterion in her own course.

**Associated Information:**

**Related Use Cases:** UC-RUB-9: Find criteria; UC-RUB-7: Remove a criterion from a rubric.
**Assumptions:**
**Open Issues:**

## **Course Section**

### **UC-SEC-1: The Course Admin finds course sections**

**UC ID and Name:** UC-SEC-1: Find course sections
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:**
**Trigger:** The course admin indicates to find course sections.
**Description:** The course admin wants to find course sections which match specific criteria, so that she can decide what to do next.

**Preconditions:**
- PRE-1. The course admin is logged into the system.

**Postconditions:**
- POST-1. A list of matching course sections is returned and displayed to the course admin. It is possible that the list is empty.

**Main Success Scenario:**
1. The course admin indicates to find course sections.
2. The system asks the course admin to enter search values according to the "Search criteria" defined in the Associated Information of this use case.
3. The course admin enters one or more search values and confirms that she has finished entering.
4. The system finds all course sections that match the provided search criteria values.
5. The system displays the matching course sections according to the "Search results display strategy" and the "Sort criteria" defined in the Associated Information of this use case.
6. Use case ends.

**Extensions:**
- **4a. No matching course sections are found:**
  - 4a1. The system alerts the course admin that no matching course sections are found.
  - 4a2. The course admin either chooses to UC-SEC-3: Create a course section or chooses to terminate the use case or chooses to return to step 2 of the normal flow.

**Priority:** High
**Frequency of Use:** 1 user, average of 5 usages per week.
**Business Rules:**

**Associated Information:**

Search criteria (aka search fields, search attributes/properties, search details, searchable qualities):

| Search property name | Data type | Validation rule | Security/access concerns | Reference to glossary |
| ---- | ---- | ---- | ---- | ---- |
| course section name | String | Optional |  |  |

Search results display strategy (specify which properties to display for each matching course section): course section name, team names

Sort criteria: course section name in descending order, team names are in ascending order

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

### **UC-SEC-2: The Course Admin views a course section**

**UC ID and Name:** UC-SEC-2: View a course section
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:**
**Trigger:** The course admin indicates to view the details of a course section.
**Description:** The course admin wants to view the details of a course section, so that she can get a better idea of the course section.

**Preconditions:**
- PRE-1. The course admin is logged into the system.

**Postconditions:**
- POST-1. The details of the specified course section are displayed to the course admin.

**Main Success Scenario:**
1. The course admin indicates to view the details of a course section.
2. The course admin finds a list of course sections through UC-SEC-1: Find course sections.
3. The course admin views the list and chooses to view the details of one specific course section.
4. The system retrieves and displays the details of this course section according to the "Details" defined in the Associated Information and the "Security/access concerns" defined in the Business Rules of this use case.
5. The course admin views the details of this course section.
6. Use case ends.

**Extensions:**
**Priority:** High
**Frequency of Use:** 1 user, average of 5 usages per week.
**Business Rules:**

**Associated Information:**

Details:

| Property name | Data type | Editability | Security/access concerns | Reference to glossary |
| ---- | ---- | ---- | ---- | ---- |
| course section name | String |  |  |  |
| startDate | String |  |  |  |
| endDate | String |  |  |  |
| Teams, team members, and instructors |  |  |  |  |
| instructors not assigned to a team |  |  |  |  |
| students not assigned to a team |  |  |  |  |
| Rubric used |  |  |  |  |

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

### **UC-SEC-3: The Course Admin creates a course section**

**UC ID and Name:** UC-SEC-3: Create a course section
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:**
**Trigger:** The course admin indicates to create a new course section.
**Description:** The course admin wants to create a new course section, so that she can invite students to join the course section.

**Preconditions:**
- PRE-1. The course admin is logged into the system.

**Postconditions:**
- POST-1. The new course section is stored in the system.

**Main Success Scenario:**
1. The course admin indicates to create a new course section.
2. The system asks the course admin to enter the details of this new course section according to the "Details" defined in the Associated Information of this use case.
3. The course admin enters the details of this new course section and confirms that she has finished.
4. The system asks the course admin to choose a rubric for the peer evaluation.
5. The course admin selects an existing rubric.
6. The system displays the criteria of the rubric.
7. The course admin confirms the usage of this rubric.
8. The system validates the course admin's inputs according to the "Details" defined in the Associated Information of this use case.
9. The system validates that the creation of the new course section will not duplicate any existing course section according to the "Duplication detection rules" defined in the Associated Information of this use case.
10. The system displays the details of the new course section and asks the course admin to confirm the creation.
11. The course admin either confirms the creation (continues the normal flow) or chooses to modify the details (return to step 3).
12. The system saves the new course section and informs the course admin that this course section has been created.
13. Use case ends.

**Extensions:**
- **5a. Rubric does not exist or the course admin wants to create a new rubric:**
  - 5a1. The course admin chooses to UC-RUB-1: Create a rubric.
  - 5a2. Returns to step 6 of the normal flow.
- **7a. The course admin indicates to edit some criteria in the rubric:**
  - 7a1. The course admin edits the name, description, and the max score of a criterion. See the Associated Information for more information.
  - 7a2. The system saves the changes.
  - 7a3. Returns to step 6 of the normal flow.
- **8a. Input validation rule violation:**
  - 8a1. The system alerts the course admin that an input validation rule is violated and displays the nature and location of the error.
  - 8a2. The course admin corrects the mistake and returns to step 8 of the normal flow.
- **9a. The system finds possible duplicates from the existing course sections:**
  - 9a1. The system alerts the course admin that the course section she is trying to create already exists in the system.
  - 9a2. The course admin either chooses to correct the mistake and return to step 8 of the normal flow or chooses to terminate the use case.

**Priority:** High
**Frequency of Use:** 1 user, 1 usage per year.
**Business Rules:**

**Associated Information:**
- Details: course section name: E.g., Section 2023-2024 Start and end date of the course section: E.g., 08/21/2023 and 05/01/2024 Editing a rubric: When the course admin edits an existing rubric, behind the scenes, the system shall first duplicate the existing rubric and then let the course admin edit it. In other words, a new rubric is created. Duplication detection rules: course section name is used as the unique identifier for a course section. The course admin shall be able to cancel the use case at any time prior to submitting it.

**Assumptions:**
**Open Issues:**

### **UC-SEC-4: The Course Admin edits a course section**

**UC ID and Name:** UC-SEC-4: Edit a course section
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:**
**Trigger:** The course admin indicates to change the details of an existing course section.
**Description:** The course admin wants to change the details of an existing course section, so that the course section information is correct and up-to-date.

**Preconditions:**
- PRE-1. The course admin is logged into the system.

**Postconditions:**
- POST-1. Changes made to the course section are stored in the system.

**Main Success Scenario:**
1. The course admin indicates to change the details of an existing course section.
2. The course admin views the details of this course section through UC-SEC-2: View a course section.
3. The course admin chooses to change the details of this course section.
4. The system asks the course admin to make changes to this course section where allowed according to the "Details" defined in the Associated Information and the "Security/access concerns" defined in the Business Rules of this use case.
5. The course admin makes changes to this course section until she confirms that she has finished changing.
6. The system validates the course admin's changes and alerts warning messages according to the "Details" defined in the Associated Information of this use case.
7. The course admin acknowledges the warnings and chooses to continue.
8. The system displays the updated details of this course section and alerts the course admin to confirm the change.
9. The course admin either confirms the change (continues the normal flow) or chooses to continue to change the details (return to step 5).
10. The system saves the changes, carries out the effect of change according to the "Details" defined in the Associated Information of this use case, and informs the course admin that this course section has been changed.
11. Use case ends.

**Extensions:**
- **6a. Input validation rule violation:**
  - 6a1. The system alerts the course admin that an input validation rule is violated and displays the nature and location of the error.
  - 6a2. The course admin corrects the mistake and returns to step 6 of the normal flow.

**Priority:** High
**Frequency of Use:** 1 user, average of 1 usage per year.
**Business Rules:**

**Associated Information:**

Details:

| Property name | Data type | Editability | Validation rule | Effect of change | Warning | Reference to glossary |
| ---- | ---- | ---- | ---- | ---- | ---- | ---- |
| course section name | String | Yes |  |  |  |  |
| Start and end date of the course section |  | Yes |  |  |  |  |
| Rubric used |  | Yes |  |  |  |  |

The course admin shall be able to cancel the use case at any time prior to submitting it.

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

### **UC-SEC-5: The Course Admin sets up active weeks for a course section**

**UC ID and Name:** UC-SEC-5: Set up active weeks for a course section
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:**
**Trigger:** The course admin indicates to set up active weeks for a course section.
**Description:** The course admin wants to set up weeks for a course section, so that the senior design students know in which weeks they need to submit WARs and peer evaluations.

**Preconditions:**
- PRE-1. The course admin is logged into the system.
- PRE-2. At least one course section is created.

**Postconditions:**
- POST-1. The active weeks for a course section are stored in the system.

**Main Success Scenario:**
1. The course admin indicates to set up weeks for a course section.
2. The system displays all the weeks of this course section according to the start and end date of the course section.
3. The course admin specifies the weeks during which the students do not need to submit WARs and peer evaluations, and confirms that she has finished.
4. The system displays the active weeks of the course section and asks the course admin to confirm the setup.
5. The course admin either confirms the setup (continues the normal flow) or chooses to modify the details (return to step 3).
6. The system saves the active weeks for this course section and informs the course admin that this setup has been done.
7. Use case ends.

**Extensions:**
**Priority:** High
**Frequency of Use:** 1 user, 1 usage per year.
**Business Rules:** BR-22

**Associated Information:**
- The course admin shall be able to cancel the use case at any time prior to submitting it.

**Assumptions:**
**Open Issues:**

## **Team**

### **UC-TEA-1: The Course Admin/Instructor finds teams**

**UC ID and Name:** UC-TEA-1: Find teams
**Created By:**
**Date Created:**
**Primary Actor:** course admin, instructor
**Secondary Actors:**
**Trigger:** The user indicates to find teams.
**Description:** The user wants to find teams which match specific criteria, so that she can decide what to do next.

**Preconditions:**
- PRE-1. The user is logged into the system.

**Postconditions:**
- POST-1. A list of matching teams is returned and displayed to the user. It is possible that the list is empty.

**Main Success Scenario:**
1. The user indicates to find teams.
2. The system asks the user to enter search values according to the "Search criteria" defined in the Associated Information of this use case.
3. The user enters one or more search values and confirms that she has finished entering.
4. The system finds all teams that match the provided search criteria values.
5. The system displays the matching teams according to the "Search results display strategy" and the "Sort criteria" defined in the Associated Information of this use case.
6. Use case ends.

**Extensions:**
- **4a. No matching teams are found:**
  - 4a1. The system alerts the user that no matching teams are found.
  - 4a2. The user either chooses to UC-TEA-3: Create a team or chooses to terminate the use case or chooses to return to step 2 of the normal flow.

**Priority:** High
**Frequency of Use:** 2 users, average of 2 usages per week.
**Business Rules:**

**Associated Information:**

Search criteria (aka search fields, search attributes/properties, search details, searchable qualities):

| Search property name | Data type | Validation rule | Default value | Reference to glossary |
| ---- | ---- | ---- | ---- | ---- |
| course section ID |  |  |  |  |
| course section name | String | Optional |  |  |
| Team name | String | Optional |  |  |
| instructor |  | Optional |  |  |

Search results display strategy (specify which properties to display for each matching team): Team name, Team description, Team website URL, Team members, instructors

Sort criteria: First, section name in descending order. Then, team name in ascending order.

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

### **UC-TEA-2: The Course Admin/Instructor views a team**

**UC ID and Name:** UC-TEA-2: View a team
**Created By:**
**Date Created:**
**Primary Actor:** course admin, instructor
**Secondary Actors:**
**Trigger:** The user indicates to view the details of a team.
**Description:** The user wants to view the details of a team, so that she can get a better idea of the team.

**Preconditions:**
- PRE-1. The user is logged into the system.

**Postconditions:**
- POST-1. The details of the specified team are displayed to the user.

**Main Success Scenario:**
1. The user indicates to view the details of a team.
2. The user finds a list of teams through UC-TEA-1: Find teams.
3. The user views the list and chooses to view the details of one specific team.
4. The system retrieves and displays the details of this team according to the "Details" defined in the Associated Information and the "Security/access concerns" defined in the Business Rules of this use case.
5. The user views the details of this team.
6. Use case ends.

**Extensions:**
**Priority:** High
**Frequency of Use:** 2 users, average of 2 usages per week.
**Business Rules:**

**Associated Information:**

Details:

| Property name | Data type | Editability | Security/access concerns | Reference to glossary |
| ---- | ---- | ---- | ---- | ---- |
| Team name |  |  |  |  |
| Team description |  |  |  |  |
| Team website URL |  |  |  |  |
| Team members |  |  |  |  |
| instructors |  |  |  |  |

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

### **UC-TEA-3: The Course Admin creates a team**

**UC ID and Name:** UC-TEA-3: Create a team
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:**
**Trigger:** The course admin indicates to create a new team.
**Description:** The course admin wants to create a new team, so that students can be assigned to it.

**Preconditions:**
- PRE-1. The course admin is logged into the system.

**Postconditions:**
- POST-1. The new team is stored in the system.

**Main Success Scenario:**
1. The course admin indicates to create a new team for a course section.
2. The system asks the course admin to enter the details of this new team according to the "Details" defined in the Associated Information of this use case.
3. The course admin enters the details of this new team and confirms that she has finished.
4. The system validates the course admin's inputs according to the "Details" defined in the Associated Information of this use case.
5. The system validates that the creation of the new team will not duplicate any existing team according to the "Duplication detection rules" defined in the Associated Information of this use case.
6. The system displays the details of the new team and asks the course admin to confirm the creation.
7. The course admin either confirms the creation (continues the normal flow) or chooses to modify the details (return to step 3).
8. The system saves the new team and informs the course admin that this team has been created.
9. Use case ends.

**Extensions:**
- **4a. Input validation rule violation:**
  - 4a1. The system alerts the course admin that an input validation rule is violated and displays the nature and location of the error.
  - 4a2. The course admin corrects the mistake and returns to step 4 of the normal flow.
- **5a. The system finds possible duplicates from the existing teams:**
  - 5a1. The system alerts the course admin that the team she is trying to create already exists in the system.
  - 5a2. The course admin either chooses to correct the mistake and return to step 4 of the normal flow or chooses to terminate the use case.

**Priority:** High
**Frequency of Use:** 1 user, 5-10 usages per year.
**Business Rules:**

**Associated Information:**
- Details: Senior design team name: E.g., Peer Evaluation Tool team Team description Team website URL Duplication detection rules: Team name must be unique The course admin shall be able to cancel the use case at any time prior to submitting it.

**Assumptions:**
**Open Issues:**

### **UC-TEA-4: The Course Admin edits a team**

**UC ID and Name:** UC-TEA-4: Edit a team
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:**
**Trigger:** The course admin indicates to change the details of an existing team.
**Description:** The course admin wants to change the name of an existing team.

**Preconditions:**
- PRE-1. The course admin is logged into the system.

**Postconditions:**
- POST-1. Changes made to the team are stored in the system.

**Main Success Scenario:**
1. The course admin indicates to change the details of an existing team.
2. The course admin views the details of this team through UC-TEA-2: View a team.
3. The course admin chooses to change the details of this team.
4. The system asks the course admin to make changes to this team where allowed according to the "Details" defined in the Associated Information and the "Security/access concerns" defined in the Business Rules of this use case.
5. The course admin makes changes to this team until she confirms that she has finished changing.
6. The system validates the course admin's changes and alerts warning messages according to the "Details" defined in the Associated Information of this use case.
7. The course admin acknowledges the warnings and chooses to continue.
8. The system displays the updated details of this team and alerts the course admin to confirm the change.
9. The course admin either confirms the change (continues the normal flow) or chooses to continue to change the details (return to step 5).
10. The system saves the changes, carries out the effect of change according to the "Details" defined in the Associated Information of this use case, and informs the course admin that this team has been changed.
11. Use case ends.

**Extensions:**
- **6a. Team name conflict:**
  - 6a1. The system alerts the course admin that the team name has been used.
  - 6a2. The course admin corrects the mistake and returns to step 6 of the normal flow.

**Priority:** High
**Frequency of Use:** 1 user, average of 6 usages per year.
**Business Rules:**

**Associated Information:**

Details:

| Property name | Data type | Editability | Validation rule | Effect of change | Warning | Reference to glossary |
| ---- | ---- | ---- | ---- | ---- | ---- | ---- |
| Team name | String | Yes |  |  |  |  |
| Team description |  | Yes |  |  |  |  |
| Team website URL |  | Yes |  |  |  |  |

No two teams can have the same name. The team name must be unique. The course admin shall be able to cancel the use case at any time prior to submitting it.

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

### **UC-TEA-5: The Course Admin assigns students to teams**

**UC ID and Name:** UC-TEA-5: Assign students to teams
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:** student
**Trigger:** The course admin indicates to assign students to teams.
**Description:** The course admin wants to assign students to teams, so that students can start to submit WARs and evaluate teammates every week.

**Preconditions:**
- PRE-1. Teams are created.
- PRE-2. Students have set up their accounts.
- PRE-3. The course admin is logged into the system.

**Postconditions:**
- POST-1. Every student is associated with one team.

**Main Success Scenario:**
1. The course admin indicates to assign students to teams.
2. The system displays a list of teams and a list of students.
3. The course admin selects a team and assigns a group of students to it.
4. The course admin repeats this step until she confirms that she has finished assigning students to all the teams.
5. The system displays the team assignment information and asks the course admin to confirm the assignment.
6. The course admin confirms the assignment.
7. The system notifies relevant actors about the assignment according to the "Notification" defined in the Associated Information of this use case.
8. Use case ends.

**Extensions:**
- **4a. The course admin finds a wrong team assignment:**
  - 4a1. The course admin removes a student from a team, reassign her to a new team, and returns to step 4 of the normal flow.

**Priority:** High
**Frequency of Use:** 1 user, 1 usage per year.
**Business Rules:**

**Associated Information:**
- Notification: The system notifies the students about their team assignment. The course admin shall be able to cancel the process at any time prior to submitting it.

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

### **UC-TEA-6: The Course Admin removes a student from a team**

**UC ID and Name:** UC-TEA-6: Remove a student from a team
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:** student
**Trigger:** The course admin indicates to remove a student from a team.
**Description:** The course admin wants to remove a student from a team, so that this student can be assigned to a new team.

**Preconditions:**
- PRE-1. Teams are created.
- PRE-2. Students have set up their accounts.
- PRE-3. The course admin is logged into the system.
- PRE-4. Students have been assigned to teams.

**Postconditions:**
- POST-1. The student is removed from a team.

**Main Success Scenario:**
1. The course admin indicates to remove a student from a team.
2. The course admin views the details of the team through UC-TEA-2: View a team.
3. The course admin removes a student from this team.
4. The system displays the new team assignments and asks the course admin to confirm the removal. The course admin confirms the removal. The system notifies relevant actors about the assignment according to the "Notification" defined in the Associated Information of this use case.
5. Use case ends.

**Extensions:**
- **5a. The course admin finds a wrong team member removal:**
  - 5a1. The course admin corrects the wrong removal and returns to step 4 of the normal flow.

**Priority:** Low
**Frequency of Use:** Rare. 1 user, 1 usage per year.
**Business Rules:**

**Associated Information:**
- Notification: The system notifies the student about her team removal. The course admin shall be able to cancel the process at any time prior to submitting it.

**Related Use Cases:** The course admin may immediately assign the student to a new team.
**Assumptions:**
**Open Issues:**

### **UC-TEA-7: The Course Admin deletes a team**

**UC ID and Name:** UC-TEA-7: Delete a team
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:** student, instructor
**Trigger:** The course admin indicates to delete an existing team.
**Description:** The course admin wants to delete an existing team.

**Preconditions:**
- PRE-1. The course admin is logged into the system.
- PRE-2. There exists at least one team.

**Postconditions:**
- POST-1. The team is deleted from the system according to the "Deletion strategy" defined in the Associated Information of this use case.

**Main Success Scenario:**
1. The course admin indicates to delete an existing team.
2. The course admin views the details of this team through UC-TEA-2: View a team.
3. The course admin chooses to delete this team.
4. The system alerts the course admin of the consequences of this deletion according to the "Data integrity and deletion rules" defined in the Associated Information of this use case, warns the course admin about the deletion, and asks the course admin to confirm.
5. The course admin confirms the deletion.
6. The system deletes the team according to the "Deletion strategy" defined in the Associated Information of this use case and alerts the course admin that this team has been deleted.
7. The system notifies relevant actors about the deletion of the team according to the "Notification" defined in the Associated Information of this use case.
8. Use case ends.

**Extensions:**
**Priority:** Low
**Frequency of Use:** Rare. 1 user, 1 usage per year.
**Business Rules:**

**Associated Information:**
- Data integrity and deletion rules: If a team already has students or instructors in it, deleting a team will automatically remove students and instructors from this team first. If a team already has WARs and peer evaluations,  deleting a team will automatically delete the associated WARs and peer evaluations. Deletion strategy: Team deletion is a physical delete. In other words, this will permanently remove the team and the associated WARs and peer evaluations from the database (cannot be recovered). Notification: students and instructors of the deleted team shall be notified. The course admin shall be able to cancel the use case at any time prior to submitting it.

**Related Use Cases:** The course admin may then choose to UC-TEA-5: Assign students to teams and UC-INS-2: Assign instructors to teams.
**Assumptions:**
**Open Issues:**

## **Student**

### **UC-STU-1: The Course Admin invites students to join a course section**

**UC ID and Name:** UC-STU-1: Invite students to join a course section
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:** student
**Trigger:** The course admin indicates to invite students to join a course section.
**Description:** The course admin wants to send invitation emails to students, so that they can join a course section.

**Preconditions:**
- PRE-1. The course admin is logged into the system.

**Postconditions:**
- POST-1. Invitation emails are sent to all the students.

**Main Success Scenario:**
1. The course admin indicates to invite students to join a course section.
2. The system asks the course admin to provide students' emails.
3. See the Associated Information section for format.
4. The course admin provides student emails and confirms that she has finished.
5. The system validates the course admin's inputs according to the emails format defined in the Associated Information of this use case.
6. The system displays the number of emails.
7. The system displays the email message.
8. See the Associated Information section for the default message.
9. The course admin either confirms to send the invitation (continues the normal flow) or chooses to modify the details (return to step 3).
10. The system sends out an email to each email address.
11. Use case ends.

**Extensions:**
- **4a. Input validation rule violation:**
  - 4a1. The system alerts the course admin that an input validation rule is violated and displays the nature and location of the error.
  - 4a2. The course admin corrects the mistake and returns to step 4 of the normal flow.
- **6a. The course admin indicates to personalize the default email message:**
  - 6a1. The course admin customizes the email content and confirms the message.
  - 6a2. Returns to step 6 of the normal flow.

**Priority:** High
**Frequency of Use:** 1 user, 1 usage per year.
**Business Rules:**

**Associated Information:**
- Email format: emails shall be separated by semicolon and the system shall ignore spaces in between. E.g., Good: john.doe@tcu.edu; f.smith@tcu.edu; tim.johnson@tcu.edu; lily.p.lee@tcu.edu Good: john.doe@tcu.edu;f.smith@tcu.edu Bad: john.doe@tcu.edu; f.smith@tcu.edu; Bad: john.doe@tcu.edu f.smith@tcu.edu Default email message: *Subject: Welcome to Project Pulse - Complete Your Registration Hello, [Name of the course admin] has invited you to join Project Pulse. To complete your registration, please use the link below: [Registration link] If you have any questions or need assistance, feel free to contact [course admin's email] or our team directly. Please note: This email is not monitored, so do not reply directly to this message. Best regards, Project Pulse Team*  The invitation link shall be unique for each student. The course admin shall be able to cancel the use case at any time prior to submitting it.

**Assumptions:**
**Open Issues:**

### **UC-STU-2: The Course Admin/Instructor finds students**

**UC ID and Name:** UC-STU-2: Find students
**Created By:**
**Date Created:**
**Primary Actor:** course admin, instructor
**Secondary Actors:**
**Trigger:** The user indicates to find students.
**Description:** The user wants to find students which match specific criteria, so that she can decide what to do next.

**Preconditions:**
- PRE-1. The user is logged into the system.

**Postconditions:**
- POST-1. A list of matching students is returned and displayed to the user. It is possible that the list is empty.

**Main Success Scenario:**
1. The user indicates to find students.
2. The system asks the user to enter search values according to the "Search criteria" defined in the Associated Information of this use case.
3. The user enters one or more search values and confirms that she has finished entering.
4. The system finds all students that match the provided search criteria values.
5. The system displays the matching students according to the "Search results display strategy" and the "Sort criteria" defined in the Associated Information of this use case.
6. Use case ends.

**Extensions:**
- **4a. No matching students are found:**
  - 4a1. The system alerts the user that no matching students are found.
  - 4a2. The user either chooses to UC-STU-1: Invite students to join a course section or chooses to terminate the use case or chooses to return to step 2 of the normal flow.

**Priority:** High
**Frequency of Use:** 2 users, average of 2 usages per week.
**Business Rules:**

**Associated Information:**

Search criteria (aka search fields, search attributes/properties, search details, searchable qualities):

| Search property name | Data type | Validation rule | Security/access concerns | Reference to glossary |
| ---- | ---- | ---- | ---- | ---- |
| First name | String | Optional |  |  |
| Last name | String | Optional |  |  |
| Email | String | Optional |  |  |
| course section name | String | Optional |  |  |
| Team name | String | Optional |  |  |
| course section ID | Integer | Optional |  |  |
| Team Id | Integer | Optional |  |  |

Search results display strategy (specify which properties to display for each matching course section): First name, last name, team name

Sort criteria: First, section name in descending order. Then, student last name in ascending order.

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

### **UC-STU-3: The Course Admin/Instructor views a student**

**UC ID and Name:** UC-STU-3: View a student
**Created By:**
**Date Created:**
**Primary Actor:** course admin, instructor
**Secondary Actors:**
**Trigger:** The user indicates to view the details of a student.
**Description:** The user wants to view the details of a student, so that she can get a better idea of the student.

**Preconditions:**
- PRE-1. The user is logged into the system.

**Postconditions:**
- POST-1. The details of the specified student are displayed to the user.

**Main Success Scenario:**
1. The user indicates to view the details of a student.
2. The user finds a list of students through UC-STU-2: Find students.
3. The user views the list and chooses to view the details of one specific student.
4. The system retrieves and displays the details of this student according to the "Details" defined in the Associated Information and the "Security/access concerns" defined in the Business Rules of this use case.
5. The user views the details of this student.
6. Use case ends.

**Extensions:**
**Priority:** High
**Frequency of Use:** 1 user, average of 10 usages per week.
**Business Rules:**

**Associated Information:**

Details:

| Property name | Data type | Editability | Security/access concerns | Reference to glossary |
| ---- | ---- | ---- | ---- | ---- |
| First name |  |  |  |  |
| Last name |  |  |  |  |
| course section name |  |  |  |  |
| Team name |  |  |  |  |
| Peer evaluations |  |  |  |  |
| WARs |  |  |  |  |

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

### **UC-STU-4: The Course Admin deletes a student**

**UC ID and Name:** UC-STU-4: Delete a student
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:** student
**Trigger:** The course admin indicates to delete a student.
**Description:** The course admin wants to delete a student, because a student may drop out of the course section.

**Preconditions:**
- PRE-1. The course admin is logged into the system.
- PRE-2. There exists at least one student in the system.

**Postconditions:**
- POST-1. The student is deleted from the system according to the "Deletion strategy" defined in the Associated Information of this use case.

**Main Success Scenario:**
1. The course admin indicates to delete a student .
2. The course admin views the details of this student through UC-STU-3: View a student.
3. The course admin chooses to delete this student.
4. The system alerts the course admin of the consequences of this deletion according to the "Data integrity and deletion rules" defined in the Associated Information of this use case, warns the course admin about the deletion, and asks the course admin to confirm.
5. The course admin confirms the deletion.
6. The system deletes the student according to the "Deletion strategy" defined in the Associated Information of this use case and alerts the course admin that this student has been deleted.
7. Use case ends.

**Extensions:**
**Priority:** Low
**Frequency of Use:** Rare. 1 user, 1 usage per year.
**Business Rules:**

**Associated Information:**
- Data integrity and deletion rules: If a student already submits WARs and peer evaluations,  deleting a student will automatically delete the associated WARs and peer evaluations. Deletion strategy: student deletion is a physical delete. In other words, this will permanently remove the student and the associated WARs and peer evaluations from the database (cannot be recovered). The course admin shall be able to cancel the use case at any time prior to submitting it.

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

### **UC-STU-5: The Course Admin/Instructor deactivates a student**

**UC ID and Name:** UC-STU-5: Deactivate a student
**Created By:**
**Date Created:**
**Primary Actor:** course admin / instructor
**Secondary Actors:** student
**Trigger:** The course admin or instructor indicates to deactivate a student.
**Description:** The course admin or instructor wants to deactivate a student — for example, when a student withdraws from the course section — so that the student no longer has access to the system while her submitted work is retained.

**Preconditions:**
- PRE-1. The course admin or instructor is logged into the system.
- PRE-2. There exists at least one active student in the system.

**Postconditions:**
- POST-1. The student's account is deactivated.

**Main Success Scenario:**
1. The course admin or instructor indicates to deactivate a student.
2. The course admin or instructor views the details of this student through UC-STU-3: View a student.
3. The course admin or instructor chooses to deactivate this student.
4. The system alerts the course admin or instructor of the consequences of this deactivation defined in the Associated Information of this use case, warns about the deactivation, and asks for confirmation.
5. The course admin or instructor confirms the deactivation.
6. The system deactivates the student and alerts the course admin or instructor that this student has been deactivated.
7. Use case ends.

**Extensions:**
**Priority:** Low
**Frequency of Use:** Rare. Occurs occasionally when a student withdraws from a course section.
**Business Rules:**

**Associated Information:**
- Consequence of the deactivation: The student will no longer be able to log in or submit weekly activity reports and peer evaluations, but the student's account and her previously submitted work are kept in the system. Deactivation will NOT remove the student from the system, and the account can be reactivated in the future (UC-STU-6: Reactivate a student). This is distinct from deletion (UC-STU-4: Delete a student), which is a physical, unrecoverable delete. The course admin or instructor shall be able to cancel the use case at any time prior to submitting it.

**Related Use Cases:** UC-STU-4: Delete a student; UC-STU-6: Reactivate a student
**Assumptions:**
**Open Issues:**

### **UC-STU-6: The Course Admin/Instructor reactivates a student**

**UC ID and Name:** UC-STU-6: Reactivate a student
**Created By:**
**Date Created:**
**Primary Actor:** course admin / instructor
**Secondary Actors:** student
**Trigger:** The course admin or instructor indicates to reactivate a deactivated student.
**Description:** The course admin or instructor wants to reactivate a deactivated student, so that the student regains access to the system.

**Preconditions:**
- PRE-1. The course admin or instructor is logged into the system.
- PRE-2. There exists at least one deactivated student in the system.

**Postconditions:**
- POST-1. The student's account is reactivated.

**Main Success Scenario:**
1. The course admin or instructor indicates to reactivate a deactivated student.
2. The course admin or instructor views the details of this student through UC-STU-3: View a student.
3. The course admin or instructor chooses to reactivate this student.
4. The system asks for confirmation.
5. The course admin or instructor confirms the reactivation.
6. The system reactivates the student and notifies this student that her account has been reactivated.
7. Use case ends.

**Extensions:**
**Priority:** Low
**Frequency of Use:** Rare. Occurs occasionally when a previously withdrawn student returns to a course section.
**Business Rules:**

**Associated Information:**
- The course admin or instructor shall be able to cancel the use case at any time prior to submitting it.

**Related Use Cases:** UC-STU-4: Delete a student; UC-STU-5: Deactivate a student
**Assumptions:**
**Open Issues:**

## **Instructor**

### **UC-INS-1: The Course Admin invites instructors to register an account**

**UC ID and Name:** UC-INS-1: Invite instructors to register an account
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:** instructor
**Trigger:** The course admin indicates to invite instructors to register an account.
**Description:** The course admin wants to invite instructors to register an account in the system, so that they can help supervise the senior design projects.

**Preconditions:**
- PRE-1. The course admin is logged into the system.

**Postconditions:**
- POST-1. Invitation emails are sent to instructors.

**Main Success Scenario:**
1. The course admin indicates to invite instructors to register an account.
2. The system asks the course admin to provide instructors' emails.
3. See the Associated Information section for format.
4. The course admin provides instructor emails and confirms that she has finished.
5. The system validates the course admin's inputs according to the emails format defined in the Associated Information of this use case.
6. The system displays the number of emails.
7. The system displays the email message.
8. See the Associated Information section for the default message.
9. The course admin either confirms to send the invitation (continues the normal flow) or chooses to modify the details (return to step 3).
10. The system sends out an email to each email address.
11. Use case ends.

**Extensions:**
- **4a. Input validation rule violation:**
  - 4a1. The system alerts the course admin that an input validation rule is violated and displays the nature and location of the error.
  - 4a2. The course admin corrects the mistake and returns to step 4 of the normal flow.
- **6a. The course admin indicates to personalize the default email message:**
  - 6a1. The course admin customizes the email content and confirms the message.
  - 6a2. Returns to step 6 of the normal flow.

**Priority:** High
**Frequency of Use:** 1 user, 1 usage per year.
**Business Rules:**

**Associated Information:**
- Email format: emails shall be separated by semicolon and the system shall ignore spaces in between. E.g., Good: john.doe@tcu.edu; f.smith@tcu.edu; tim.johnson@tcu.edu; lily.p.lee@tcu.edu Good: john.doe@tcu.edu;f.smith@tcu.edu Bad: john.doe@tcu.edu; f.smith@tcu.edu; Bad: john.doe@tcu.edu f.smith@tcu.edu Default email message: *Subject: Welcome to Project Pulse - Complete Your Registration Hello, [Name of the course admin] has invited you to join Project Pulse. To complete your registration, please use the link below: [Registration link] If you have any questions or need assistance, feel free to contact [course admin's email] or our team directly. Please note: This email is not monitored, so do not reply directly to this message. Best regards, Project Pulse Team*  The invitation link shall be unique for each instructor. The course admin shall be able to cancel the use case at any time prior to submitting it.

**Assumptions:**
**Open Issues:**

### **UC-INS-2: The Course Admin assigns instructors to teams**

**UC ID and Name:** UC-INS-2: Assign instructors to teams
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:** instructor
**Trigger:** The course admin indicates to assign instructors to teams.
**Description:** The course admin wants to assign instructors to teams, so that instructors can start to supervise teams assigned to them.

**Preconditions:**
- PRE-1. Teams are created.
- PRE-2. Instructors have set up their accounts.
- PRE-3. The course admin is logged into the system.

**Postconditions:**
- POST-1. Instructors are associated with teams.

**Main Success Scenario:**
1. The course admin indicates to assign instructors to teams.
2. The system displays a list of teams and a list of instructors .
3. The course admin selects a team and assigns one or more instructors to it.
4. The course admin repeats this step until she confirms that she has finished assigning instructors to all the teams.
5. The system displays the team assignment information and asks the course admin to confirm the assignment.
6. The course admin confirms the assignment.
7. The system notifies relevant actors about the assignment according to the "Notification" defined in the Associated Information of this use case.
8. Use case ends.

**Extensions:**
- **4a. The course admin finds a wrong team assignment:**
  - 4a1. The course admin removes an instructor from a team, reassign her to a new team, and returns to step 4 of the normal flow.

**Priority:** High
**Frequency of Use:** 1 user, 1 usage per year.
**Business Rules:** BR-21

**Associated Information:**
- Notification: The system notifies the instructors about their team assignment. The course admin shall be able to cancel the process at any time prior to submitting it.

**Related Use Cases:**
**Assumptions:** The instructor must be assigned to the course section of the team first.
**Open Issues:**

### **UC-INS-3: The Course Admin removes an instructor from a team**

**UC ID and Name:** UC-INS-3: Remove an instructor from a team
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:** instructor
**Trigger:** The course admin indicates to remove an instructor from a team.
**Description:** The course admin wants to remove an instructor from a team, so that this instructor on longer supervises this team.

**Preconditions:**
- PRE-1. Teams are created.
- PRE-2. Instructors have set up their accounts.
- PRE-3. The course admin is logged into the system.
- PRE-4. Instructors have been assigned to teams.

**Postconditions:**
- POST-1. The instructor is removed from a team.

**Main Success Scenario:**
1. The course admin indicates to remove an instructor from a team.
2. The course admin views the details of the team through UC-TEA-2: View a team.
3. The course admin removes an instructor from this team.
4. The system displays the new team assignments and asks the course admin to confirm the removal. The course admin confirms the removal. The system notifies relevant actors about the assignment according to the "Notification" defined in the Associated Information of this use case.
5. Use case ends.

**Extensions:**
- **5a. The course admin finds a wrong team member removal:**
  - 5a1. The course admin corrects the wrong removal and returns to step 4 of the normal flow.

**Priority:** Low
**Frequency of Use:** Rare. 1 user, 1 usage per year.
**Business Rules:** BR-21

**Associated Information:**
- Notification: The system notifies the instructor about her team removal. The course admin shall be able to cancel the process at any time prior to submitting it.

**Related Use Cases:** The course admin may immediately assign the instructor to a new team.
**Assumptions:**
**Open Issues:**

### **UC-INS-4: The Course Admin finds instructors**

**UC ID and Name:** UC-INS-4: Find instructors
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:**
**Trigger:** The course admin indicates to find instructors.
**Description:** The course admin wants to find instructors which match specific criteria, so that she can decide what to do next.

**Preconditions:**
- PRE-1. The course admin is logged into the system.

**Postconditions:**
- POST-1. A list of matching instructors is returned and displayed to the course admin. It is possible that the list is empty.

**Main Success Scenario:**
1. The course admin indicates to find instructors.
2. The system asks the course admin to enter search values according to the "Search criteria" defined in the Associated Information of this use case.
3. The course admin enters one or more search values and confirms that she has finished entering.
4. The system finds all instructors that match the provided search criteria values.
5. The system displays the matching instructors according to the "Search results display strategy" and the "Sort criteria" defined in the Associated Information of this use case.
6. Use case ends.

**Extensions:**
- **4a. No matching instructors are found:**
  - 4a1. The system alerts the course admin that no matching instructors are found.
  - 4a2. The course admin either chooses to UC-INS-1: Invite instructors to register an account or chooses to terminate the use case or chooses to return to step 2 of the normal flow.

**Priority:** High
**Frequency of Use:** 1 user, 3 usages per year.
**Business Rules:**

**Associated Information:**

Search criteria (aka search fields, search attributes/properties, search details, searchable qualities):

| Search property name | Data type | Validation rule | Security/access concerns | Reference to glossary |
| ---- | ---- | ---- | ---- | ---- |
| First name | String | Optional |  |  |
| Last name | String | Optional |  |  |
| Team name | String | Optional |  |  |
| Status | Active or Deactivated | Optional |  |  |

Search results display strategy (specify which properties to display for each matching course section): First name, last name, team name, status

Sort criteria: First, academic year in reverse chronological order. Then, instructor last name in ascending order.

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

### **UC-INS-5: The Course Admin views an instructor**

**UC ID and Name:** UC-INS-5: View an instructor
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:**
**Trigger:** The course admin indicates to view the details of an instructor.
**Description:** The course admin wants to view the details of an instructor, so that she can get a better idea of the instructor.

**Preconditions:**
- PRE-1. The course admin is logged into the system.

**Postconditions:**
- POST-1. The details of the specified instructor are displayed to the course admin.

**Main Success Scenario:**
1. The course admin indicates to view the details of an instructor.
2. The course admin finds a list of instructors through UC-INS-4: Find instructors.
3. The course admin views the list and chooses to view the details of one specific instructor.
4. The system retrieves and displays the details of this instructor according to the "Details" defined in the Associated Information and the "Security/access concerns" defined in the Business Rules of this use case.
5. The course admin views the details of this instructor.
6. Use case ends.

**Extensions:**
**Priority:** Medium
**Frequency of Use:** 1 user, 5 usages per year.
**Business Rules:**

**Associated Information:**

Details:

| Property name | Data type | Editability | Security/access concerns | Reference to glossary |
| ---- | ---- | ---- | ---- | ---- |
| First name |  |  |  |  |
| Last name |  |  |  |  |
| Supervised Teams |  |  |  |  |
| Status | Active or Deactivated |  |  |  |

Supervised teams shall be organized by section names.

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

### **UC-INS-6: The Course Admin deactivates an instructor**

**UC ID and Name:** UC-INS-6: Deactivate an instructor
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:**
**Trigger:** The course admin indicates to deactivate an instructor.
**Description:** The course admin wants to deactivate an instructor, so that this instructor no longer has access to the system.

**Preconditions:**
- PRE-1. The course admin is logged into the system.
- PRE-2. There exists at least one instructor in the system.

**Postconditions:**
- POST-1. The instructor's account is deactivated.

**Main Success Scenario:**
1. The course admin indicates to deactivate an instructor.
2. The course admin views the details of this instructor through UC-INS-5: View an instructor.
3. The course admin chooses to deactivate this instructor and enters a reason.
4. The system alerts the course admin of the consequences of this deactivationdefined in the Associated Information of this use case, warns the course admin about the deactivation, and asks the course admin to confirm.
5. The course admin confirms the deactivation.
6. The system deactivates the instructor and alerts the course admin that this instructor has been deactivated.
7. Use case ends.

**Extensions:**
**Priority:** Low
**Frequency of Use:** Rare. 1 user, 1 usage per year.
**Business Rules:**

**Associated Information:**
- Consequence of the deactivation: The instructor will no longer have access to the system. But the instructor's information is kept in the system. Deactivation: Deactivation will NOT remove the instructor from the system and the instructor's account can be recovered in the future. The course admin shall be able to cancel the use case at any time prior to submitting it.

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

### **UC-INS-7: The Course Admin reactivates an instructor**

**UC ID and Name:** UC-INS-7: Reactivate an instructor
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:**
**Trigger:** The course admin indicates to reactivate a deactivated instructor.
**Description:** The course admin wants to reactivate a deactivated instructor, so that this instructor has access to the system.

**Preconditions:**
- PRE-1. The course admin is logged into the system.
- PRE-2. There exists at least one deactivated instructor in the system.

**Postconditions:**
- POST-1. The instructor's account is reactivated.

**Main Success Scenario:**
1. The course admin indicates to reactivate a deactivated instructor.
2. The course admin views the details of this instructor through UC-INS-5: View an instructor.
3. The course admin chooses to reactivate this instructor.
4. The system asks the course admin to confirm.
5. The course admin confirms the reactivation.
6. The system reactivates the instructor and notifies this instructor that her account has been reactivated.
7. Use case ends.

**Extensions:**
**Priority:** Low
**Frequency of Use:** Rare. 1 user, 1 usage per year.
**Business Rules:**

**Associated Information:**
- The course admin shall be able to cancel the use case at any time prior to submitting it.

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

## **Account**

### **UC-ACC-1: The Student sets up a student account**

**UC ID and Name:** UC-ACC-1: Set up a student account
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:**
**Trigger:** The student clicks the registration link in the invitation email.
**Description:** The student wants to set up an account, so that she can join a course section and submit WARs and peer evaluations.

**Preconditions:**
- PRE-1. An invitation email is sent to the student.

**Postconditions:**
- POST-1. The student account is set up.

**Main Success Scenario:**
1. The student clicks the registration link in the invitation email.
2. The system opens a new page and asks the student to enter the details of this new account according to the "Details" defined in the Associated Information of this use case.
3. The student enters the details of this new account and confirms that she has finished.
4. The system validates the student's inputs according to the "Details" defined in the Associated Information of this use case.
5. The system displays the details of the new account and asks the student to confirm the registration.
6. The student either confirms the registration (continues the normal flow) or chooses to modify the details (return to step 3).
7. The system saves the information about the new account and informs the student that this account has been created.
8. The system redirects the student to the login page.
9. Use case ends.

**Extensions:**
- **2a. The student has already set up the account:**
  - 2a1. The system alerts the student that she has already set up her account and shall log in.
  - 2a2. The system redirects the student to the login page.
  - 2a3. Use case ends.
- **4a. Input validation rule violation:**
  - 4a1. The system alerts the student that an input validation rule is violated and displays the nature and location of the error.
  - 4a2. The student corrects the mistake and returns to step 4 of the normal flow.

**Priority:** High
**Frequency of Use:** Approximately 35-40 users, 1 usage per year.
**Business Rules:**

**Associated Information:**
- Details: First name Last name Email Password The student shall be able to cancel the use case at any time prior to submitting it.

**Assumptions:**
**Open Issues:**

### **UC-ACC-2: The Student edits an account**

**UC ID and Name:** UC-ACC-2: Edit an account
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:**
**Trigger:** The student indicates to change the details of her account.
**Description:** The student wants to change the details of her account, so that she can correct mistakes made during registration or change the password.

**Preconditions:**
- PRE-1. The student is logged into the system.

**Postconditions:**
- POST-1. Changes made to the account are stored in the system.

**Main Success Scenario:**
1. The student indicates to change the details of her account.
2. The system displays the details of her account.
3. The student chooses to change the details of this account.
4. The system asks the student to make changes to this account where allowed according to the "Details" defined in the Associated Information and the "Security/access concerns" defined in the Business Rules of this use case.
5. The student makes changes to this account until she confirms that she has finished changing.
6. The system validates the student's changes and alerts warning messages according to the "Details" defined in the Associated Information of this use case.
7. The student acknowledges the warnings and chooses to continue.
8. The system displays the updated details of this account and alerts the student to confirm the change.
9. The student either confirms the change (continues the normal flow) or chooses to continue to change the details (return to step 5).
10. The system saves the changes, carries out the effect of change according to the "Details" defined in the Associated Information of this use case, and informs the student that this account has been changed.
11. Use case ends.

**Extensions:**
- **6a. Input validation rule violation:**
  - 6a1. The system alerts the student that an input validation rule is violated and displays the nature and location of the error.
  - 6a2. The student corrects the mistake and returns to step 6 of the normal flow.

**Priority:** High
**Frequency of Use:** Rare. Approximately 35-40 users, 1 usage per year.
**Business Rules:**

**Associated Information:**

Details:

| Property name | Data type | Editability | Validation rule | Effect of change | Warning | Reference to glossary |
| ---- | ---- | ---- | ---- | ---- | ---- | ---- |
| First name | String | Yes |  |  |  |  |
| Last name | String | Yes |  |  |  |  |
| Email | String | Yes |  |  |  |  |

The student shall be able to cancel the use case at any time prior to submitting it.

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

### **UC-ACC-3: The Instructor sets up an instructor account**

**UC ID and Name:** UC-ACC-3: Set up an instructor account
**Created By:**
**Date Created:**
**Primary Actor:** instructor
**Secondary Actors:**
**Trigger:** The instructor clicks the registration link in the invitation email.
**Description:** The instructor wants to set up an account, so that she can supervise senior design projects in a course section.

**Preconditions:**
- PRE-1. An invitation email is sent to the instructor.

**Postconditions:**
- POST-1. The instructor account is set up.

**Main Success Scenario:**
1. The instructor clicks the registration link in the invitation email.
2. The system opens a new page and asks the instructor to enter the details of this new account according to the "Details" defined in the Associated Information of this use case.
3. The instructor enters the details of this new account and confirms that she has finished.
4. The system validates the instructor's inputs according to the "Details" defined in the Associated Information of this use case.
5. The system displays the details of the new account and asks the instructor to confirm the registration.
6. The instructor either confirms the registration (continues the normal flow) or chooses to modify the details (return to step 3).
7. The system saves the information about the new account and informs the instructor that this account has been created.
8. The system redirects the instructor to the login page.
9. Use case ends.

**Extensions:**
- **2a. The instructor has already set up the account:**
  - 2a1. The system alerts the instructor that she has already set up her account and shall log in.
  - 2a2. The system redirects the instructor to the login page.
  - 2a3. Use case ends.
- **4a. Input validation rule violation:**
  - 4a1. The system alerts the instructor that an input validation rule is violated and displays the nature and location of the error.
  - 4a2. The instructor corrects the mistake and returns to step 4 of the normal flow.

**Priority:** High
**Frequency of Use:** Approximately 2 users, average of 1 usage per year.
**Business Rules:**

**Associated Information:**
- Details: First name Middle initial Last name Password Reenter password: must be the same as password. The instructor shall be able to cancel the use case at any time prior to submitting it.

**Assumptions:**
**Open Issues:**

## **Weekly Activity Report**

### **UC-WAR-1: The Student manages activities in a Weekly Activity Report (WAR)**

**UC ID and Name:** UC-WAR-1: Manage activities in a weekly activity report
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:**
**Trigger:** The student indicates to manage activities in a WAR.
**Description:** The student wants to manage activities in a WAR, so that she can add/edit/delete an activity in a WAR.

**Preconditions:**
- PRE-1. The student is logged into the system.

**Postconditions:**
- POST-1. A new activity is added to the WAR for that week. or
- POST-2. An existing activity is edited. or
- POST-3. An existing activity is deleted.

**Main Success Scenario:**
1. The student indicates to manage activities in a WAR.
2. The system asks the student to select an active week.
3. The student specifies the active week (cannot select a future active week).
4. The system displays the activities already added by this student in the WAR and asks the student to select operations: Add a new activity (steps 6-11) Edit an existing activity (steps 12-17) Delete an existing activity (steps 18-21) The student selects one out of the three operations.
5. Based on the student's selection in step 4, the flow goes through either steps 6-11, or steps 12-17, or steps 18-21.
6. The student selects to add a new activity to this WAR.
7. See the "Details" defined in the Associated Information of this use case.
8. The student enters the details of the activity and confirms that she has finished.
9. The system validates the student's inputs according to the "Details" defined in the Associated Information of this use case.
10. The student either confirms the creation of the activity (continues the normal flow) or chooses to modify the details (return to step 7).
11. The system adds this activity to this WAR and informs the student that this WAR has been updated.
12. The student selects to edit an existing activity in this WAR.
13. The student edits the activity.
14. The system validates the change.
15. The system displays the details of the updated activity and asks the student to confirm the change.
16. The student either confirms the change (continues the normal flow) or chooses to modify the details (return to step 12).
17. The system saves the change and informs the student that this WAR has been updated.
18. The student selects to delete an existing event.
19. The system asks the student to confirm the deletion.
20. The student confirms the deletion.
21. The system deletes the activity and informs the student that this WAR has been updated.
22. Use case ends.

**Extensions:**
- **8a. Input validation rule violation:**
  - 8a1. The system alerts the student that an input validation rule is violated and displays the nature and location of the error.
  - 8a2. The student corrects the mistake and returns to step 8 of the normal flow.
- **13a. Input validation rule violation:**
  - 13a1. The system alerts the student that an input validation rule is violated and displays the nature and location of the error.
  - 13a2. The student corrects the mistake and returns to step 13 of the normal flow.

**Priority:** High
**Frequency of Use:** Approximately 35-40 users, average of 3 usages per week.
**Business Rules:**

**Associated Information:**
- Details: The student can add activities to a WAR. For each activity, the student shall provide the following: Activity category: DEVELOPMENT, TESTING, BUGFIX, COMMUNICATION, DOCUMENTATION, DESIGN, PLANNING, LEARNING, DEPLOYMENT, SUPPORT, MISCELLANEOUS Activity Description Planned hours Actual hours Status: In progress, Under testing, Done. The above properties are editable. The student shall be able to cancel the use case at any time prior to submitting it.

**Assumptions:**
**Open Issues:**

### **UC-WAR-2: The Instructor/Student generates a WAR report of a team**

**UC ID and Name:** UC-WAR-2: Generate a WAR report of a team
**Created By:**
**Date Created:**
**Primary Actor:** instructor, student
**Secondary Actors:**
**Trigger:** The instructor/student indicates to generate a WAR report of a team.
**Description:** The user wants to run a WAR report, so that she can better understand how students contribute to the project in a week.

**Preconditions:**
- PRE-1. The user is logged into the system.

**Postconditions:**
- POST-1. The details of the report are returned and displayed to the user.

**Main Success Scenario:**
1. The user indicates to generate a WAR report.
2. The system asks the user to provide configurable report generating parameters according to the "Report generating parameters" defined in the Associated Information of this use case.
3. The user enters the required parameters and confirms that she has finished entering.
4. The system validates the input parameters according to the "Report generating parameters" defined in the Associated Information of this use case.
5. The system generates the WAR report according to the "Report generating algorithm" defined in the Associated Information of this use case and displays to the user according to the "Report generating parameters" defined in the Associated Information of this use case.
6. The system delivers the generated report according to the specified report disposition in the specified format in the "Report generating parameters" defined in the Associated Information of this use case.
7. Use case ends.

**Extensions:**
- **4a. Input validation rule violation:**
  - 4a1. The system alerts the user that an input validation rule is violated and displays the nature and location of the error.
  - 4a2. The user corrects the mistake and returns to step 4 of the normal flow.
- **5a. No data is returned:**
  - 5a1. The system alerts the user that no data is available in the generated report.
  - 5a2. The user either chooses to return to step 3 of the normal flow or chooses to terminate the use case.

**Priority:** High
**Frequency of Use:** Approximately 37 users, average of 1 usage per week.
**Business Rules:**

**Associated Information:**

Report generating parameters:
- Active week: Each WAR report is associated with a week. The instructor shall first indicate for which active week she wants to generate a WAR. E.g., "02-12-2024 to 02-18-2024"; by default, it shall be the previous week.
- Columns to include: student name, Activity category, Planned activity, Description, Planned hours, Actual hours, Status. See the example below.
- Sorting criteria: by default, sort by last name in ascending order.
- Pagination: Not needed.
- Format of the generated report: HTML.

An example of the generated report:

| student | Activity category | Planned activity | Description | Planned hours | Actual hours | Status |
| ---- | ---- | ---- | ---- | ---- | ---- | ---- |
| John Doe | Bug fixing | Activity 1 | Fix the login bug…… | 4 | 5 | Done |
|  | Documentation | Activity 2 | Write three new use cases. They are …… | 5 |  | In Progress |

The report shall show who did not turn in the WAR for that week.

Report generating algorithm: N/A

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

### **UC-WAR-3: The Instructor generates a WAR report of the student**

**UC ID and Name:** UC-WAR-3: Generate a WAR report of the student
**Created By:**
**Date Created:**
**Primary Actor:** instructor
**Secondary Actors:**
**Trigger:** The instructor indicates to generate a WAR report of a student.
**Description:** The instructor wants to run a WAR report of a student, so that she can better understand how this student contributes to the project during a period of time.

**Preconditions:**
- PRE-1. The instructor is logged into the system.

**Postconditions:**
- POST-1. The details of the report are returned and displayed to the instructor.

**Main Success Scenario:**
1. The instructor indicates to generate a WAR report of a student.
2. The instructor views the details of this student through UC-STU-3: View a student.
3. The instructor chooses to generate a WAR report of this student.
4. The system asks the instructor to provide configurable report generating parameters according to the "Report generating parameters" defined in the Associated Information of this use case.
5. The instructor enters the required parameters and confirms that she has finished entering.
6. The system validates the input parameters according to the "Report generating parameters" defined in the Associated Information of this use case.
7. The system generates the WAR report according to the "Report generating algorithm" defined in the Associated Information of this use case and displays to the user according to the "Report generating parameters" defined in the Associated Information of this use case.
8. The system delivers the generated report according to the specified report disposition in the specified format in the "Report generating parameters" defined in the Associated Information of this use case.
9. Use case ends.

**Extensions:**
- **6a. Input validation rule violation:**
  - 6a1. The system alerts the instructor that an input validation rule is violated and displays the nature and location of the error.
  - 6a2. The instructor corrects the mistake and returns to step 6 of the normal flow.
- **7a. No data is returned:**
  - 7a1. The system alerts the instructor that no data is available in the generated report.
  - 7a2. The instructor either chooses to return to step 5 of the normal flow or chooses to terminate the use case.

**Priority:** High
**Frequency of Use:** 2 users, average of 10 usage per week.
**Business Rules:**

**Associated Information:**

Report generating parameters:
- Period: Start active week and end active week.
- Columns to include: Activity category, Planned activity, Description, Planned hours, Actual hours, Status. See the example below.
- Sorting criteria: by default, sort by active weeks in chronological order.
- Pagination: Not needed.
- Format of the generated report: HTML.

An example of the generated report:

Active week: 02-12-2024 to 02-18-2024

| Activity category | Planned activity | Description | Planned hours | Actual hours | Status |
| ---- | ---- | ---- | ---- | ---- | ---- |
| Bug fixing | Activity 1 | Fix the login bug…… | 4 | 5 | Done |
| Documentation | Activity 2 | Write three new use cases. They are …… | 5 |  | In Progress |

Active week: 02-19-2024 to 02-25-2024

| Activity category | Planned activity | Description | Planned hours | Actual hours | Status |
| ---- | ---- | ---- | ---- | ---- | ---- |
| New feature dev | Activity 3 | Fix the login bug…… | 10 | 9 | Done |
| Documentation | Activity 2 | Write three new use cases. They are …… | 5 | 10 | Done |
| …… |  |  |  |  |  |

Report generating algorithm: N/A

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

## **Peer Evaluation**

### **UC-EVA-1: The Student submits a peer evaluation for the previous week**

**UC ID and Name:** UC-EVA-1: Submit a peer evaluation for the previous week
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:**
**Trigger:** The student indicates to submit a peer evaluation for the previous week.
**Description:** The student wants to submit a peer evaluation for the previous week, so that she can provide feedback and assessment for every team member.

**Preconditions:**
- PRE-1. The student is logged into the system.

**Postconditions:**
- POST-1. The peer evaluation is stored in the system.

**Main Success Scenario:**
1. The student indicates to submit a peer evaluation for the previous week.
2. The system asks the student to provide peer evaluations for every team member.
3. See the "Details" defined in the Associated Information of this use case.
4. The student evaluates each team member (self included) and confirms that she has finished.
5. The system validates the student's inputs according to the "Details" defined in the Associated Information of this use case.
6. The system displays the details of the peer evaluation and asks the student to confirm the evaluation and submission.
7. The system notes that a submitted peer evaluation remains editable while its submission window is open; the student may re-submit it to update it until the window closes (BR-23, BR-24).
8. The student either confirms the evaluation and submission (continues the normal flow) or chooses to modify the details (return to step 3).
9. The system saves the peer evaluation and informs the student that this peer evaluation has been submitted.
10. Use case ends.

**Extensions:**
- **4a. Input validation rule violation:**
  - 4a1. The system alerts the student that an input validation rule is violated and displays the nature and location of the error.
  - 4a2. The student corrects the mistake and returns to step 4 of the normal flow.

**Priority:** High
**Frequency of Use:** Approximately 35-40 users, 1 usage per week.
**Business Rules:** BR-23, BR-24

**Associated Information:**

Details:
- Every team member MUST be evaluated.
- Scores MUST be integers.

Example (each team member, self included, is evaluated on every rubric criterion):

| student | Quality of work — How do you rate the quality of this teammate's work? (1-10) | … | Public comments | Private comments |
| ---- | ---- | ---- | ---- | ---- |
| John Doe | 8 | … | … | … |
| Lily Fisher | 10 | … | … | … |
| Tim Smith | 9 | … | … | … |

Private comments are for the instructor only. Public comments will be sent to the student under assessment. The student shall be able to cancel the use case at any time prior to submitting it.

**Assumptions:**
**Open Issues:**

### **UC-EVA-2: The Student views her own peer evaluation report**

**UC ID and Name:** UC-EVA-2: View her own peer evaluation report
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:**
**Trigger:** The student indicates to view her own peer evaluation report (on demand).
**Description:** The student wants to run a peer evaluation report, so that she can better understand how she is assessed by her teammates.

**Preconditions:**
- PRE-1. The student is logged into the system.

**Postconditions:**
- POST-1. The details of the report are returned and displayed to the student.

**Main Success Scenario:**
1. The student indicates to generate a peer evaluation report.
2. The system asks the student to provide configurable report generating parameters according to the "Report generating parameters" defined in the Associated Information of this use case.
3. The student enters the required parameters and confirms that she has finished entering.
4. The system validates the input parameters according to the "Report generating parameters" defined in the Associated Information of this use case.
5. The system generates the peer evaluation report according to the "Report generating algorithm" defined in the Associated Information of this use case and displays to the student according to the "Report generating parameters" defined in the Associated Information of this use case.
6. The system delivers the generated report according to the specified report disposition in the specified format in the "Report generating parameters" defined in the Associated Information of this use case.
7. Use case ends.

**Extensions:**
- **4a. Input validation rule violation:**
  - 4a1. The system alerts the student that an input validation rule is violated and displays the nature and location of the error.
  - 4a2. The student corrects the mistake and returns to step 4 of the normal flow.
- **5a. No data is returned:**
  - 5a1. The system alerts the student that no data is available in the generated report.
  - 5a2. The student either chooses to return to step 3 of the normal flow or chooses to terminate the use case.

**Priority:** High
**Frequency of Use:** Approximately 35-40 users, average of 1 usage per week.
**Business Rules:** BR-25

**Associated Information:**

Report generating parameters:
- Active week: Each peer evaluation report is associated with a week. The instructor shall first indicate for which active week she wants to generate a peer evaluation. E.g., "02-12-2024 to 02-18-2024"; by default, it shall be the previous week.
- Columns to include: student name, average rubric criterion scores, public comments, average total grade. See the example below.
- Pagination: Not needed.
- Format of the generated report: HTML.

An example of the generated report:

| student | Quality of work — How do you rate the quality of this teammate's work? (1-10) | … | Public comments | Grade |
| ---- | ---- | ---- | ---- | ---- |
| John Doe | 8.5 | … | Good work. Need to work harder. … | 54/60 |

Attention, a student shall never see the private comments and the evaluators.

Report generating algorithm: For each individual criterion score (e.g., Quality of work), the system shall consider the scores provided by all teammates and compute an average. For the overall grade, see the algorithm in UC-EVA-3: Generate a peer evaluation report of the entire course section.

**Related Use Cases:** UC-EVA-3:Generate a peer evaluation report of the entire course section
**Assumptions:**
**Open Issues:**

### **UC-EVA-3: The Instructor generates a peer evaluation report of the entire course section**

**UC ID and Name:** UC-EVA-3: Generate a peer evaluation report of the entire course section
**Created By:**
**Date Created:**
**Primary Actor:** instructor
**Secondary Actors:**
**Trigger:** The instructor indicates to generate a peer evaluation report of the entire course section.
**Description:** The instructor wants to run a peer evaluation report, so that she can better understand students' performance within a team environment.

**Preconditions:**
- PRE-1. The instructor is logged into the system.

**Postconditions:**
- POST-1. The details of the report are returned and displayed to the instructor.

**Main Success Scenario:**
1. The instructor indicates to generate a peer evaluation report of the entire course section.
2. The system asks the instructor to provide configurable report generating parameters according to the "Report generating parameters" defined in the Associated Information of this use case.
3. The instructor enters the required parameters and confirms that she has finished entering.
4. The system validates the input parameters according to the "Report generating parameters" defined in the Associated Information of this use case.
5. The system generates the peer evaluation report according to the "Report generating algorithm" defined in the Associated Information of this use case and displays to the instructor according to the "Report generating parameters" defined in the Associated Information of this use case.
6. The system delivers the generated report according to the specified report disposition in the specified format in the "Report generating parameters" defined in the Associated Information of this use case.
7. Use case ends.

**Extensions:**
- **4a. Input validation rule violation:**
  - 4a1. The system alerts the instructor that an input validation rule is violated and displays the nature and location of the error.
  - 4a2. The instructor corrects the mistake and returns to step 4 of the normal flow.
- **5a. No data is returned:**
  - 5a1. The system alerts the instructor that no data is available in the generated report.
  - 5a2. The instructor either chooses to return to step 3 of the normal flow or chooses to terminate the use case.

**Priority:** High
**Frequency of Use:** Approximately 2 users, average of 1 usage per week.
**Business Rules:**

**Associated Information:**

Report generating parameters:
- Active week: Each peer evaluation report is associated with a week. The instructor shall first indicate for which active week she wants to generate a peer evaluation. E.g., "02-12-2024 to 02-18-2024"; by default, it shall be the previous week.
- Columns to include: student name, grade, comments. See the example below.
- Sorting criteria: by default, sort by last name in ascending order.
- Pagination: Not needed.
- Format of the generated report: HTML.

An example of the generated report:

| student | Grade | Commented by | Public comments | Private comments |
| ---- | ---- | ---- | ---- | ---- |
| John Doe | 54/60 | Tim Smith | Good work. | Nothing. |
|  |  | Lily Fisher | Need to work harder. | Dr. Wei, I need to talk more about John. |
| Lily Fisher | … | … | … | … |

The report shall show who did not turn in the peer evaluation for that week.

Report generating algorithm: How to compute the peer evaluation grade for a student? Each student receives multiple peer evaluations from her teammates every week. First, obtain the peer evaluations received by a student for that week. For each peer evaluation, compute a total score by adding up the individual criterion scores. Then compute the average of the total scores across the peer evaluations. For example, John Doe receives two peer evaluations from Tim Smith and Lily Fisher, respectively. Tim Smith gives the following scores based on the rubric: 10, 9, 10, 9, 10, 10. So the total score given by Tim is 58. Lily Fisher gives the following scores based on the rubric: 5, 5, 10, 10, 10, 10. So the total score given by Tim is 50. The grade that John Doe receives that week is (58 + 50) / 2 = 54.

Details of a peer evaluation: The instructor may choose to see more details of one student's peer evaluation. For example, this table below shows the scores given by each evaluator in the same team to John Doe.

| Evaluator of John Doe | Quality of work — How do you rate the quality of this teammate's work? (1-10) | … | Public comments | Private comments |
| ---- | ---- | ---- | ---- | ---- |
| John Doe | 10 | … | … | … |
| Lily Fisher | 6 | … | … | … |
| Tim Smith | 9 | … | … | … |

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

### **UC-EVA-4: The Instructor generates a peer evaluation report of a student**

**UC ID and Name:** UC-EVA-4: Generate a peer evaluation report of a student
**Created By:**
**Date Created:**
**Primary Actor:** instructor
**Secondary Actors:**
**Trigger:** The instructor indicates to generate a peer evaluation report of a student.
**Description:** The instructor wants to run a peer evaluation report of a student, so that she can better understand this student's performance during a period of time.

**Preconditions:**
- PRE-1. The instructor is logged into the system.

**Postconditions:**
- POST-1. The details of the report are returned and displayed to the instructor.

**Main Success Scenario:**
1. The instructor indicates to generate a peer evaluation report of a student.
2. The instructor views the details of this student through UC-STU-3: View a student.
3. The instructor chooses to generate a peer evaluation report of this student.
4. The system asks the instructor to provide configurable report generating parameters according to the "Report generating parameters" defined in the Associated Information of this use case.
5. The instructor enters the required parameters and confirms that she has finished entering.
6. The system validates the input parameters according to the "Report generating parameters" defined in the Associated Information of this use case.
7. The system generates the peer evaluation report according to the "Report generating algorithm" defined in the Associated Information of this use case and displays to the instructor according to the "Report generating parameters" defined in the Associated Information of this use case.
8. The system delivers the generated report according to the specified report disposition in the specified format in the "Report generating parameters" defined in the Associated Information of this use case.
9. Use case ends.

**Extensions:**
- **6a. Input validation rule violation:**
  - 6a1. The system alerts the instructor that an input validation rule is violated and displays the nature and location of the error.
  - 6a2. The instructor corrects the mistake and returns to step 6 of the normal flow.
- **7a. No data is returned:**
  - 7a1. The system alerts the instructor that no data is available in the generated report.
  - 7a2. The instructor either chooses to return to step 5 of the normal flow or chooses to terminate the use case.

**Priority:** High
**Frequency of Use:** 2 users, average of 10 usage per week.
**Business Rules:**

**Associated Information:**

Report generating parameters:
- Period: Start active week and end active week.
- Columns to include: Week, grade, comments. See the example below.
- Sorting criteria: by default, sort by week in chronological order.
- Pagination: Not needed.
- Format of the generated report: HTML.

An example of the generated report:

| Week | Grade | Commented by | Public comments | Private comments |
| ---- | ---- | ---- | ---- | ---- |
| 02-12-2024 - 02-18-2024 | 54/60 | Tim Smith | Good work. | Nothing. |
|  |  | Lily Fisher | Need to work harder. | Dr. Wei, I need to talk more about John. |
| 02-19-2024 - 02-25-2024 | 55/60 | … | … | … |

Report generating algorithm: Refer to the algorithm defined in UC-EVA-3: Generate a peer evaluation report of the entire course section.

Details of a peer evaluation: The instructor may choose to see more details of one student's peer evaluation. Refer to the algorithm defined in UC-EVA-3: Generate a peer evaluation report of the entire course section.

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

## **Templates and Provisioning**

### **UC-TPL-1: The Course Admin creates team documents from built-in templates**

**UC ID and Name:** UC-TPL-1: Create team documents from built-in templates
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:**
**Trigger:** The course admin indicates to set up the requirement documents for a team.
**Description:** When a team is created, it has no requirement documents. The course admin instantiates the team's requirement documents from the built-in templates so that the documents and their document sections exist — empty and ready to author — and become visible to the team. This is normally done once per team, immediately after the team is created.

**Preconditions:**
- PRE-1. The course admin is logged into the system.
- PRE-2. The team exists.
- PRE-3. Built-in templates are configured for the team.

**Postconditions:**
- POST-1. The team's requirement documents, each with the document sections defined by its built-in template, exist in the system.
- POST-2. The newly created documents are empty (no authored content) and visible to the team for authoring.

**Main Success Scenario:**
1. The course admin indicates to set up the requirement documents for a team.
2. The system displays the set of documents that will be created from the team's built-in templates.
3. The course admin confirms the creation.
4. The system creates each requirement document and instantiates its document sections from the corresponding built-in template, with no authored content.
5. The system makes the created documents available to the team.
6. The system informs the course admin that the team's documents have been created.
7. Use case ends.

**Extensions:**
- **3a. One or more documents already exist for the team**
  - 3a1. The system informs the course admin that the team already has requirement documents of those types; an existing document cannot be regenerated or overwritten, so only the missing document types can be created.
  - 3a2. The course admin either creates only the missing documents (the types not yet created) or terminates the use case.
- **4a. No built-in templates are configured for the team**
  - 4a1. The system alerts the course admin that no templates are available for the team and that no documents can be created.
  - 4a2. The course admin terminates the use case.

**Priority:** High
**Frequency of Use:** Once per team, at team setup; rarely repeated.
**Business Rules:** BR-3 — Only a course admin may create a team's requirement documents, and each document type is created at most once per team (an existing document cannot be regenerated or overwritten).
**Associated Information:**

Details:
- The set of documents created is determined by the built-in templates configured for the team (e.g., the Project Glossary, Use Cases, Business Rules, and section-based requirement documents such as Vision and Scope and SRS).
- The document sections of each document are instantiated from its template; documents and document sections are created empty, ready for authoring.
- MVP: built-in templates are fixed (hardcoded); template customization is out of scope.

**Related Use Cases:** UC-GLO-1: View the Project Glossary; UC-DOC-1: View a section-based requirement document; UC-DOC-3: View the Use Cases document.
**Assumptions:**
**Open Issues:**

## **Glossary**

### **UC-GLO-1: The Student views the Project Glossary**

**UC ID and Name:** UC-GLO-1: View the Project Glossary
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:**
**Trigger:** The student indicates to view the Project Glossary.
**Description:** The student wants to view the Project Glossary so that she can learn about all the glossary terms defined so far.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the Project Glossary.

**Postconditions:**
- POST-1. All glossary terms are returned and displayed to the student. It is possible that the list is empty.

**Main Success Scenario:**
1. The student indicates to view the Project Glossary.
2. The system finds all glossary terms and displays them according to the "Search results display strategy" and the "Sort criteria" defined in the Associated Information of this use case.
3. Use case ends.

**Extensions:**
- **2a. The student chooses to select a different set of properties to display the matching glossary terms**
  - 2a1. The system displays the current "Search results display strategy."
  - 2a2. The student enters a customized "Search results display strategy," confirms that she has finished entering, and returns to step 2 of the normal flow.
- **2b. The student chooses to re-sort the search results**
  - 2b1. The student re-sorts the search result according to the "Sort criteria" defined in the Associated Information of this use case and returns to step 2 of the normal flow.

**Priority:** High
**Frequency of Use:** Varies by team and project phase; most frequent during active authoring and near deadlines.
**Business Rules:** BR-1, BR-2 (team-scoped access control).
**Associated Information:**
- Search results display strategy (specify which properties to display for each matching glossary term): Term, Definition
- Sort criteria: ASC by term

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

### **UC-GLO-2: The Student finds glossary terms**

**UC ID and Name:** UC-GLO-2: Find glossary terms
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:**
**Trigger:** The student indicates to find glossary terms.
**Description:** The student wants to find glossary terms that match specific criteria, so that she can decide what to do next about those found terms.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the Project Glossary.

**Postconditions:**
- POST-1. A list of matching glossary terms is returned and displayed to the student. It is possible that the list is empty.

**Main Success Scenario:**
1. The student indicates to find glossary terms.
2. The system asks the student to enter search values according to the "Search criteria" defined in the Associated Information of this use case.
3. The student enters one or more search values and confirms that she has finished entering.
4. The system finds all glossary terms that match the provided search criteria values.
5. The system displays the matching glossary terms according to the "Search results display strategy" and the "Sort criteria" defined in the Associated Information of this use case.
6. Use case ends.

**Extensions:**
- **4a. No matching glossary terms are found**
  - 4a1. The system alerts the student that no matching glossary terms are found.
  - 4a2. The student either chooses to UC-GLO-4: create a glossary term or chooses to terminate the use case or chooses to return to step 2 of the normal flow.
- **5a. The student chooses to select a different set of properties to display the matching glossary terms**
  - 5a1. The system displays the current "Search results display strategy."
  - 5a2. The student enters a customized "Search results display strategy," confirms that she has finished entering, and returns to step 5 of the normal flow.
- **5b. The student chooses to re-sort the search results**
  - 5b1. The student re-sorts the search result according to the "Sort criteria" defined in the Associated Information of this use case and returns to step 5 of the normal flow.

**Priority:** High
**Frequency of Use:** Varies by team and project phase; most frequent during active authoring and near deadlines.
**Business Rules:** BR-1, BR-2 (team-scoped access control).
**Associated Information:**

Search criteria (aka search fields, search attributes/properties, search details, searchable qualities):

| Search property name | Data type | Options | Validation rule | Security/access concerns | Reference to glossary |
| ---- | ---- | ---- | ---- | ---- | ---- |
| Term | String |  |  |  |  |
| Definition | String |  |  |  |  |

The system shall support fuzzy search for both glossary terms and term definitions.

Search results display strategy (specify which properties to display for each matching glossary term): Term, Definition

Sort criteria: ASC by term

**Related Use Cases:** The student can perform other actions after this use case. After this use case succeeds, the student may select any of the displayed glossary terms and take any of the following actions on the selected item:
- UC-GLO-3: View a glossary term
- UC-GLO-4: Create a glossary term
- UC-GLO-5: Change a glossary term definition
- UC-GLO-6: Rename a glossary term
- UC-GLO-7: Delete a glossary term

**Assumptions:**
**Open Issues:**

### **UC-GLO-3: The Student views a glossary term**

**UC ID and Name:** UC-GLO-3: View a glossary term
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:**
**Trigger:** The student indicates to view the details of a glossary term.
**Description:** The student wants to view the details of a glossary term so that she can learn more about the glossary term.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the Project Glossary.

**Postconditions:**
- POST-1. The details of the specified glossary term are displayed to the student.

**Main Success Scenario:**
1. The student indicates to view the details of a glossary term.
2. The student finds a list of glossary terms through UC-GLO-2: Find glossary terms.
3. The student views the list and chooses to view the details of one specific glossary term.
4. The system retrieves and displays the details of this glossary term according to the "Details" defined in the Associated Information and the "Security/access concerns" defined in the business rules of this use case.
5. The student views the details of this glossary term.
6. Use case ends.

**Extensions:**

**Priority:** High
**Frequency of Use:** Varies by team and project phase; most frequent during active authoring and near deadlines.
**Business Rules:** BR-1, BR-2 (team-scoped access control).
**Associated Information:**

Details:

| Property name | Data type | Editability | Security/access concerns | Reference to glossary |
| ---- | ---- | ---- | ---- | ---- |
| Term | String |  |  |  |
| Definition | String |  |  |  |
| Created by | String |  |  |  |
| Created date | String |  |  |  |
| Last modified by | String |  |  |  |
| Last modified date | String |  |  |  |

**Related Use Cases:** UC-GLO-2: Find glossary terms
**Assumptions:**
**Open Issues:**

### **UC-GLO-4: The Student creates a glossary term**

**UC ID and Name:** UC-GLO-4: Create a glossary term
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:** Other students on the same team
**Trigger:** The student indicates to create a new glossary term.
**Description:** The student wants to create a new glossary term so that the new term can be used in other requirement documents.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the Project Glossary.
- PRE-3. The document is not locked for review.

**Postconditions:**
- POST-1. The new glossary term is stored in the system.
- POST-2. The system records authorship metadata (e.g., last-modified timestamp and editor identity).

**Main Success Scenario:**
1. The student indicates to create a new glossary term.
2. The system asks the student to enter the details of this new glossary term according to the "Details" defined in the Associated Information of this use case.
3. The student enters the details of this new glossary term and confirms that she has finished.
4. The system validates the student's inputs according to the "Details" defined in the Associated Information of this use case.
5. The system validates that the creation of the new glossary term will not duplicate any existing glossary term according to the "Duplication detection rules" defined in the Associated Information of this use case.
6. The system displays the details of the new glossary term and asks the student to confirm the creation.
7. The student either confirms the creation (continues the normal flow) or chooses to modify the details (return to step 3).
8. The system saves the new glossary term and informs the student that this glossary term has been created.
9. Use case ends.

**Extensions:**
- **4a. Input validation rule violation:**
  - 4a1. The system alerts the student that an input validation rule is violated and displays the nature and location of the error.
  - 4a2. The student corrects the mistake and returns to step 4 of the normal flow.
- **5a. The system finds possible duplicates from the existing glossary terms:**
  - 5a1. The system alerts the student that the glossary term she is trying to create already exists in the system.
  - 5a2. The student either chooses to correct the mistake and return to step 4 of the normal flow or chooses to terminate the use case.

**Priority:** High
**Frequency of Use:** Varies by team and project phase; most frequent during active authoring and near deadlines.
**Business Rules:** BR-1, BR-2 (team-scoped access control); BR-6 (a glossary term name is unique within the team's Glossary).
**Associated Information:**

Details:

| Property name | Data type | Editability | Validation rule | Effect of change | Reference to glossary |
| ---- | ---- | ---- | ---- | ---- | ---- |
| Term | String | Yes |  |  |  |
| Definition | String | Yes |  |  |  |

*Column "Effect of change" shows the consequences of modification other than saving.*

Duplication detection rules: glossary term must be unique

Notification: None — routine authoring changes are not notified (FR-NOT-2); saved changes propagate to connected teammates in real time (UC-COL-1).

The student shall be able to cancel the use case at any time prior to submitting it.

**Related Use Cases:** The student may first choose to UC-GLO-2: Find glossary terms, but cannot find any, then decide to create one.
**Assumptions:**
**Open Issues:**

### **UC-GLO-5: The Student changes a glossary term definition**

**UC ID and Name:** UC-GLO-5: Change a glossary term definition
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:** Other students on the same team
**Trigger:** The student indicates to change the details of an existing glossary term.
**Description:** The student wants to change the definition of an existing glossary term, so that the definition remains accurate and up-to-date.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the Project Glossary.
- PRE-3. The document is not locked for review.

**Postconditions:**
- POST-1. Changes made to the glossary term definition are stored in the system.
- POST-2. The system records authorship metadata (e.g., last-modified timestamp and editor identity).

**Main Success Scenario:**
1. The student indicates to change the definition of an existing glossary term.
2. The student views the definition of this glossary term through UC-GLO-3: View a glossary term.
3. The student chooses to change the definition of this glossary term.
4. The system asks the student to make changes to this glossary term where allowed according to the "Details" defined in the Associated Information and the "Security/access concerns" defined in the business rules of this use case.
5. The student makes changes to this glossary term definition until she confirms that she has finished changing.
6. The system validates the student's changes.
7. The system displays the updated definition of this glossary term and alerts the student to confirm the change.
8. The student either confirms the change (continues the normal flow) or chooses to continue to change the definition (return to step 5).
9. The system saves the changes, carries out the effect of change according to the "Details" defined in the Associated Information of this use case, and informs the student that this glossary term has been changed.
10. Use case ends.

**Extensions:**
- **6a. Input validation rule violation:**
  - 6a1. The system alerts the student that an input validation rule is violated and displays the nature and location of the error.
  - 6a2. The student corrects the mistake and returns to step 6 of the normal flow.

**Priority:** High
**Frequency of Use:** Varies by team and project phase; most frequent during active authoring and near deadlines.
**Business Rules:** BR-1, BR-2 (team-scoped access control).
**Associated Information:**

Details:

| Property name | Data type | Editability | Validation rule | Effect of change | Warning | Reference to glossary |
| ---- | ---- | ---- | ---- | ---- | ---- | ---- |
| Definition | String | Yes |  |  |  |  |

*Column "Effect of change" shows consequences of modification other than saving.*

Notification: None — routine authoring changes are not notified (FR-NOT-2); saved changes propagate to connected teammates in real time (UC-COL-1).

The student shall be able to cancel the use case at any time prior to submitting it.

**Related Use Cases:** The student may first choose to UC-GLO-3: View a glossary term, then decide to change one.
**Assumptions:**
**Open Issues:**

### **UC-GLO-6: The Student renames a glossary term**

**UC ID and Name:** UC-GLO-6: Rename a glossary term
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:** Other students on the same team
**Trigger:** The student indicates to rename a glossary term.
**Description:** The student wants to rename a glossary term so that the best term is used in the requirement documents.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the Project Glossary.
- PRE-3. The document is not locked for review.

**Postconditions:**
- POST-1. Terminology references across documents remain consistent with the updated Glossary state.
- POST-2. The system records authorship metadata (e.g., last-modified timestamp and editor identity).

**Main Success Scenario:**
1. The student indicates to rename a glossary term.
2. The system asks the student to enter the new term name.
3. The student enters the new term name.
4. The system validates the new term name (e.g., not empty and unique within the team's Glossary).
5. The system displays a preview list of all references that will be updated across the team's requirement documents.
6. The student confirms the rename operation.
7. The system updates the glossary term name and updates all references to the term.
8. The system saves all changes, records authorship metadata, and informs the student that the rename completed successfully.
9. Use case ends.

**Extensions:**
- **4a. Duplicate term name**
  - 4a1. The system alerts the student that the new term name already exists in the glossary document
  - 4a2. The student corrects the mistake and returns to step 4 of the normal flow.
- **4b. Invalid term name**
  - 4b1. The system alerts the student that an input validation rule is violated and displays the nature and location of the error.
  - 4b2. The student corrects the mistake and returns to step 4 of the normal flow.

**Priority:** High
**Frequency of Use:** Varies by team and project phase; most frequent during active authoring and near deadlines.
**Business Rules:** BR-1, BR-2 (team-scoped access control); BR-6 (a glossary term name is unique within the team's Glossary).
**Associated Information:**

Details:
- The system must support "preview before apply" for rename operations.
- Reference updates must be consistent and deterministic (no partial selection by default).
- If references are stored structurally, updates use the stored reference relationships; if references are text-based, updates must be bounded to valid term references only.

Notification: None — routine authoring changes are not notified (FR-NOT-2); saved changes propagate to connected teammates in real time (UC-COL-1).

The student shall be able to cancel the use case at any time prior to submitting it.

**Related Use Cases:** The student may first choose to UC-GLO-3: View a glossary term, then decide to rename one.
**Assumptions:**
**Open Issues:**

### **UC-GLO-7: The Student deletes a glossary term**

**UC ID and Name:** UC-GLO-7: Delete a glossary term
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:**
**Trigger:** The student indicates to delete a glossary term.
**Description:** The student wants to delete a glossary term so that this glossary term is removed from active use in the project. The system enforces data integrity rules to prevent deletion of terms that are still referenced by requirement artifacts.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the Project Glossary.
- PRE-3. The document is not locked for review.

**Postconditions:**
- POST-1. The glossary term is deleted from the system according to the "Deletion strategy" defined in the Associated Information of this use case.

**Main Success Scenario:**
1. The student indicates to delete an existing glossary term.
2. The student views the details of this glossary term through UC-GLO-3: View a glossary term.
3. The student chooses to delete this glossary term.
4. The system validates that the deletion can be carried out according to the "Referring objects handling strategy" defined in the Associated Information of this use case.
5. The system alerts the student of the consequences of this deletion according to the "Referring objects handling strategy" defined in the Associated Information of this use case, warns the student about the deletion, and asks the student to confirm.
6. The student confirms the deletion.
7. The system deletes the glossary term according to the "Deletion strategy" and the "Referring objects handling strategy" defined in the Associated Information of this use case and alerts the student that this glossary term has been deleted.
8. Use case ends.

**Extensions:**
- **4a. Data integrity and deletion rule violation:**
  - 4a1. The system alerts the student that the deletion is blocked due to existing references.
  - 4a2. The system prompts the student to remove such references before retrying this use case.
  - 4a3. use case terminates.

**Priority:** High
**Frequency of Use:** Varies by team and project phase; most frequent during active authoring and near deadlines.
**Business Rules:** BR-1, BR-2 (team-scoped access control); BR-12 (a referenced term cannot be deleted; deletion is logical).
**Associated Information:**

Deletion strategy:
- Logical delete: The glossary term is marked as deleted and removed from active use.
- Deleted glossary terms are not visible to students and cannot be reused.
- Only privileged administrative users may view deleted terms for audit purposes.

Referring objects handling strategy:
- Deletion is blocked if the glossary term is referenced by any requirement artifact.
- The student must remove or update references via other use cases before retrying deletion.

Notification: None — routine authoring changes are not notified (FR-NOT-2); saved changes propagate to connected teammates in real time (UC-COL-1).

The student shall be able to cancel the use case at any time prior to submitting it.

**Related Use Cases:** The student may first choose to UC-GLO-3: View a glossary term, then decide to delete one.
**Assumptions:**
**Open Issues:**

## **Requirement Documents**

### **UC-DOC-1: The Student views a section-based requirement document**

**UC ID and Name:** UC-DOC-1: View a section-based requirement document
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:**
**Trigger:** The student indicates to view a section-based requirement document.
**Description:** The student wants to view a section-based requirement document (e.g., Vision and Scope, SRS, business rules) so that she can read and review the document to better understand the project.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the requirement document.

**Postconditions:**
- POST-1. All the document sections of the document are displayed to the student.

**Main Success Scenario:**
1. The student indicates to view a section-based requirement document.
2. The system retrieves all the related document sections and displays them to the student in the correct order.
3. The system displays each document section's title, guidance/examples (if any), and current content.
4. The student navigates between document sections and reads the content.
5. Use case ends.

**Extensions:**

**Priority:** High
**Frequency of Use:** Varies by team and project phase; most frequent during active authoring and near deadlines.
**Business Rules:** BR-1, BR-2 (team-scoped access control).
**Associated Information:**
**Related Use Cases:**
**Assumptions:**
**Open Issues:**

### **UC-DOC-2: The Student edits a section-based requirement document**

**UC ID and Name:** UC-DOC-2: Edit a section-based requirement document
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:** Other students on the same team
**Trigger:** The student indicates to edit a section-based requirement document (e.g., Vision and Scope, SRS).
**Description:** The student wants to edit the content in a structured, section-based requirement document so that project requirements are recorded in the correct document sections and remain current.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the requirement document.
- PRE-3. The document is not locked for review.

**Postconditions:**
- POST-1. The updated document section content is stored in the system.
- POST-2. The system records authorship metadata (e.g., last-modified timestamp and editor identity).

**Main Success Scenario:**
1. The student indicates to edit a section-based requirement document (e.g., Vision and Scope, SRS).
2. The student views the document sections of the document through UC-DOC-1: View a section-based requirement document.
3. The student selects a document section to edit.
4. The system acquires an exclusive lock on the selected document section for the student.
5. The system displays the document section's guidance and examples, as well as the current content in an editor.
6. The student edits the document section content.
7. The system performs basic validation on the edited content and displays any warnings or errors.
8. The student confirms completion of the edit.
9. The system saves the updated content, updates authorship metadata, and confirms that the document has been updated.
10. The system releases the lock on the document section.
11. Use case ends.

**Extensions:**
- **4a. document section already locked by another team member**
  - 4a1. The system alerts the student that another team member is currently editing this document section and does not grant the lock.
  - 4a2. The student either chooses to select a different document section (returns to step 3) or chooses to terminate the use case.
- **7a. Input validation rule violation**
  - 7a1. The system alerts the student that an input validation rule is violated and displays the nature and location of the error.
  - 7a2. The student corrects the mistake and returns to step 7 of the normal flow.

**Priority:** High
**Frequency of Use:** Very frequent: multiple times per student per working session; peaks near deadlines.
**Business Rules:** BR-1 (team-scoped access control); BR-9 (editing a document section requires an exclusive lock).
**Associated Information:**

Details:
- The set of document sections, their order, and the rules for each section of a section-based document (Vision and Scope, SRS) are defined by that document's built-in Template (see UC-TPL-1: Create team documents from built-in templates). This use case edits one document section at a time within that template-defined structure.

Notification: None — routine authoring changes are not notified (FR-NOT-2); saved changes propagate to connected teammates in real time (UC-COL-1).

The student shall be able to cancel the use case at any time prior to submitting it.

**Related Use Cases:** The student may first choose to UC-DOC-1: View a section-based requirement document, then decide to edit one.
**Assumptions:**
**Open Issues:**

### **UC-DOC-3: The Student views the Use Cases document**

**UC ID and Name:** UC-DOC-3: View the Use Cases document
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:**
**Trigger:** The student indicates to view the Use Cases document.
**Description:** The student wants to view the Use Cases document so that she can read the catalog of use cases defined for the project so far.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the Use Cases document.

**Postconditions:**
- POST-1. All use cases are returned and displayed to the student. It is possible that the list is empty.

**Main Success Scenario:**
1. The student indicates to view the Use Cases document.
2. The system finds all use cases and displays them according to the "Search results display strategy" and the "Sort criteria" defined in the Associated Information of this use case.
3. The student may choose to view the details of one specific use case through UC-DOC-4: View a use case.
4. Use case ends.

**Extensions:**
- **2a. The student chooses to select a different set of properties to display the matching use cases**
  - 2a1. The system displays the current "Search results display strategy."
  - 2a2. The student enters a customized "Search results display strategy," confirms that she has finished entering, and returns to step 2 of the normal flow.
- **2b. The student chooses to re-sort the search results**
  - 2b1. The student re-sorts the search results according to the "Sort criteria" defined in the Associated Information of this use case and returns to step 2 of the normal flow.

**Priority:** High
**Frequency of Use:** Varies by team and project phase; most frequent during active authoring and near deadlines.
**Business Rules:** BR-1, BR-2 (team-scoped access control).
**Associated Information:**
- Search results display strategy (specify which properties to display for each matching use case): UC ID and Name, Primary Actor, Priority
- Sort criteria: ASC by UC ID

**Related Use Cases:** UC-DOC-4: View a use case; UC-DOC-5: Create a use case.
**Assumptions:**
**Open Issues:**

### **UC-DOC-4: The Student views a use case**

**UC ID and Name:** UC-DOC-4: View a use case
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:**
**Trigger:** The student indicates to view the details of a use case.
**Description:** The student wants to view the details of a use case so that she can better understand the user requirements.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the Use Cases document.

**Postconditions:**
- POST-1. The details of the specified use case are displayed to the student.

**Main Success Scenario:**
1. The student indicates to view the details of a use case.
2. The student finds a list of use cases through UC-DOC-3: View the Use Cases document.
3. The student views the list and chooses to view the details of one specific use case.
4. The system retrieves and displays the details of this use case according to the "Details" defined in the Associated Information and the "Security/access concerns" defined in the business rules of this use case.
5. The student views the details of this use case.
6. Use case ends.

**Extensions:**

**Priority:** High
**Frequency of Use:** Varies by team and project phase; most frequent during active authoring and near deadlines.
**Business Rules:** BR-1, BR-2 (team-scoped access control).
**Associated Information:**

Details:
- use case ID and Name
- Author and Date Created
- Primary and Secondary Actors
- Trigger
- Description
- Preconditions
- Postconditions
- Main Success Scenario/Normal Flow
- Extensions
- Priority
- Frequency of Use
- Business Rules
- Associated Information
- Assumptions
- Open Issues

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

### **UC-DOC-5: The Student creates a use case**

**UC ID and Name:** UC-DOC-5: Create a use case
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:**
**Trigger:** The student indicates to create a new use case in the Use Cases document.
**Description:** The student wants to create a new use case in the Use Cases document so that a new user requirement is added in the document.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the Use Cases document.
- PRE-3. The document is not locked for review.

**Postconditions:**
- POST-1. The new use case is stored in the system.
- POST-2. The system records authorship metadata (e.g., last-modified timestamp and editor identity).

**Main Success Scenario:**
1. The student indicates to create a new use case in the Use Cases document.
2. The system asks the student to enter the details of this new use case according to the "Details" defined in the Associated Information of this use case.
3. The student enters the details of this new use case and confirms that she has finished.
4. The system validates the student's inputs according to the "Details" defined in the Associated Information of this use case.
5. The system validates that the creation of the new use case will not duplicate any existing use case according to the "Duplication detection rules" defined in the Associated Information of this use case.
6. The system displays the details of the new use case and asks the student to confirm the creation.
7. The student either confirms the creation (continues the normal flow) or chooses to modify the details (returns to step 3).
8. The system saves the new use case and informs the student that this use case has been created.
9. Use case ends.

**Extensions:**
- **4a. Input validation rule violation**
  - 4a1. The system alerts the student that an input validation rule is violated and displays the nature and location of the error.
  - 4a2. The student corrects the mistake and returns to step 4 of the normal flow.
- **5a. The system finds possible duplicates from the existing use cases**
  - 5a1. The system alerts the student that the use case she is trying to create already exists in the system.
  - 5a2. The student either chooses to correct the mistake and return to step 4 of the normal flow or chooses to terminate the use case.

**Priority:** High
**Frequency of Use:** Varies by team and project phase; most frequent during active authoring and near deadlines.
**Business Rules:** BR-1, BR-2 (team-scoped access control); BR-7 (a use case name is unique within the team's Use Cases document).
**Associated Information:**

Details:
- use case Name
- Primary and Secondary Actors
- Trigger
- Description
- Preconditions
- Postconditions
- Main Success Scenario/Normal Flow
- Extensions
- Priority
- Frequency of Use
- Business Rules
- Associated Information
- Assumptions
- Open Issues

Duplication detection rules: use case name must be unique

Notification: None — routine authoring changes are not notified (FR-NOT-2); saved changes propagate to connected teammates in real time (UC-COL-1).

The student shall be able to cancel the use case at any time prior to submitting it.

**Related Use Cases:** The student may first choose to UC-DOC-3: View the Use Cases document but cannot find the use case, then decide to create one.
**Assumptions:**
**Open Issues:**

### **UC-DOC-6: The Student edits a use case**

**UC ID and Name:** UC-DOC-6: Edit a use case
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:**
**Trigger:** The student indicates to edit the details of an existing use case.
**Description:** The student wants to edit the details of an existing use case so that the use case remains accurate and up-to-date.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the Use Cases document.
- PRE-3. The document is not locked for review.

**Postconditions:**
- POST-1. Changes made to the use case are stored in the system.
- POST-2. The system records authorship metadata (e.g., last-modified timestamp and editor identity).

**Main Success Scenario:**
1. The student indicates to change the details of an existing use case.
2. The student views the details of this use case through UC-DOC-4: View a use case.
3. The student chooses to change the details of this use case.
4. The system acquires an exclusive lock on this use case for the student.
5. The system asks the student to make changes to this use case where allowed according to the "Details" defined in the Associated Information and the "Security/access concerns" defined in the business rules of this use case.
6. The student makes changes to this use case until she confirms that she has finished changing.
7. The system validates the student's changes and alerts warning messages according to the "Details" defined in the Associated Information of this use case.
8. The student acknowledges the warnings and chooses to continue.
9. The system displays the updated details of this use case and alerts the student to confirm the change.
10. The student either confirms the change (continues the normal flow) or chooses to continue to change the details (return to step 6).
11. The system saves the changes, carries out the effect of change according to the "Details" defined in the Associated Information of this use case, and informs the student that this use case has been changed.
12. The system releases the lock on the use case.
13. Use case ends.

**Extensions:**
- **4a. use case already locked by another team member**
  - 4a1. The system alerts the student that another team member is currently editing this use case and does not grant the lock.
  - 4a2. The student either waits and returns to step 3 of the normal flow or chooses to terminate the use case.
- **7a. Input validation rule violation**
  - 7a1. The system alerts the student that an input validation rule is violated and displays the nature and location of the error.
  - 7a2. The student corrects the mistake and returns to step 7 of the normal flow.

**Priority:** High
**Frequency of Use:** Varies by team and project phase; most frequent during active authoring and near deadlines.
**Business Rules:** BR-1, BR-2 (team-scoped access control); BR-9 (editing a use case requires an exclusive lock).
**Associated Information:**

Details:
- use case Name
- Primary and Secondary Actors
- Trigger
- Description
- Preconditions
- Postconditions
- Main Success Scenario/Normal Flow
- Extensions
- Priority
- Frequency of Use
- Business Rules
- Associated Information
- Assumptions
- Open Issues

Notification: None — routine authoring changes are not notified (FR-NOT-2); saved changes propagate to connected teammates in real time (UC-COL-1).

The student shall be able to cancel the use case at any time prior to submitting it.

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

## **Requirement Artifacts**

### **UC-ART-1: The Student finds requirement artifacts**

**UC ID and Name:** UC-ART-1: Find requirement artifacts
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:**
**Trigger:** The student indicates to find requirement artifacts.
**Description:** The student wants to find requirement artifacts by keyword, artifact type, and artifact key so that she can quickly locate relevant content.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the requirement artifacts.

**Postconditions:**
- POST-1. A list of matching requirement artifacts is returned and displayed to the student. It is possible that the list is empty.

**Main Success Scenario:**
1. The student indicates to find requirement artifacts.
2. The system asks the student to enter search values according to the "Search criteria" defined in the Associated Information of this use case.
3. The student enters one or more search values and confirms that she has finished entering.
4. The system finds all requirement artifacts that match the provided search criteria values.
5. The system displays the matching requirement artifacts according to the "Search results display strategy" and the "Sort criteria" defined in the Associated Information of this use case.
6. Use case ends.

**Extensions:**
- **4a. No matching requirement artifacts are found**
  - 4a1. The system alerts the student that no matching requirement artifacts are found.
  - 4a2. The student either chooses to UC-ART-3: create a requirement artifact or chooses to terminate the use case or chooses to return to step 2 of the normal flow.
- **5a. The student chooses to select a different set of properties to display the matching requirement artifacts**
  - 5a1. The system displays the current "Search results display strategy."
  - 5a2. The student enters a customized "Search results display strategy," confirms that she has finished entering, and returns to step 5 of the normal flow.
- **5b. The student chooses to re-sort the search results**
  - 5b1. The student re-sorts the search result according to the "Sort criteria" defined in the Associated Information of this use case and returns to step 5 of the normal flow.

**Priority:** High
**Frequency of Use:** Varies by team and project phase; most frequent during active authoring and near deadlines.
**Business Rules:** BR-1, BR-2 (team-scoped access control).
**Associated Information:**

Search criteria (aka search fields, search attributes/properties, search details, searchable qualities):

| Search property name | Data type | Options | Validation rule | Security/access concerns | Reference to glossary |
| ---- | ---- | ---- | ---- | ---- | ---- |
| Keyword | String (free text) | — | Optional; trimmed; matched case-insensitively as a fuzzy match across the artifact's content and Artifact Key | Team-scoped (BR-1, BR-2) | Requirement Artifact |
| Artifact key | String | — | Optional; matched case-insensitively (e.g., FR-1, UC-3) | Team-scoped (BR-1, BR-2) | Artifact Key |
| Artifact content | String (free text) | — | Optional; fuzzy match against the artifact's body content | Team-scoped (BR-1, BR-2) | Requirement Artifact |
| Type | Enumeration | The artifact types defined by the Artifact Type glossary term | Optional; must be one of the defined artifact types | Team-scoped (BR-1, BR-2) | Artifact Type |

All search properties are optional; the student may supply any combination, and the system returns the artifacts that match every supplied property. The system shall support fuzzy search.

Search results display strategy (specify which properties to display for each matching requirement artifact): Artifact Key, Type, a content excerpt, and last modified by and date.

Sort criteria: default ascending by Artifact Key; the student may re-sort by Type or by last-modified date.

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

### **UC-ART-2: The Student views a requirement artifact**

**UC ID and Name:** UC-ART-2: View a requirement artifact
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:**
**Trigger:** The student indicates to view the details of a requirement artifact.
**Description:** The student wants to view the details of a requirement artifact so that she can better understand the project requirements.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the requirement artifact.

**Postconditions:**
- POST-1. The details of the specified requirement artifact are displayed to the student.

**Main Success Scenario:**
1. The student indicates to view the details of a requirement artifact.
2. The student finds a list of requirement artifacts through UC-ART-1: Find requirement artifacts.
3. The student views the list and chooses to view the details of one specific requirement artifact.
4. The system retrieves and displays the details of this requirement artifact according to the "Details" defined in the Associated Information and the "Security/access concerns" defined in the business rules of this use case.
5. The student views the details of this requirement artifact.
6. Use case ends.

**Extensions:**

**Priority:** High
**Frequency of Use:** Varies by team and project phase; most frequent during active authoring and near deadlines.
**Business Rules:** BR-1, BR-2 (team-scoped access control).
**Associated Information:**
- Artifact key.
- Artifact type.
- Artifact content.
- Metadata: priority, status.
- Notes.
- All requirement artifact links belonging to this artifact.
- For a glossary term:
  - Term
  - Definition
  - Created by and date
  - Last modified by and date
- For a use case:
  - use case ID and Name
  - Author and Date Created
  - Primary and Secondary Actors
  - Trigger
  - Description
  - Preconditions
  - Postconditions
  - Main Success Scenario/Normal Flow
  - Extensions
  - Priority
  - Frequency of Use
  - Business Rules
  - Associated Information
  - Assumptions
  - Open Issues

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

### **UC-ART-3: The Student creates a requirement artifact**

**UC ID and Name:** UC-ART-3: Create a requirement artifact
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:**
**Trigger:** The student, while viewing a list-type document section, indicates to add a requirement artifact to it.
**Description:** The student wants to create a new requirement artifact (for example, a business objective, risk, or feature) and add it to the requirements graph. In the MVP she does this from within the document section that holds artifacts of that kind — she navigates to the section (e.g., business objectives) and adds the artifact there, so the section determines both the artifact's placement and its artifact type.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the requirement document.
- PRE-3. The document is not locked for review.
- PRE-4. The student is viewing the list-type document section to which the artifact will belong.

**Postconditions:**
- POST-1. The new requirement artifact is stored in the system.
- POST-2. The artifact appears in the requirements graph.
- POST-3. The artifact appears in the document section it was created in (and in any other document view its type maps to).
- POST-4. The system records authorship metadata (e.g., last-modified timestamp and editor identity).

**Main Success Scenario:**
1. The student, viewing a list-type document section, indicates to add a requirement artifact to it.
2. The system asks the student to enter the details of the new artifact according to the "Details" defined in the Associated Information of this use case; because the document section determines the artifact's type, the student enters its content, metadata, and notes rather than choosing a type.
3. The student enters the details of this new requirement artifact and confirms that she has finished.
4. The system validates the student's inputs according to the "Details" defined in the Associated Information of this use case.
5. The system checks whether an artifact of the same type with similar content already exists, according to the "Duplication detection rules" defined in the Associated Information of this use case.
6. The system displays the details of the new requirement artifact and asks the student to confirm the creation.
7. The student either confirms the creation (continues the normal flow) or chooses to modify the details (returns to step 3).
8. The system saves the new requirement artifact, adds it to the requirements graph, and informs the student that this requirement artifact has been created.
9. The system places the new artifact in the document section it was created in (and in any other document view its type maps to).
10. Use case ends.

**Extensions:**
- **4a. Input validation rule violation**
  - 4a1. The system alerts the student that an input validation rule is violated and displays the nature and location of the error.
  - 4a2. The student corrects the mistake and returns to step 4 of the normal flow.
- **5a. The system finds a possible duplicate (an existing artifact of the same type with similar content)**
  - 5a1. The system warns the student and shows the possible duplicate(s).
  - 5a2. The student may open the existing artifact instead of creating a new one, revise her input and return to step 4, or proceed with the creation if the new artifact is genuinely distinct (continue at step 6). The warning is advisory and does not block creation.

**Priority:** High
**Frequency of Use:** Varies by team and project phase; most frequent during active authoring and near deadlines.
**Business Rules:** BR-1, BR-2 (team-scoped access control); BR-5 (unique, stable artifact key). The document section the artifact is created in determines its placement and its artifact type.
**Associated Information:**

Details:

| Property name | Data type | Editability | Validation rule | Effect of change | Reference to glossary |
| ---- | ---- | ---- | ---- | ---- | ---- |
| Requirement artifact type |  |  |  |  |  |
| Content |  |  |  |  |  |
| Links |  |  |  |  |  |
| Metadata: priority, status |  |  |  |  |  |
| Notes |  |  |  |  |  |

*Column "Effect of change" shows consequences of modification other than saving.*

The artifact's type is set by the document section it is created in; the student does not choose it in the MVP.

Placement:
- MVP: the artifact is created in the document section the student is currently viewing; that section determines its placement and artifact type.
- Future (requirements graph view): when the student adds an artifact directly in the graph — which shows all artifacts independent of document sections — there is no section context, so she selects the destination section, or the system places it by artifact type (the artifact type → authoring-home map in the SRS's [Business Domain Model](software-requirements-specification.md#business-domain-model)).

Duplication detection rules: Advisory only — the system warns when an artifact of the same type has similar content (fuzzy match) but does not block creation; the student decides whether to proceed, revise, or open the existing artifact.

Notification: None — routine authoring changes are not notified (FR-NOT-2); saved changes propagate to connected teammates in real time (UC-COL-1).

The student shall be able to cancel the use case at any time prior to submitting it.

**Related Use Cases:** The student may first choose to UC-ART-1: Find requirement artifacts but cannot find any, then decide to create one.
**Assumptions:**
**Open Issues:**

### **UC-ART-4: The Student creates a requirement artifact from document content (promote selection)**

**UC ID and Name:** UC-ART-4: Create a requirement artifact from document content (promote selection)
**Status:** Tabled — future release

The student selects text within a requirement document and promotes it to a structured requirement artifact of a chosen type, enabling traceability, navigation, and consistent reuse across documents.

For the MVP, the "Promote Selection to requirement artifact" use case is tabled.

Reason:
Core requirement artifacts (e.g., business objectives, risks, features, functional requirements) are created explicitly through structured Create \<Artifact Type\> actions, not written as free text. Since students do not author these elements in unstructured prose, promotion provides limited value in the MVP.

Note:
This use case may be revisited in future versions if free-text authoring or refactoring workflows are introduced.

### **UC-ART-5: The Student edits a requirement artifact**

**UC ID and Name:** UC-ART-5: Edit a requirement artifact
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:**
**Trigger:** The student indicates to edit the details of an existing requirement artifact.
**Description:** The student wants to edit the details of an existing requirement artifact so that the requirements model remains accurate and up-to-date.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the requirement artifact.
- PRE-3. The requirement artifact is not locked for review.

**Postconditions:**
- POST-1. Changes made to the requirement artifact are stored in the system.
- POST-2. The system records authorship metadata (e.g., last-modified timestamp and editor identity).

**Main Success Scenario:**
1. The student indicates to change the details of an existing requirement artifact.
2. The student views the details of this requirement artifact through UC-ART-2: View a requirement artifact.
3. The student chooses to change the details of this requirement artifact.
4. The system asks the student to make changes to this requirement artifact where allowed according to the "Details" defined in the Associated Information and the "Security/access concerns" defined in the business rules of this use case.
5. The student makes changes to this requirement artifact until she confirms that she has finished changing.
6. The system validates the student's changes and alerts warning messages according to the "Details" defined in the Associated Information of this use case.
7. The student acknowledges the warnings and chooses to continue.
8. The system displays the updated details of this requirement artifact and alerts the student to confirm the change.
9. The student either confirms the change (continues the normal flow) or chooses to continue to change the details (return to step 5).
10. The system saves the changes, carries out the effect of change according to the "Details" defined in the Associated Information of this use case, and informs the student that this requirement artifact has been changed.
11. Use case ends.

**Extensions:**
- **6a. Input validation rule violation**
  - 6a1. The system alerts the student that an input validation rule is violated and displays the nature and location of the error.
  - 6a2. The student corrects the mistake and returns to step 6 of the normal flow.

**Priority:** High
**Frequency of Use:** Varies by team and project phase; most frequent during active authoring and near deadlines.
**Business Rules:** BR-1, BR-2 (team-scoped access control).
**Associated Information:**

Details:

| Property name | Data type | Editability | Validation rule | Effect of change | Warning | Reference to glossary |
| ---- | ---- | ---- | ---- | ---- | ---- | ---- |
| Requirement artifact type |  | No — fixed by the Document Section the artifact belongs to (MVP) |  |  |  |  |
| Content |  | Yes |  |  |  |  |
| Notes |  | Yes |  |  |  |  |
| Metadata: priority, status |  | Yes |  |  |  |  |

*Column "Effect of change" shows consequences of modification other than saving.*

Notification: None — routine authoring changes are not notified (FR-NOT-2); saved changes propagate to connected teammates in real time (UC-COL-1).

The student shall be able to cancel the use case at any time prior to submitting it.

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

### **UC-ART-6: The Student deletes a requirement artifact**

**UC ID and Name:** UC-ART-6: Delete a requirement artifact
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:**
**Trigger:** The student indicates to delete an existing requirement artifact.
**Description:** The student wants to delete an existing requirement artifact so that this artifact is no longer needed.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the requirement artifact.
- PRE-3. The requirement artifact is not locked for review.

**Postconditions:**
- POST-1. The requirement artifact is deleted from the system according to the "Deletion strategy" defined in the Associated Information of this use case.

**Main Success Scenario:**
1. The student indicates to delete an existing requirement artifact.
2. The student views the details of this requirement artifact through UC-ART-2: View a requirement artifact.
3. The student chooses to delete this requirement artifact.
4. The system validates that the deletion can be carried out according to the "Referring objects handling strategy" defined in the Associated Information of this use case.
5. The system alerts the student of the consequences of this deletion according to the "Referring objects handling strategy" defined in the Associated Information of this use case, warns the student about the deletion, and asks the student to confirm.
6. The student confirms the deletion.
7. The system deletes the requirement artifact according to the "Deletion strategy" and the "Referring objects handling strategy" defined in the Associated Information of this use case and alerts the student that this requirement artifact has been deleted.
8. Use case ends.

**Extensions:**
- **4a. Data integrity and deletion rule violation**
  - 4a1. The system alerts the student that a deletion rule is violated and displays the nature of the violation.
  - 4a2. The system prompts possible actions to resolve the violation.
  - 4a3. use case terminates.

**Priority:** High
**Frequency of Use:** Varies by team and project phase; most frequent during active authoring and near deadlines.
**Business Rules:** BR-1, BR-2 (team-scoped access control); BR-12 (a referenced artifact cannot be deleted; deletion is logical).
**Associated Information:**

Deletion strategy:
- Logical delete: The requirement artifact is marked as deleted and removed from active use.
- Deleted requirement artifacts are not visible to students and cannot be reused.
- Only privileged administrative users may view deleted requirement artifacts for audit purposes.

Referring objects handling strategy:
- Deletion is blocked if the requirement artifact is referenced by any requirement artifact.
- The student must remove or update references via other use cases before retrying deletion.

Notification: None — routine authoring changes are not notified (FR-NOT-2); saved changes propagate to connected teammates in real time (UC-COL-1).

The student shall be able to cancel the use case at any time prior to submitting it.

**Related Use Cases:** The student may first choose to UC-ART-2: View a requirement artifact, then decide to delete one.
**Assumptions:**
**Open Issues:**

## **Artifact Links and Tracing**

### **UC-LNK-1: The Student views requirement artifact links**

**UC ID and Name:** UC-LNK-1: View requirement artifact links
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:**
**Trigger:** The student indicates to view the artifact links of a requirement artifact.
**Description:** The student wants to view all the relationships of a requirement artifact so that she can better understand its upstream and downstream links.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the requirement artifact.

**Postconditions:**
- POST-1. The artifact's relationships are displayed to the student.

**Main Success Scenario:**
1. The student indicates to view the links of a requirement artifact.
2. The student finds a list of requirement artifacts through UC-ART-1: Find requirement artifacts.
3. The student views the list and chooses to view the links of one specific requirement artifact.
4. The system retrieves and displays the links of this requirement artifact according to the "Details" defined in the Associated Information and the "Security/access concerns" defined in the business rules of this use case.
5. The student views the links.
6. Use case ends.

**Extensions:**
- **5a. The student expands a displayed artifact to see its further links**
  - 5a1. The student selects a related artifact shown in step 4 and chooses to expand it.
  - 5a2. The system retrieves and displays that artifact's links one additional hop out (lazy expansion), grouped by relationship type and with directionality indicated as in step 4, and returns to step 5.
  - 5a3. The system does not re-expand an artifact already shown in the current view, so traversal stays finite even when the graph contains cycles; the student may keep expanding to any depth. For an ordered, end-to-end path along abstraction levels (rather than free exploration), the student uses UC-LNK-6: Trace a requirement across levels.

**Priority:** High
**Frequency of Use:** Varies by team and project phase; most frequent during active authoring and near deadlines.
**Business Rules:** BR-1, BR-2 (team-scoped access control).
**Associated Information:**
- Each link shall include the relationship type and the related requirement artifact.
- The system shall group upstream and downstream artifacts by relationship type.
- Relationships may be shown in list or simple graph form in the MVP.
- Directionality (upstream vs downstream) must be clearly indicated.
- The default view shows one hop (the artifact's immediate links). The student may expand any displayed artifact one hop at a time and may keep expanding to any depth — it is a graph, so multi-hop traversal is not artificially capped. The system loads each hop on demand (lazy expansion) rather than eagerly loading the whole graph, keeping the view responsive as it grows.
- An artifact already shown is not expanded again within the same view, so cycles do not cause loops or duplicate nodes and the displayed set stays finite.

**Related Use Cases:** UC-LNK-6: Trace a requirement across levels — for an ordered, end-to-end path along abstraction levels rather than free local exploration.
**Assumptions:**
**Open Issues:**

### **UC-LNK-2: The Student views a requirement artifact link**

**UC ID and Name:** UC-LNK-2: View a requirement artifact link
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:**
**Trigger:** The student indicates to view the details of an artifact link.
**Description:** The student wants to view the details of a single artifact link so that she can understand the type and direction of the relationship between the two connected requirement artifacts and navigate from one to the other.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the requirement artifact.

**Postconditions:**
- POST-1. The details of the specified link are displayed to the student.

**Main Success Scenario:**
1. The student indicates to view the details of a requirement artifact link.
2. The student finds a list of links through UC-LNK-1: View requirement artifact links.
3. The student views the list and chooses to view the details of one specific link.
4. The system retrieves and displays the details of this link according to the "Details" defined in the Associated Information and the "Security/access concerns" defined in the business rules of this use case.
5. The student views the details of this link.
6. Use case ends.

**Extensions:**

**Priority:** High
**Frequency of Use:** Varies by team and project phase; most frequent during active authoring and near deadlines.
**Business Rules:** BR-1, BR-2 (team-scoped access control).
**Associated Information:**

Details:
- relationship type
- the related requirement artifact
- notes
- created by and date
- last modified by and date

Relationships may be shown in list or simple graph form in the MVP.

Directionality (upstream vs downstream) must be clearly indicated.

**Related Use Cases:** UC-LNK-1: View requirement artifact links; UC-LNK-6: Trace a requirement across levels.
**Assumptions:**
**Open Issues:**

### **UC-LNK-3: The Student creates a requirement artifact link**

**UC ID and Name:** UC-LNK-3: Create a requirement artifact link
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:**
**Trigger:** The student indicates to create a requirement artifact link between two requirement artifacts.
**Description:** The student wants to create a requirement artifact link (e.g., DERIVES_FROM / REALIZES / REFERENCES) between two requirement artifacts so that the relationship between requirement artifacts can be recorded and managed.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns both requirement artifacts.
- PRE-3. The requirement artifacts are not locked for review.

**Postconditions:**
- POST-1. The new artifact link is stored in the system.
- POST-2. The system records authorship metadata (e.g., last-modified timestamp and editor identity).

**Main Success Scenario:**
1. The student indicates to create a requirement artifact link between two requirement artifacts.
2. The student views the details of one requirement artifact through UC-ART-2: View a requirement artifact.
3. The student chooses to create a requirement artifact link for this artifact.
4. The system prompts the student to select:
   - a target artifact
   - a relationship type (e.g., DERIVES_FROM, REALIZES, REFERENCES)
5. The student enters the optional link notes and confirms.
6. The system validates the relationship (e.g., allowed link type, no self-linking).
7. The system saves the new link and informs the student that this link has been created.
8. Use case ends.

**Extensions:**
- **6a. Input validation rule violation**
  - 6a1. The system alerts the student that the selected relationship is not allowed between the chosen artifact types.
  - 6a2. The student corrects the mistake and returns to step 4 of the normal flow.
- **6b. The system finds possible duplicates from the existing links**
  - 6b1. The system alerts the student that the link she is trying to create already exists in the system.
  - 6b2. The student either chooses to correct the mistake and return to step 4 of the normal flow or chooses to terminate the use case.

**Priority:** High
**Frequency of Use:** Varies by team and project phase; most frequent during active authoring and near deadlines.
**Business Rules:** BR-1, BR-2 (team-scoped access control); BR-8 (link uniqueness, no self-linking, allowed link types only).
**Associated Information:**

Link Types:
- DERIVES_FROM
- REALIZES
- REFERENCES
- IMPACTS
- MITIGATES
- MOTIVATES

The system validates the selected (source type, link type, target type) combination against the link-type compatibility matrix defined under BR-8 in [business-rules.md](business-rules.md) (step 6; extension 6a handles a violation).

Optionally, the student can enter a note for this link.

Duplication detection rules: Source, target artifact, and link type must be unique

Notification: None — routine authoring changes are not notified (FR-NOT-2); saved changes propagate to connected teammates in real time (UC-COL-1).

The student shall be able to cancel the use case at any time prior to submitting it.

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

### **UC-LNK-4: The Student edits a requirement artifact link**

**UC ID and Name:** UC-LNK-4: Edit a requirement artifact link
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:**
**Trigger:** The student indicates to edit a requirement artifact link between two requirement artifacts.
**Description:** The student wants to edit an existing requirement artifact link between two requirement artifacts so that the relationship between requirement artifacts can be updated.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns both requirement artifacts.
- PRE-3. The requirement artifacts are not locked for review.

**Postconditions:**
- POST-1. The updated artifact link is stored in the system.
- POST-2. The system records authorship metadata (e.g., last-modified timestamp and editor identity).

**Main Success Scenario:**
1. The student indicates to change the details of an existing link.
2. The student views the details of this link through UC-LNK-2: View a requirement artifact link.
3. The student chooses to change the details of this link.
4. The system asks the student to make changes to this link where allowed according to the "Details" defined in the Associated Information and the "Security/access concerns" defined in the business rules of this use case.
5. The student makes changes to this link until she confirms that she has finished changing.
6. The system validates the student's changes and alerts warning messages according to the "Details" defined in the Associated Information of this use case.
7. The student acknowledges the warnings and chooses to continue.
8. The system displays the updated details of this link and alerts the student to confirm the change.
9. The student either confirms the change (continues the normal flow) or chooses to continue to change the details (return to step 5).
10. The system saves the changes, carries out the effect of change according to the "Details" defined in the Associated Information of this use case, and informs the student that this link has been changed.
11. Use case ends.

**Extensions:**
- **6a. Input validation rule violation**
  - 6a1. The system alerts the student that the selected relationship is not allowed between the chosen artifact types.
  - 6a2. The student corrects the mistake and returns to step 4 of the normal flow.
- **6b. The system finds possible duplicates from the existing links**
  - 6b1. The system alerts the student that the updated link would duplicate an existing link.
  - 6b2. The student either chooses to correct the mistake and return to step 4 of the normal flow or chooses to terminate the use case.

**Priority:** High
**Frequency of Use:** Varies by team and project phase; most frequent during active authoring and near deadlines.
**Business Rules:** BR-1, BR-2 (team-scoped access control); BR-8 (link uniqueness, no self-linking, allowed link types only).
**Associated Information:**

The student may update the target artifact, link note, and link types:
- DERIVES_FROM
- REALIZES
- REFERENCES
- IMPACTS
- MITIGATES
- MOTIVATES

A changed target artifact or link type is validated against the link-type compatibility matrix defined under BR-8 in [business-rules.md](business-rules.md) (step 6; extension 6a handles a violation).

Duplication detection rules: Source, target artifact, and link type must be unique

Notification: None — routine authoring changes are not notified (FR-NOT-2); saved changes propagate to connected teammates in real time (UC-COL-1).

The student shall be able to cancel the use case at any time prior to submitting it.

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

### **UC-LNK-5: The Student deletes a requirement artifact link**

**UC ID and Name:** UC-LNK-5: Delete a requirement artifact link
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:**
**Trigger:** The student indicates to delete an existing link.
**Description:** The student wants to delete an existing link so that a relationship that no longer holds is removed from the requirements graph.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns both requirement artifacts.
- PRE-3. The requirement artifacts are not locked for review.

**Postconditions:**
- POST-1. The link is permanently deleted from the system; both endpoint artifacts remain unchanged.

**Main Success Scenario:**
1. The student indicates to delete an existing link.
2. The student views the details of this link through UC-LNK-2: View a requirement artifact link.
3. The student chooses to delete this link.
4. The system warns the student that the link will be permanently removed and asks her to confirm.
5. The student confirms the deletion.
6. The system deletes the link, leaving both endpoint artifacts unchanged, and informs the student that the link has been deleted.
7. Use case ends.

**Extensions:**

**Priority:** High
**Frequency of Use:** Varies by team and project phase; most frequent during active authoring and near deadlines.
**Business Rules:** BR-1, BR-2 (team-scoped access control).
**Associated Information:**

Deletion strategy:
- Hard delete: the link (a graph edge) is permanently removed. The two endpoint requirement artifacts are unaffected, and no other artifact references a link, so there are no referring objects to reconcile.

Notification: None — routine authoring changes are not notified (FR-NOT-2); saved changes propagate to connected teammates in real time (UC-COL-1).

The student shall be able to cancel the use case at any time prior to submitting it.

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

### **UC-LNK-6: The Student traces a requirement across levels**

**UC ID and Name:** UC-LNK-6: Trace a requirement across levels
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:**
**Trigger:** The student indicates to trace a requirement across multiple abstraction levels.
**Description:** The student wants to trace a requirement from a high-level objective through intermediate artifacts (such as features and use cases) down to functional requirements so that she can understand requirement coverage, rationale, and completeness.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the requirement artifact.

**Postconditions:**
- POST-1. The traced requirement path is displayed to the student.

**Main Success Scenario:**
1. The student indicates to trace a requirement across multiple abstraction levels.
2. The student finds a list of high-level requirement artifacts through UC-ART-1: Find requirement artifacts, e.g., business objectives.
3. The student views the list and chooses to trace one specific requirement artifact.
4. The system retrieves downstream artifacts linked to the selected artifact (e.g., Features).
5. The system displays the next-level artifacts in the trace path.
6. The student selects one of the displayed artifacts to continue tracing.
7. The system retrieves and displays the next-level artifacts (e.g., use cases).
8. The student continues navigation until reaching functional requirements.
9. The system displays the completed trace path (Objective → Feature → use case → functional requirement).
10. Use case ends.

**Extensions:**
- **2a. Start trace from a lower-level requirement artifact**
  - 2a1. The system retrieves and displays upstream artifacts.
  - 2a2. The trace proceeds in reverse order.

**Priority:** High
**Frequency of Use:** Varies by team and project phase; most frequent during active authoring and near deadlines.
**Business Rules:** BR-1, BR-2 (team-scoped access control).
**Associated Information:**
- The system shall display all available trace paths if multiple trace paths exist.
- Trace paths may be displayed as an ordered list or simple graph.
- Tracing is lazy and incremental: the system loads each next level on demand as the student advances (steps 4–8) rather than precomputing the full graph, and it does not re-expand an artifact already on the current path, so cycles terminate. The trace spans as many levels as the path requires (e.g., Objective → Feature → use case → functional requirement).

**Related Use Cases:** UC-LNK-1: View requirement artifact links — free, any-direction, any-depth exploration of an artifact's links. This use case instead follows an ordered, goal-driven path along abstraction levels (e.g., Objective → Feature → use case → functional requirement). Difference: UC-LNK-1 is free exploration; UC-LNK-6 is an ordered end-to-end path.
**Assumptions:**
**Open Issues:**

## **Validation**

### **UC-VAL-1: The Student runs validation (ReqLint) on the current document**

**UC ID and Name:** UC-VAL-1: Run validation on a requirement document
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:**
**Trigger:** The student indicates to run ReqLint validation on the currently selected requirement document.
**Description:** The student wants to run ReqLint on a single requirement document to detect structural issues, missing required content, naming violations, and other deterministic rule violations. The system reports validation results as errors and warnings tied to specific document sections or items.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the requirement document.

**Postconditions:**
- POST-1. Validation results for the selected document are generated and displayed.

**Main Success Scenario:**
1. The student indicates to run ReqLint validation on the currently selected requirement document.
2. The system runs document-level ReqLint checks (e.g., required document sections completed, formatting rules).
3. The system generates a list of validation issues with severity (ERROR/WARNING/INFO).
4. The system displays validation issues grouped by document section and rule category.
5. The student selects an issue to view details.
6. The system navigates the student to the relevant document section/item and highlights the location.
7. Use case ends.

**Extensions:**
- **2a. ReqLint validation cannot be completed**
  - 2a1. The system is unable to finish the validation run (for example, a processing error in the ReqLint engine).
  - 2a2. The system informs the student that validation did not complete and that no results are available, and invites the student to run it again; the student returns to step 1.
- **3a. The document has no validation issues**
  - 3a1. The document-level ReqLint checks produce no errors, warnings, or info findings.
  - 3a2. The system reports that the document passed validation and displays an empty issue list — a clean result is a successful outcome, distinct from validation never having been run. Use case ends.
- **6a. The student corrects an issue and re-runs validation**
  - 6a1. From the highlighted location, the student edits the affected document section or use case to resolve the finding (UC-DOC-2: Edit a document section, or UC-DOC-6: Edit a use case).
  - 6a2. The student re-runs ReqLint and returns to step 1; the system regenerates the issue list, which reflects the correction.

**Priority:** High
**Frequency of Use:** Varies by team and project phase; most frequent during active authoring and near deadlines.
**Business Rules:**
**Associated Information:**

Details (Examples of Document-Level Checks):
- Required document section missing or empty
- Required fields missing for a document section/item
- Naming convention violations (e.g., missing artifact keys where required)
- Required "shall" structure for functional requirements (if applicable)
- use case step format rules (if applicable)

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

## **Collaboration**

### **UC-COL-1: The Student collaboratively edits a requirement document**

*Post-MVP — this real-time collaborative-editing session is deferred beyond the initial release (see the SRS Real-Time Collaboration Requirements). In the MVP, teammates author the same document through per-section locking (UC-DOC-2, UC-DOC-6) and coordinate via comment threads (UC-COL-2, UC-COL-3).*

**UC ID and Name:** UC-COL-1: Collaboratively edit a requirement document
**Status:** Deferred — post-MVP
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:** Other students on the same team
**Trigger:** The student opens a requirement document that one or more teammates are also connected to.
**Description:** The student wants to author a requirement document at the same time as her teammates so that the team can work together in real time — seeing who else is present, watching each other's changes appear live, and being kept from overwriting one another's work. This use case covers the shared editing *session*; the act of editing an individual document section or use case is performed through UC-DOC-2 and UC-DOC-6, which acquire and release the locks.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the requirement document.
- PRE-3. The document is not locked for review.

**Postconditions:**
- POST-1. All connected team members see a consistent, up-to-date view of the document.
- POST-2. No collaborator's saved changes are overwritten or corrupted by another collaborator.

**Main Success Scenario:**
1. The student opens a requirement document through UC-DOC-1: View a section-based requirement document (or UC-DOC-3: View the Use Cases document).
2. The system displays which teammates are currently connected to the document and which document sections or use cases are currently locked, and by whom.
3. The system notifies all connected team members that the student has joined the document.
4. The student edits a document section through UC-DOC-2: Edit a section-based requirement document, or a use case through UC-DOC-6: Edit a use case.
5. As the student acquires a lock and saves changes (within UC-DOC-2 / UC-DOC-6), the system broadcasts the updated lock state and the saved content to all connected team members in real time.
6. The system reflects teammates' presence, lock changes, and saved changes in the student's view as they occur.
7. The student leaves the document.
8. The system notifies all connected team members that the student has disconnected.
9. Use case ends.

**Extensions:**
- **4a. The student attempts to edit a document section or use case that is locked by a teammate**
  - 4a1. UC-DOC-2 / UC-DOC-6 handle this: the system does not grant the lock, and the student chooses a different target or waits.
- **6a. A teammate joins or disconnects during the session**
  - 6a1. The system updates the displayed presence and lock state for the student and returns to step 4 of the normal flow.

**Priority:** High
**Frequency of Use:** Very frequent: multiple times per student per working session; peaks near deadlines.
**Business Rules:** BR-9, BR-11 — Editing and locking of individual document sections and use cases are governed by UC-DOC-2 and UC-DOC-6. The system shall ensure that real-time updates do not overwrite or corrupt content saved by other collaborators.
**Associated Information:**

Presence and synchronization:
- The system shall notify connected users when a collaborator joins or disconnects.
- The system shall broadcast lock acquisition and release to all team members viewing the document.
- The system shall ensure that real-time updates do not overwrite or corrupt content saved by other collaborators.

**Related Use Cases:** UC-DOC-2: Edit a section-based requirement document; UC-DOC-6: Edit a use case; UC-DOC-1: View a section-based requirement document; UC-DOC-3: View the Use Cases document.
**Assumptions:**
**Open Issues:**

### **UC-COL-2: The Student comments on a requirement document**

**UC ID and Name:** UC-COL-2: Comment on a requirement document
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:**
**Trigger:** The student indicates to add a comment while viewing a requirement document.
**Description:** The student wants to add a new comment to a requirement document, or a specific document section, or requirement artifact so that she can review content, ask questions, suggest improvements, or discuss issues with teammates without directly modifying the requirement content.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the document or artifact (or, for review feedback, the instructor assigned to the course section — see UC-REV-2).

**Postconditions:**
- POST-1. The new comment is stored in the system with status OPEN (unresolved).

**Main Success Scenario:**
1. The student indicates to add a comment while viewing a requirement document.
2. The student selects a document, or a document section, or artifact.
3. The system asks the student to enter the details of this new comment according to the "Details" defined in the Associated Information of this use case.
4. The student enters the details of this new comment and confirms that she has finished.
5. The system validates the student's inputs according to the "Details" defined in the Associated Information of this use case.
6. The system displays the details of the new comment and asks the student to confirm the creation.
7. The student either confirms the creation (continues the normal flow) or chooses to modify the details (returns to step 3).
8. The system saves the new comment and informs the student that this comment has been created.
9. Use case ends.

**Extensions:**
- **5a. Input validation rule violation**
  - 5a1. The system alerts the student that an input validation rule is violated and displays the nature and location of the error.
  - 5a2. The student corrects the mistake and returns to step 4 of the normal flow.

**Priority:** High
**Frequency of Use:** Varies by team and project phase; most frequent during active authoring and near deadlines.
**Business Rules:** BR-1, BR-2 (team-scoped access control); BR-20 (comments may be added by team students and the course section's instructor, regardless of the document's review-lock state).
**Associated Information:**

Details: Comments may be attached to document, document sections, and artifacts.

Notification: None — per the project's notification policy (FR-NOT-2), creating a comment does not raise a notification; the comment is visible in the document, document section, or artifact view where it is attached, and appears to connected teammates in real time (UC-COL-1).

The student shall be able to cancel the use case at any time prior to submitting it.

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

### **UC-COL-3: The Student resolves a comment**

**UC ID and Name:** UC-COL-3: Resolve a comment
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:**
**Trigger:** The student indicates to resolve a comment.
**Description:** The student wants to resolve a comment so that she can indicate that the issue has been addressed or the discussion is concluded.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the comment (or, for review feedback, the instructor assigned to the course section — see UC-REV-2).

**Postconditions:**
- POST-1. The comment is marked as resolved in the system.

**Main Success Scenario:**
1. The student indicates to resolve a comment.
2. The student finds a list of comments through UC-DOC-1: View a section-based requirement document or UC-ART-2: View a requirement artifact.
3. The student views the list and chooses to resolve one specific comment.
4. The system prompts the student to enter a resolution note.
5. The student confirms resolution.
6. The system marks the comment as resolved.
7. The system updates the comment display to show it is resolved.
8. Use case ends.

**Extensions:**

**Priority:** High
**Frequency of Use:** Varies by team and project phase; most frequent during active authoring and near deadlines.
**Business Rules:** BR-1, BR-2 (team-scoped access control); BR-20 (comments may be resolved by team students and the course section's instructor, regardless of the document's review-lock state).
**Associated Information:**
**Related Use Cases:**
**Assumptions:**
**Open Issues:**

## **Review and Submission**

### **UC-REV-1: The Student submits requirements for review**

**UC ID and Name:** UC-REV-1: Submit requirements for review
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:** instructor
**Trigger:** The student indicates to submit a requirement document for instructor review.
**Description:** The student wants to submit a requirement document for review so that the instructor can evaluate the team's work and the document is frozen against further student edits while it is under review.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the requirement document.
- PRE-3. The document is not already locked for review.

**Postconditions:**
- POST-1. The document is locked for review and becomes read-only for students.
- POST-2. The instructor of the course section is notified that the document has been submitted.

**Main Success Scenario:**
1. The student indicates to submit a requirement document for review.
2. The system displays the document to be submitted and asks the student to confirm the submission.
3. The student confirms the submission.
4. The system locks the document for review, making it read-only for all students on the team.
5. The system records submission metadata (e.g., submission timestamp and submitting student identity).
6. The system notifies the instructor of the course section that the document has been submitted for review.
7. The system informs the student that the document has been submitted.
8. Use case ends.

**Extensions:**
- **3a. The student cancels the submission**
  - 3a1. The student chooses not to confirm the submission and terminates the use case; the document remains editable.
- **4a. A teammate is currently editing the document**
  - 4a1. The system alerts the student that one or more teammates are still editing the document.
  - 4a2. The student either waits and returns to step 3 of the normal flow or chooses to terminate the use case.

**Priority:** High
**Frequency of Use:** Around each project review milestone.
**Business Rules:** BR-13 — Once a document is locked for review, students may no longer edit it until the instructor returns it for revision (see UC-REV-2).
**Associated Information:**

Notification: Notify the course section's instructor on submission, delivered by email via the Gmail SMTP integration (FR-NOT-1).

**Related Use Cases:** UC-REV-2: The instructor reviews a team's requirement documents.
**Assumptions:**
**Open Issues:**

### **UC-REV-2: The Instructor reviews a team's requirement documents**

**UC ID and Name:** UC-REV-2: Review a team's requirement documents
**Created By:**
**Date Created:**
**Primary Actor:** instructor
**Secondary Actors:** student
**Trigger:** The instructor indicates to review a team's submitted requirement documents.
**Description:** The instructor wants to review a team's submitted requirement documents so that she can provide feedback and either accept the work or return it to the team for revision.

**Preconditions:**
- PRE-1. The instructor is logged into the system.
- PRE-2. The instructor is assigned to the course section that contains the team.
- PRE-3. The document has been submitted and is locked for review.

**Postconditions:**
- POST-1. The instructor's feedback is stored in the system.
- POST-2. The document is either accepted (status ACCEPTED, kept read-only) or returned for revision (status RETURNED, unlocked and made editable for students again).
- POST-3. The team is notified of the review outcome.

**Main Success Scenario:**
1. The instructor indicates to review a team's requirement documents.
2. The system displays the team's submitted documents.
3. The instructor reads the documents and adds feedback through UC-COL-2: Comment on a requirement document.
4. The instructor chooses to return the document for revision.
5. The system unlocks the document, making it editable for students again.
6. The system notifies the team that the document has been returned for revision.
7. Use case ends.

**Extensions:**
- **4a. The instructor accepts the document**
  - 4a1. The instructor marks the document as accepted instead of returning it.
  - 4a2. The system sets the document status to ACCEPTED, keeps it read-only (it is not returned for editing), records the acceptance, notifies the team, and the use case ends.

**Priority:** High
**Frequency of Use:** Around each project review milestone.
**Business Rules:** BR-14 — Only an instructor assigned to the course section may review, accept, or return a submitted document. Returning a document for revision unlocks it for student editing.
**Associated Information:**

Notification: Notify the team's students of the review outcome (returned for revision or accepted), delivered by email via the Gmail SMTP integration (FR-NOT-1).

**Related Use Cases:** UC-COL-2: Comment on a requirement document; UC-REV-1: The student submits requirements for review.
**Assumptions:**
**Open Issues:**

## **Export**

### **UC-EXP-1: The Student exports a requirement document**

**UC ID and Name:** UC-EXP-1: Export a requirement document
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:**
**Trigger:** The student indicates to export a requirement document.
**Description:** The student wants to export a requirement document to a selected output format (e.g., Word, PDF, Markdown) so that the document can be shared, submitted, or reviewed outside the system while preserving structure and content.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the requirement document.

**Postconditions:**
- POST-1. An exported file representing the requirement document is generated successfully.
- POST-2. The generated file is made available for the student to download.

**Main Success Scenario:**
1. The student indicates to export a requirement document.
2. The system displays available export formats (e.g., Word, PDF, Markdown).
3. The student selects an export format and confirms.
4. The system generates the exported document using the selected format and template.
5. The system preserves document structure, document section ordering, and content.
6. The system embeds requirement identifiers (artifact keys) where applicable.
7. The system provides the generated file for download.
8. Use case ends.

**Extensions:**

**Priority:** High
**Frequency of Use:** Varies by team and project phase; most frequent during active authoring and near deadlines.
**Business Rules:**
**Associated Information:**

Details:

Supported formats may include:
- Word (.docx)
- PDF
- Markdown (.md)

Formatting is deterministic and derived from the built-in document template.

Markdown export prioritizes structure and traceability over visual styling.

Exported documents must preserve template-defined structure and document section ordering.

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

### **UC-EXP-2: The Student exports all the requirement documents as a bundle**

**UC ID and Name:** UC-EXP-2: Export all requirement documents as a bundle
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:**
**Trigger:** The student indicates to export all requirement documents for the team as a single bundle.
**Description:** The student wants to export all requirement documents associated with a team as a bundled package in a selected output format (or formats) so that the complete requirements specification can be delivered, archived, or submitted as a single unit.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the requirement documents.

**Postconditions:**
- POST-1. A bundled export containing all requirement documents is generated successfully.
- POST-2. The bundled file is made available for the student to download.

**Main Success Scenario:**
1. The student indicates to export all requirement documents for the team as a single bundle.
2. The system displays available export formats (e.g., Word, PDF, Markdown).
3. The student selects a bundle format and confirms.
4. The system generates each requirement document using its corresponding export template.
5. The system packages the generated documents into a single bundled archive (e.g., ZIP or combined PDF).
6. The system preserves document ordering and naming according to the template definition.
7. The system provides the bundled file for download.
8. Use case ends.

**Extensions:**

**Priority:** High
**Frequency of Use:** Varies by team and project phase; most frequent during active authoring and near deadlines.
**Business Rules:**
**Associated Information:**

Details:

Bundle formats may include:
- ZIP archive containing multiple files
- Single combined PDF

Default document order follows the template-defined ordering (e.g., Vision and Scope → Glossary → Use Cases → Business Rules → SRS).

File names include document type and team identifier.

**Related Use Cases:**
**Assumptions:**
**Open Issues:**

## **AI Configuration**

### **UC-CFG-1: The Instructor configures the teaching context for a course section**

**UC ID and Name:** UC-CFG-1: Configure the teaching context for a course section
**Created By:**
**Date Created:**
**Primary Actor:** instructor
**Secondary Actors:**
**Trigger:** The instructor indicates to view or edit the teaching context for a course section she teaches.
**Description:** The instructor wants to author and maintain the teaching context for a course section — the instructor-authored context that encodes the standards students are graded against (e.g., INVEST, testability, Wiegers categories), common student mistakes, and the thinking order to follow — so that the course section's AI assistants draw on a deliberately designed curriculum. The teaching context is a first-class, instructor-controlled teaching artifact.

**Preconditions:**
- PRE-1. The instructor is logged into the system.
- PRE-2. The instructor is assigned to the course section.

**Postconditions:**
- POST-1. The course section's teaching context is updated and stored.
- POST-2. The course section's assistants use the updated teaching context on subsequent requests.

**Main Success Scenario:**
1. The instructor indicates to view the teaching context for a course section she teaches.
2. The system displays the current teaching context for the course section.
3. The instructor edits the teaching context content (e.g., standards, common-mistake guidance, and thinking order). Per-assistant behavior is configured separately through UC-CFG-3.
4. The system saves the updated teaching context and confirms.
5. Use case ends.

**Extensions:**

**Priority:** High
**Frequency of Use:** Occasional; at course setup and periodic refinement.
**Business Rules:** BR-4 — Only an instructor assigned to the course section may view or edit its teaching context. The teaching context is the context the course section's assistants draw on.
**Associated Information:**

- The teaching context is the curriculum: it encodes how requirements are taught and graded.
- Used by all of the course section's assistants.
- Realized by FR-AI-6.

**Related Use Cases:** UC-AI-2: Elicit requirements with the elicitation assistant; UC-AI-5: Request a critique from the critique assistant; UC-AI-6: Ask an assistant to explain a concept (Tutor Mode); UC-AI-3: Practice a client interview with a role-playing client assistant; UC-CFG-2: Enable or disable AI assistants for a course section; UC-CFG-3: Configure the assistant instructions for a course section.
**Assumptions:**
**Open Issues:**

### **UC-CFG-2: The Instructor enables or disables AI assistants for a course section**

**UC ID and Name:** UC-CFG-2: Enable or disable AI assistants for a course section
**Created By:**
**Date Created:**
**Primary Actor:** instructor
**Secondary Actors:**
**Trigger:** The instructor indicates to change which AI assistants are available for a course section she teaches.
**Description:** The instructor wants to enable or disable AI assistants — individually, per assistant — for the students in a course section she teaches, so that she controls whether and when each kind of AI help is permitted (for example, disabling the drafting assistant during a graded elicitation exercise while leaving the tutor assistant on). This setting governs the availability of the student-facing AI use cases (UC-AI-2, UC-AI-3, UC-AI-4, UC-AI-5, UC-AI-6, UC-AI-7, UC-AI-9, UC-AI-10) for that course section's teams.

**Preconditions:**
- PRE-1. The instructor is logged into the system.
- PRE-2. The instructor is assigned to the course section.

**Postconditions:**
- POST-1. The per-assistant enable/disable settings for the course section are updated and stored.
- POST-2. The change takes effect for students in the course section on subsequent requests.

**Main Success Scenario:**
1. The instructor indicates to change the AI assistant settings for a course section she teaches.
2. The system displays each AI assistant and its current enabled/disabled state for the course section.
3. The instructor enables or disables one or more assistants.
4. The system saves the updated settings and applies them.
5. The system informs the instructor that the settings have been updated.
6. Use case ends.

**Extensions:**

- **4a. A student is mid-session with an assistant that is being disabled**
  - 4a1. The system applies the change on the student's next assistant request and goes to step 5 of the normal flow.

**Priority:** High
**Frequency of Use:** Occasional; at course setup and around graded exercises.
**Business Rules:** BR-4, BR-15 — Only an instructor assigned to the course section may change its AI assistant settings. Assistants may be enabled or disabled individually. When an assistant is disabled, its corresponding student-facing use case is unavailable for that course section's students. The drafting assistant (UC-AI-7) shall be disabled by default.
**Associated Information:**
- Default state: the drafting assistant is off; other assistants' defaults are set at course setup.
- Realized by FR-AI-7.

**Related Use Cases:** UC-AI-2: Elicit requirements with the elicitation assistant; UC-AI-5: Request a critique from the critique assistant; UC-AI-7: Generate a draft requirement skeleton with an assistant; UC-AI-6: Ask an assistant to explain a concept (Tutor Mode); UC-AI-3: Practice a client interview with a role-playing client assistant; UC-AI-4: Turn meeting notes into structured requirements; UC-CFG-1: Configure the teaching context for a course section.
**Assumptions:**
**Open Issues:**

### **UC-CFG-3: The Instructor configures the assistant instructions for a course section**

**UC ID and Name:** UC-CFG-3: Configure the assistant instructions for a course section
**Created By:**
**Date Created:**
**Primary Actor:** instructor
**Secondary Actors:**
**Trigger:** The instructor indicates to view or edit the instructions for an AI assistant in a course section she teaches.
**Description:** The instructor wants to author and maintain the assistant instructions for each AI assistant in a course section she teaches — the per-assistant directives that govern how an assistant behaves: its role (what the assistant does), its persona and tone (for example, Socratic, patient, warm, like an experienced requirements engineer), and what it must avoid (for example, handing students finished requirements rather than eliciting their next draft). Whereas the teaching context (UC-CFG-1) supplies the course-section-wide standards every assistant is held to, assistant instructions are configured per assistant so the instructor can shape each assistant's behavior independently. Assistant instructions are a first-class, instructor-controlled teaching artifact.

**Preconditions:**
- PRE-1. The instructor is logged into the system.
- PRE-2. The instructor is assigned to the course section.

**Postconditions:**
- POST-1. The selected assistant's assistant instructions for the course section are updated and stored.
- POST-2. The assistant uses the updated assistant instructions on subsequent requests.

**Main Success Scenario:**
1. The instructor indicates to view the assistant instructions for a course section she teaches.
2. The system displays the available AI assistants and the current assistant instructions for each.
3. The instructor selects an assistant and edits its assistant instructions (its role, its persona and tone, and the behavior it must avoid).
4. The system saves the updated assistant instructions and confirms.
5. Use case ends.

**Extensions:**

**Priority:** High
**Frequency of Use:** Occasional; at course setup and periodic refinement.
**Business Rules:** BR-4 — Only an instructor assigned to the course section may view or edit its assistant instructions. Assistant instructions are authored per assistant and govern assistant behavior, while the teaching context governs the standards the assistant enforces.
**Associated Information:**
- Assistant instructions are the per-assistant behavioral layer; the teaching context (UC-CFG-1) is the shared standards layer the instructions reference.
- Applies to every assistant in the course section.
- Realized by FR-AI-16.

**Related Use Cases:** UC-CFG-1: Configure the teaching context for a course section; UC-CFG-2: Enable or disable AI assistants for a course section.
**Assumptions:**
**Open Issues:**

### **UC-CFG-4: The Instructor configures the cross-document review criteria for a course section**

**UC ID and Name:** UC-CFG-4: Configure the cross-document review criteria for a course section
**Created By:**
**Date Created:**
**Primary Actor:** instructor
**Secondary Actors:**
**Trigger:** The instructor indicates to view or edit the cross-document review criteria used by the critique assistant for a course section she teaches.
**Description:** The instructor wants to author and maintain the cross-document review criteria for a course section she teaches — the criteria the critique assistant applies when a student requests a whole-project review (UC-AI-10) of the team's entire set of requirement documents. The criteria define the dimensions and standards the whole-project review checks across documents (for example: every feature has a downstream use case and functional requirement; every cited glossary term, business rule, and FR resolves; scope statements agree across documents; no conflicts between documents). Whereas the teaching context (UC-CFG-1) supplies the course-section-wide, per-requirement quality standards and assistant instructions (UC-CFG-3) supply per-assistant behavior, the cross-document review criteria define what the whole-project review evaluates across the document set. They must be defined before the whole-project review is available to the course section's students. A course admin, who is also an instructor of her course, may perform this use case.

**Preconditions:**
- PRE-1. The instructor is logged into the system.
- PRE-2. The instructor is assigned to the course section.

**Postconditions:**
- POST-1. The course section's cross-document review criteria are updated and stored.
- POST-2. The critique assistant uses the updated criteria on subsequent whole-project reviews (UC-AI-10).

**Main Success Scenario:**
1. The instructor indicates to view the cross-document review criteria for a course section she teaches.
2. The system displays the current cross-document review criteria for the course section (or an indication that none are defined yet, optionally seeded from a system-provided default).
3. The instructor edits the criteria (the dimensions and standards the whole-project review checks across documents).
4. The system validates and saves the updated criteria and confirms.
5. Use case ends.

**Extensions:**
- **4a. The criteria are empty or invalid**
  - 4a1. The system alerts the instructor that the criteria cannot be saved as entered and does not store them; the instructor corrects the input and returns to step 3.

**Priority:** High
**Frequency of Use:** Occasional; at course setup and periodic refinement.
**Business Rules:** BR-4 — Only an instructor assigned to the course section (including a course admin of its course) may view or edit its cross-document review criteria.
**Associated Information:**
- The cross-document review criteria are a course-section-level teaching artifact, distinct from the teaching context (UC-CFG-1) and per-assistant assistant instructions (UC-CFG-3); they govern the critique assistant's whole-project review mode (UC-AI-10) specifically.
- While the criteria are undefined for a course section, the whole-project review is unavailable to that course section's students (FR-AI-23).
- The system-provided default the criteria are seeded from (main flow step 2) is maintained in `docs/product/cross-document-review-criteria.md` (the default criteria set + the critique assistant's whole-project-review system prompt).
- Realized by FR-AI-22; honors FR-AI-23.

**Related Use Cases:** UC-AI-10: Request a whole-project review from the critique assistant; UC-CFG-1: Configure the teaching context for a course section; UC-CFG-3: Configure the assistant instructions for a course section.
**Assumptions:**
**Open Issues:**

## **AI Assistants**

### **UC-AI-1: The Student imports client pitch materials as project source**

**UC ID and Name:** UC-AI-1: Import client pitch materials as project source
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:**
**Trigger:** The student indicates to import the client's pitch materials into the project.
**Description:** At project kickoff the client pitches the project with materials such as a slide deck and a short PDF covering background, stakeholders, problem statement, users, objectives, desired functionality, possible solutions, prototypes, and a candidate tech stack. The student imports these into RAM as project source material so that the team and the AI assistants can reference them while eliciting and authoring requirements. The materials are inputs the team works from — not authored requirement content.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the project.

**Postconditions:**
- POST-1. The imported materials are stored as project source material for the team.
- POST-2. The materials' content is available to the team and, where AI assistants are enabled, as context for those assistants.

**Main Success Scenario:**
1. The student indicates to import client pitch materials.
2. The system asks the student to provide the files (e.g., a slide deck and a PDF).
3. The student selects the files and confirms.
4. The system validates the files against the supported formats and size limits.
5. The system stores the files as project source material and extracts their text content so it can be referenced and supplied as context to the AI assistants.
6. The system confirms that the materials are available to the team.
7. Use case ends.

**Extensions:**
- **4a. A file is in an unsupported format or exceeds the size limit**
  - 4a1. The system informs the student which file was rejected and why, and the student provides a different file or terminates the use case.
- **5a. Text extraction is incomplete (e.g., an image-only or scanned PDF)**
  - 5a1. The system stores the materials, informs the student that automatic text extraction was incomplete, and notes that assistant context from those materials may be limited.

**Priority:** High
**Frequency of Use:** Once per project at kickoff; occasionally updated when the client provides revised materials.
**Business Rules:** BR-19 — Only a member of the team that owns the project may import its project source material. Imported materials are project inputs, not authored requirement content, and are not themselves graded as requirements.
**Associated Information:**
- The elicitation assistant uses the imported materials for gap analysis and interview-question preparation (UC-AI-2); the critique and drafting assistants may use them as context.
- Supported formats are PDF (`.pdf`) and PowerPoint (`.pptx`, `.ppt`); the per-file size limit is system-configurable (default 25 MB).
- Realized by FR-IMP-1, FR-IMP-2; honors FR-AI-14.

**Related Use Cases:** UC-AI-2: Elicit requirements with the elicitation assistant; UC-AI-4: Turn meeting notes into structured requirements.
**Assumptions:**
**Open Issues:**

### **UC-AI-2: The Student elicits requirements with the elicitation assistant**

**UC ID and Name:** UC-AI-2: Elicit requirements with the elicitation assistant
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:** Elicitation assistant (realized via the LLM Service)
**Trigger:** The student indicates to start an elicitation session ("Help me elicit") with the elicitation assistant to prepare for or work through a discussion with the client.
**Description:** The student usually does not yet have the requirements — the knowledge lives with a client who is typically non-technical and is not present in the system. The student wants the elicitation assistant to coach her through eliciting requirements from that client: helping her formulate the right questions to ask, plan follow-up questions, and verify the client's answers (spotting vagueness, contradictions, and gaps that warrant another round). The assistant is deliberately Socratic — it coaches the student's own questioning and does not invent requirements or author content. Once the client's input is clarified and verified, the student returns to the system and drafts the content herself. This use case serves two scopes: a **broad, project-wide** session — typically the first one, run right after importing the client's pitch materials (UC-AI-1) with no authoring destination selected, to prepare for the first elicitation workshop — and a **focused, per-authoring-destination** session, run while eliciting for a particular authoring destination (a document section or a use case) so that the assistant has context for what she is trying to elicit. The two scopes differ only in what the assistant is grounded in; the coaching loop is the same.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team.
- PRE-3. The elicitation assistant is enabled for the student's course section (see UC-CFG-2).

**Postconditions:**
- POST-1. An elicitation dialog (the assistant's coaching, and the questions and client answers the student worked through) is recorded for the session.
- POST-2. No requirement content is authored by the assistant; any requirements the student captures are drafted by her through the relevant authoring use cases.

**Main Success Scenario:**
1. The student starts an elicitation session for a particular authoring destination — a document section or a use case.
2. The system gathers the authoring destination's content and template context, the imported project source material, and the course section's teaching context.
3. The system asks the elicitation assistant, via the LLM Service, to analyze the gaps between the authoring destination and its template and to propose client-interview questions for them, phrased in plain, non-technical language.
4. The system presents the assistant's suggested questions and follow-up strategies to the student.
5. The student conducts the discussion with the client outside the system and brings the client's answers back into the session.
6. The student shares the client's answers with the assistant; the assistant helps her verify them — flagging vague, ambiguous, or contradictory answers and proposing follow-up questions for the next round.
7. The student repeats the discuss-and-verify loop (returns to step 4) until the client's input is clear and complete enough to author.
8. Once the client's input is verified, the student returns to the system and drafts the content herself through UC-DOC-2: Edit a section-based requirement document (or UC-DOC-6: Edit a use case), optionally translating her verified notes into candidate requirements through UC-AI-4: Turn meeting notes into structured requirements.
9. Use case ends.

**Extensions:**
- **1a. The student runs a broad, project-wide session (e.g., first-workshop prep after the pitch import)**
  - 1a1. The student starts the session without selecting an authoring destination.
  - 1a2. The system gathers the project's current requirements coverage across its documents and sections in place of a single authoring destination's context.
  - 1a3. The system asks the elicitation assistant, via the LLM Service, to perform a gap analysis comparing that coverage against what a complete set requires.
  - 1a4. The scenario continues from step 4.
- **1b. The imported pitch materials have gone stale and the student excludes them for the session**
  - 1b1. The system grounds the session only on the team's current drafted requirements, omitting the project source material from the assistant's context, and the scenario continues.
- **3a. The elicitation assistant is disabled for the course section**
  - 3a1. The system informs the student that the elicitation assistant is unavailable and terminates the use case.
- **3b. The LLM Service is unavailable or times out**
  - 3b1. The system informs the student that AI assistance is temporarily unavailable and that the rest of RAM continues to operate, and terminates the use case.
- **4a. The student has already drafted a list of questions and wants them reviewed**
  - 4a1. The student submits her drafted questions; the assistant reviews them, flags technical jargon, and suggests plain-language phrasings suited to a non-technical client, each with a rationale. The student revises her questions herself, optionally accepting a suggested phrasing through UC-AI-8: Review and accept or reject an assistant proposal, then returns to step 4.
- **4b. The student wants to rehearse the interview before meeting the client**
  - 4b1. The student practices through UC-AI-3: Practice a client interview with a role-playing client assistant, then returns to step 5.
- **5a. The student can resolve the assistant's questions without a new client conversation**
  - 5a1. Using her existing knowledge and the imported project source material, the student answers the assistant's questions directly and proceeds to draft the content (step 8) — the common case for a focused, per-document-section "help me elicit" while authoring.
- **6a. The student asks the assistant to draft the requirements from the answers**
  - 6a1. Per the assistant's configured Socratic behavior, the system declines to author finished requirements and instead helps the student structure her own draft (optionally via UC-AI-4), returning to step 8.

**Priority:** High
**Frequency of Use:** A broad, project-wide session typically once at the start, right after the pitch import, to prepare the first elicitation workshop; thereafter frequent, focused per-authoring-destination sessions throughout the elicitation-heavy phase, before and between client meetings.
**Business Rules:** BR-15, BR-16, BR-18 — The elicitation assistant shall be Socratic — it shall coach the student's questioning, follow-up, and verification rather than author requirements for her. The assistant must be enabled for the course section (UC-CFG-2). The system shall not author or modify student content with assistant-generated text without explicit confirmation. Real clients are not participants in the system. The project source material is a kickoff input that may become stale; where it conflicts with later authored-and-verified requirements, the authored content prevails, and the assistant shall not treat the imported pitch materials as authoritative over the team's current, verified requirements. The student may exclude the project source material from the assistant's grounding for a session, directing the assistant to rely only on the current drafted requirements.
**Associated Information:**
- Educational intent: the assistant trains the student to elicit from and communicate with a non-technical client and to verify client input — core skills this version teaches; productivity is deliberately subordinated to learning.
- Real clients are deliberately kept out of the system; the client discussion happens off-platform. The student can rehearse it against a simulated client through UC-AI-3.
- The assistant grounds its gap analysis in the imported project source material (UC-AI-1) when available and not excluded for the session, and draws on the teaching context configured for the course section (UC-CFG-1).
- The imported pitch materials are a starting snapshot that rots as the project evolves; in the broad, project-wide mode the assistant grounds its gap analysis increasingly in the project's current requirements coverage, and flags when a gap or assumption rests only on the (possibly stale) pitch so the student knows to re-verify with the client. Later in the project the student can exclude the pitch entirely for a session and elicit against the drafted requirements alone.
- Reviewing the student's own draft questions — flagging technical jargon and suggesting plain-language phrasings with rationale — is part of this coaching, so the student learns to communicate with a non-technical client.
- All calls to the LLM Service are routed through the server-side AI proxy.
- Realized by FR-AI-1, FR-AI-11, FR-AI-15, FR-AI-17; honors FR-AI-3, FR-AI-13, FR-AI-14.

**Related Use Cases:** UC-AI-3: Practice a client interview with a role-playing client assistant; UC-AI-4: Turn meeting notes into structured requirements; UC-AI-9: Consult the project assistant; UC-DOC-2: Edit a section-based requirement document; UC-DOC-6: Edit a use case; UC-CFG-1: Configure the teaching context for a course section; UC-CFG-2: Enable or disable AI assistants for a course section.
**Assumptions:**
**Open Issues:**

### **UC-AI-3: The Student practices a client interview with a role-playing client assistant**

**UC ID and Name:** UC-AI-3: Practice a client interview with a role-playing client assistant
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:** Client Role-Play assistant (realized via the LLM Service)
**Trigger:** The student indicates to start a practice client-interview session.
**Description:** The student wants to practice interviewing a non-technical client by conversing with an assistant that role-plays a client who gives vague, non-technical, and sometimes contradictory answers — so that the student practices extracting requirements from realistic, imperfect client input. This supports the educational goal of teaching client communication without putting a real client in the system.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team.
- PRE-3. The client role-play assistant is enabled for the student's course section (see UC-CFG-2).

**Postconditions:**
- POST-1. A practice interview transcript is recorded for the session.
- POST-2. The session is a practice exercise on a fictional scenario; it produces no requirement content and contributes nothing to the team's real requirements.

**Main Success Scenario:**
1. The student indicates to start a practice client-interview session, optionally selecting a scenario.
2. The system asks the client role-play assistant, via the LLM Service, to adopt the persona of a non-technical client for the selected scenario, using the teaching context for the scenario and grading standards.
3. The student asks the simulated client a question.
4. The client role-play assistant responds in character — in plain, non-technical language that may be vague or contradictory.
5. The student continues the interview (returns to step 3), probing and clarifying, and optionally records notes.
6. The student ends the session and reviews the recorded transcript and her notes to reflect on her interviewing technique.
7. Use case ends.

**Extensions:**
- **2a. The client role-play assistant is disabled for the course section**
  - 2a1. The system informs the student that the assistant is unavailable and terminates the use case.
- **2b. The LLM Service is unavailable or times out**
  - 2b1. The system informs the student that AI assistance is temporarily unavailable and terminates the use case.

**Priority:** Low
**Frequency of Use:** Periodic; tied to client-communication exercises.
**Business Rules:** BR-15, BR-16 — The client role-play assistant shall stay in the non-technical client persona and shall not author requirements for the student. The assistant must be enabled for the course section (UC-CFG-2).
**Associated Information:**
- Educational goal: train client communication without putting real clients in the system.
- The assistant draws on the teaching context for the scenario and the standards being taught.
- The practice runs on a fictional scenario: nothing the simulated client says is real project input, and the session yields no requirement content for the team's project. Its value is the interviewing skill the student builds before the real elicitation (UC-AI-2) — it is deliberately not wired to the structuring use case (UC-AI-4), which is for real client-meeting notes.
- Realized by FR-AI-10; honors FR-AI-13.

**Related Use Cases:** UC-AI-2: Elicit requirements with the elicitation assistant; UC-CFG-2: Enable or disable AI assistants for a course section.
**Assumptions:**
**Open Issues:**

### **UC-AI-4: The Student turns meeting notes into structured requirements**

**UC ID and Name:** UC-AI-4: Turn meeting notes into structured requirements
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:** Structuring assistant (realized via the LLM Service)
**Trigger:** The student indicates to turn plain-language meeting notes into structured requirements.
**Description:** The student wants an assistant to help translate her plain-language client-meeting notes into structured requirements (e.g., candidate use cases, business rules, or functional requirements) so that she learns the translation from informal client language to structured requirements. The assistant proposes candidates; the student inspects and accepts or rejects each, and remains the author. Because a single set of notes usually yields several kinds of requirement, the candidates target different authoring destinations — a document section of a section-based requirement document (either narrative text such as Background or Introduction, or a requirement-artifact list such as Risks, Objectives, or functional requirements), or a use case in the Use Cases document, which is an artifact in its own right and not a document section — and each accepted candidate is applied to its authoring destination.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the content.
- PRE-3. The structuring assistant is enabled for the student's course section (see UC-CFG-2).
- PRE-4. The student has plain-language notes to translate.

**Postconditions:**
- POST-1. Candidate structured requirements are proposed; only those the student explicitly accepts become artifacts she authors.

**Main Success Scenario:**
1. The student provides her plain-language meeting notes and indicates to translate them.
2. The system gathers relevant context (the authoring destination templates, related glossary terms, and the teaching context).
3. The system asks the structuring assistant, via the LLM Service, to propose candidate structured requirements from the notes — each identifying its authoring destination (a document section, or a use case in the Use Cases document) and the note it derives from.
4. The structuring assistant returns the candidate requirements, organized by their authoring destinations — document sections and use cases alike (a single set of notes typically spans several).
5. The student reviews and accepts or rejects each candidate through UC-AI-8: Review and accept or reject an assistant proposal.
6. For each accepted candidate, the system applies it to its authoring destination as a draft the student authors — which requires her to hold that authoring destination's lock (a document section via UC-DOC-2, or a use case via UC-DOC-6) — and she refines and completes it through UC-DOC-5: Create a use case or UC-ART-3: Create a requirement artifact.
7. Use case ends.

**Extensions:**
- **2a. The structuring assistant is disabled for the course section**
  - 2a1. The system informs the student that the assistant is unavailable and terminates the use case.
- **3a. The LLM Service is unavailable or times out**
  - 3a1. The system informs the student that AI assistance is temporarily unavailable and terminates the use case.
- **4a. The notes contain nothing the assistant can structure (e.g., too sparse or off-topic)**
  - 4a1. The system tells the student that no candidates were produced and suggests adding detail or eliciting more from the client (UC-AI-2); the use case ends.
- **6a. A candidate's authoring destination is locked by another team member**
  - 6a1. A candidate whose authoring destination — a document section or a use case — is held locked by another member cannot be applied until the lock is released; it remains pending while the student applies the candidates whose authoring destinations she can lock.

**Priority:** High
**Frequency of Use:** Periodic; after a real client meeting.
**Business Rules:** BR-16, BR-17 — Candidate requirements shall be applied only through the explicit acceptance loop of UC-AI-8 (no "accept all"). The assistant must be enabled for the course section (UC-CFG-2). Each candidate shall show the note it derives from as rationale. An accepted candidate shall be applied to its authoring destination — a document section (UC-DOC-2) or a use case (UC-DOC-6) — and is subject to that authoring destination's lock; a candidate whose authoring destination is locked by another team member shall not be applied until the lock is released.
**Associated Information:**
- Teaches the translation from informal client language to structured requirements.
- The meeting notes are the student's own notes from a real client interaction (typically the off-platform interview she prepared for via UC-AI-2) — transient input provided for this session, not project source material (UC-AI-1) and not stored or graded as requirement content. Practice-interview notes (UC-AI-3) are fictional and are deliberately not used here.
- This use case **structures** verified notes into candidate requirements; it is distinct from the **verification** the elicitation assistant performs in UC-AI-2 (step 6), which checks the client's raw answers for clarity, consistency, and completeness. The normal order is verify first (UC-AI-2), then structure the verified notes here.
- Each accepted candidate is applied through UC-AI-8 as the student's own authored edit (recorded as hers, with provenance), then refined by her — the assistant never silently writes content.
- Realized by FR-AI-12, FR-AI-9; honors FR-AI-3, FR-AI-8, FR-AI-13.

**Related Use Cases:** UC-AI-2: Elicit requirements with the elicitation assistant; UC-AI-8: Review and accept or reject an assistant proposal; UC-DOC-2: Edit a section-based requirement document; UC-DOC-5: Create a use case; UC-DOC-6: Edit a use case; UC-ART-3: Create a requirement artifact; UC-CFG-2: Enable or disable AI assistants for a course section.
**Assumptions:**
**Open Issues:**

### **UC-AI-5: The Student requests a critique from the critique assistant**

**UC ID and Name:** UC-AI-5: Request a critique from the critique assistant
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:** Critique assistant (realized via the LLM Service)
**Trigger:** The student indicates to request an assistant critique of the currently selected document section or use case.
**Description:** The student wants the critique assistant to evaluate an authored document section or use case for clarity, ambiguity, consistency, completeness, and testability, and to explain why each issue weakens the requirements — so that the student can improve it herself. The critique is qualitative, rationale-rich feedback that complements, and is distinct from, the deterministic ReqLint validation in UC-VAL-1. The assistant's rationale is itself the lesson.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the requirement document.
- PRE-3. The critique assistant is enabled for the student's course section (see UC-CFG-2).
- PRE-4. A document section or a use case is selected and has authored content.

**Postconditions:**
- POST-1. A critique (findings with rationale) is generated and displayed, visually distinguished from student-authored content.
- POST-2. No content is modified by the assistant.

**Main Success Scenario:**
1. The student indicates to request a critique of the selected item (a document section or a use case).
2. The system gathers the selected item's content and relevant context (its type, template guidance, related glossary terms, and the teaching context).
3. The system asks the critique assistant, via the LLM Service, to evaluate the selected item.
4. The critique assistant returns findings, each with a category (e.g., clarity, ambiguity, consistency, completeness, testability) and an instructive rationale explaining why it weakens the requirement.
5. The system presents the findings to the student, visually distinguished from authored content, each linked to the relevant location in the document section or use case.
6. The student reviews a finding, navigates to the location, and revises the document section or use case herself through UC-DOC-2: Edit a section-based requirement document (or UC-DOC-6: Edit a use case).
7. Use case ends.

**Extensions:**
- **3a. The critique assistant is disabled for the course section**
  - 3a1. The system informs the student that the critique assistant is unavailable and terminates the use case.
- **3b. The LLM Service is unavailable or times out**
  - 3b1. The system informs the student that AI assistance is temporarily unavailable and terminates the use case.
- **4a. The critique assistant proposes a concrete rewrite for a finding**
  - 4a1. The student reviews and accepts or rejects the proposed rewrite through UC-AI-8: Review and accept or reject an assistant proposal.

**Priority:** High
**Frequency of Use:** Frequent; typically after drafting and before submission.
**Business Rules:** BR-16, BR-17 — Every critique finding shall include an instructive rationale. The critique assistant must be enabled for the course section (UC-CFG-2). The assistant shall not modify student-authored content without explicit confirmation (UC-AI-8). Critique is advisory and distinct from ReqLint validation (UC-VAL-1).
**Associated Information:**
- Critique evaluates against the standards encoded in the teaching context (e.g., INVEST, testability, Wiegers categories).
- Distinct from UC-VAL-1 (ReqLint): ReqLint is deterministic rule-checking; critique is qualitative assistant feedback that complements it.
- Requesting a critique is read-only — it neither locks nor modifies the selected item. Acting on a finding changes content only through the normal path: a manual revision (UC-DOC-2 / UC-DOC-6) or an accepted rewrite (extension 4a) via UC-AI-8, each subject to the selected item's lock.
- Realized by FR-AI-2, FR-AI-9; honors FR-AI-3, FR-AI-5, FR-AI-13.

**Related Use Cases:** UC-VAL-1: Run validation (ReqLint) on the current document; UC-AI-8: Review and accept or reject an assistant proposal; UC-DOC-2: Edit a section-based requirement document; UC-DOC-6: Edit a use case; UC-CFG-1: Configure the teaching context for a course section; UC-CFG-2: Enable or disable AI assistants for a course section.
**Assumptions:**
**Open Issues:**

### **UC-AI-6: The Student asks an assistant to explain a concept (Tutor Mode)**

**UC ID and Name:** UC-AI-6: Ask an assistant to explain a concept (Tutor Mode)
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:** Tutor assistant (realized via the LLM Service)
**Trigger:** The student indicates to ask the tutor assistant to explain a concept, a glossary term, or a flagged issue.
**Description:** The student wants the tutor assistant to explain a requirements-engineering concept, a glossary term, or a flagged issue (a ReqLint validation finding or a critique finding) — including, for a flagged issue, the rule that was violated and a suggested fix — so that she learns while authoring. Tutor Mode is purely educational; it does not modify content.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team.
- PRE-3. The tutor assistant is enabled for the student's course section (see UC-CFG-2).

**Postconditions:**
- POST-1. An explanation is generated and displayed; no content is modified.

**Main Success Scenario:**
1. The student selects what to explain — a concept, a glossary term, or a flagged ReqLint/critique issue — and asks the tutor assistant for an explanation.
2. The system gathers the relevant context (the selected item, related glossary terms, and the teaching context; for a flagged issue, the rule involved).
3. The system asks the tutor assistant, via the LLM Service, for an explanation.
4. The tutor assistant returns an instructive explanation; for a flagged issue, it describes the rule that was violated and a suggested fix.
5. The system displays the explanation in the AI panel.
6. The student optionally asks a follow-up question (returns to step 3).
7. Use case ends.

**Extensions:**
- **3a. The tutor assistant is disabled for the course section**
  - 3a1. The system informs the student that the tutor assistant is unavailable and terminates the use case.
- **3b. The LLM Service is unavailable or times out**
  - 3b1. The system informs the student that AI assistance is temporarily unavailable and terminates the use case.

**Priority:** High
**Frequency of Use:** Frequent; on demand while authoring or reviewing validation results.
**Business Rules:** BR-16 — Tutor Mode shall not modify student-authored content. The tutor assistant must be enabled for the course section (UC-CFG-2). For a flagged validation issue, the explanation shall identify the rule violated and a suggested fix.
**Associated Information:**
- The tutor draws on the teaching context so explanations match the standards the student is graded against.
- Realized by FR-AI-4; honors FR-AI-13.

**Related Use Cases:** UC-VAL-1: Run validation (ReqLint) on the current document; UC-GLO-3: View a glossary term; UC-AI-5: Request a critique from the critique assistant; UC-CFG-1: Configure the teaching context for a course section; UC-CFG-2: Enable or disable AI assistants for a course section.
**Assumptions:**
**Open Issues:**

### **UC-AI-7: The Student generates a draft requirement skeleton with an assistant**

**UC ID and Name:** UC-AI-7: Generate a draft requirement skeleton with an assistant
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:** Drafting assistant (realized via the LLM Service)
**Trigger:** The student indicates to generate a draft skeleton for a use case or a set of requirements from a short prompt.
**Description:** The student wants the drafting assistant to produce a draft structural skeleton — for example, an empty use-case template with candidate step placeholders, or a set of candidate functional-requirement stubs — that gives her a starting structure to refine. Because handing students polished requirements undercuts the educational goal, this assistant is disabled by default for a course section and produces scaffolding and clearly-marked candidates rather than finished content; it is intended primarily for the secondary, future real-developer audience, for whom it can be enabled.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the content.
- PRE-3. The drafting assistant is enabled for the student's course section (see UC-CFG-2). It is disabled by default in course use.

**Postconditions:**
- POST-1. A draft skeleton is proposed; nothing is added to the student's content except through explicit acceptance.

**Main Success Scenario:**
1. The student indicates to generate a draft skeleton and provides a short prompt or selects the authoring destination (e.g., a new use case, or requirements for a feature).
2. The system gathers relevant context (the authoring destination template, related glossary terms, and the teaching context).
3. The system asks the drafting assistant, via the LLM Service, to produce a structural skeleton with candidate placeholders.
4. The drafting assistant returns the proposed skeleton.
5. The student reviews and accepts or rejects the proposal through UC-AI-8: Review and accept or reject an assistant proposal.
6. The accepted skeleton is applied as a draft on acceptance in step 5; the student then refines and completes it herself, authoring it out through UC-DOC-5: Create a use case (or UC-DOC-6 / UC-ART-3).
7. Use case ends.

**Extensions:**
- **3a. The drafting assistant is disabled for the course section**
  - 3a1. The system informs the student that the drafting assistant is unavailable and terminates the use case.
- **3b. The LLM Service is unavailable or times out**
  - 3b1. The system informs the student that AI assistance is temporarily unavailable and terminates the use case.

**Priority:** Low
**Frequency of Use:** Occasional; primarily in real-developer use rather than course use.
**Business Rules:** BR-15, BR-16, BR-17 — The drafting assistant shall be disabled by default for a course section and is enabled only through UC-CFG-2. Proposed content shall be applied only through the explicit acceptance loop of UC-AI-8 (no "accept all"). The assistant shall produce scaffolding and clearly-marked candidates, not silently inserted finished content.
**Associated Information:**
- Educational tiebreaker: in course use this assistant is off by default so that students structure their own work; it exists chiefly for the secondary, future real-developer audience.
- Realized by FR-AI-20, FR-AI-8; honors FR-AI-3, FR-AI-5, FR-AI-7, FR-AI-13; governed by UC-CFG-2.

**Related Use Cases:** UC-DOC-5: Create a use case; UC-ART-3: Create a requirement artifact; UC-AI-8: Review and accept or reject an assistant proposal; UC-CFG-2: Enable or disable AI assistants for a course section.
**Assumptions:**
**Open Issues:**

### **UC-AI-8: The Student reviews and accepts or rejects an assistant proposal**

**UC ID and Name:** UC-AI-8: Review and accept or reject an assistant proposal
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:** The proposing assistant (realized via the LLM Service)
**Trigger:** An assistant has produced a concrete proposal (a draft, rewrite, or set of candidate artifacts) and the student indicates to review it.
**Description:** Whenever an assistant proposes concrete content — a skeleton (UC-AI-7), a rewrite (UC-AI-5), or structured requirements from notes (UC-AI-4) — the student must inspect the proposal against its rationale and consciously accept or reject it before any change is made to her content. This propose → inspect → accept loop is a deliberate teaching instrument: it forces the student to read the diff, understand the rationale, and make a decision. There is intentionally no "accept all" shortcut. This use case captures that shared loop; the proposing use cases invoke it.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the content.
- PRE-3. An assistant has produced a proposal for review.

**Postconditions:**
- POST-1. Each proposed change is either accepted (and applied to its authoring destination as the student's authored content) or rejected (and discarded); no proposed change is applied without an explicit per-item acceptance.

**Main Success Scenario:**
1. The system presents the assistant's proposal as a set of discrete proposed changes — each targeting an authoring destination (a document section or a use case) — accompanied by the assistant's rationale and shown against that authoring destination's current content, visually distinguished from student-authored content.
2. The student inspects a proposed change and its rationale.
3. The student explicitly accepts or rejects that change.
4. On acceptance, the system applies the change to its authoring destination as authored content (recorded as the student's edit), which requires the student to hold that authoring destination's lock (a document section via UC-DOC-2, or a use case via UC-DOC-6). On rejection, the system discards the change.
5. The student repeats steps 2–4 for each proposed change.
6. Use case ends.

**Extensions:**
- **1a. The student dismisses the entire proposal**
  - 1a1. The system discards all proposed changes; no content is modified; the use case ends.
- **2a. The student requests an explanation of a proposed change**
  - 2a1. The student asks the tutor assistant to explain through UC-AI-6: Ask an assistant to explain a concept, then returns to step 2.
- **4a. A change's authoring destination is locked by another team member**
  - 4a1. The system cannot apply that change until the lock is released; it remains pending while the student accepts, rejects, and applies the changes whose authoring destinations she can lock.

**Priority:** High
**Frequency of Use:** Whenever a generative assistant produces a proposal.
**Business Rules:** BR-16, BR-17 — The system shall not apply assistant-generated content without an explicit, per-item acceptance by the student. The system shall not provide an "accept all" shortcut. Each proposed change shall be shown with its rationale and visually distinguished until accepted. A change shall be applied only to its authoring destination — a document section (UC-DOC-2) or a use case (UC-DOC-6) — and only while the student holds that authoring destination's lock.
**Associated Information:**
- The friction in this loop is pedagogically intentional and shall not be optimized away.
- Realized by FR-AI-8; honors FR-AI-3, FR-AI-5.

**Related Use Cases:** UC-AI-5: Request a critique from the critique assistant; UC-AI-7: Generate a draft requirement skeleton with an assistant; UC-AI-4: Turn meeting notes into structured requirements; UC-AI-6: Ask an assistant to explain a concept (Tutor Mode); UC-DOC-2: Edit a section-based requirement document; UC-DOC-6: Edit a use case.
**Assumptions:**
**Open Issues:**

### **UC-AI-9: The Student consults the project assistant**

**UC ID and Name:** UC-AI-9: Consult the project assistant
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:** Project assistant (realized via the LLM Service)
**Trigger:** The student opens the project assistant in the project workspace and asks for help (for example, "Where should I start?", "What's missing?", or "Help me elicit").
**Description:** In the project workspace the student wants a single conversational entry point that helps her orient herself, understand where the team's requirements stand, find the right document or artifact, and reach the right specialized assistant for the task at hand. The project assistant is a router: it answers questions about project status and coverage, points the student to the relevant document section or use case, and routes her into the specialized assistants — for example launching a broad, project-wide elicitation session through UC-AI-2, a per-destination critique through UC-AI-5, or a whole-project review through UC-AI-10. Like every assistant it is deliberately Socratic and educational: it orients, explains, and routes, but never authors requirement content for her; authoring stays with the student through the normal use cases.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team.
- PRE-3. The project assistant is enabled for the student's course section (see UC-CFG-2).

**Postconditions:**
- POST-1. The assistant conversation is recorded for the session.
- POST-2. No requirement content is authored by the assistant; any work the student does happens through the authoring or assistant use cases the assistant routes her to.

**Main Success Scenario:**
1. The student opens the project assistant in the project workspace and asks a question about her project.
2. The system gathers the project's current requirements coverage across its documents and sections, the imported project source material, and the course section's teaching context.
3. The system asks the project assistant, via the LLM Service, to interpret the request and respond — answering about status or coverage, pointing to the relevant document or artifact, or recommending the specialized assistant or action that fits.
4. The system presents the assistant's response and any recommended next actions to the student.
5. The student follows a recommended action, and the system routes her into the corresponding use case (for example, UC-AI-2 for elicitation or UC-AI-5 for a critique).
6. The student continues the conversation (returns to step 1) or proceeds in the use case she was routed to.
7. Use case ends.

**Extensions:**
- **3a. The project assistant is disabled for the course section**
  - 3a1. The system informs the student that the assistant is unavailable and terminates the use case.
- **3b. The LLM Service is unavailable or times out**
  - 3b1. The system informs the student that AI assistance is temporarily unavailable and that the rest of RAM continues to operate, and terminates the use case.
- **4a. The student asks the assistant to author or change requirement content**
  - 4a1. Per its Socratic configuration, the system declines to author content and instead routes the student to the appropriate authoring use case (for example, UC-DOC-2 or UC-DOC-6) or specialized assistant.
- **5a. The recommended assistant is disabled for the course section**
  - 5a1. The system tells the student the recommended assistant is unavailable and suggests an available alternative or action.

**Priority:** High
**Frequency of Use:** Frequent; a common entry point into the project workspace throughout the project.
**Business Rules:** BR-15, BR-16 — The project assistant shall be Socratic — it shall orient, explain, and route rather than author requirement content. The assistant must be enabled for the course section (UC-CFG-2). The system shall not author or modify student content with assistant-generated text without explicit confirmation. When routing the student into a specialized assistant, the system shall honor that assistant's own enablement and rules for the course section.
**Associated Information:**
- The assistant is a router over the specialized assistants; it invokes their use cases rather than duplicating their behavior — for example, broad, project-wide elicitation runs through UC-AI-2.
- It draws on the project's current requirements coverage, the imported project source material, and the teaching context configured for the course section (UC-CFG-1).
- Educational intent: it lowers the cost of finding the right next step without doing the student's thinking for her; productivity is deliberately subordinate to learning.
- All calls to the LLM Service are routed through the server-side AI proxy.
- Realized by FR-AI-18, FR-AI-19; honors FR-AI-3, FR-AI-7, FR-AI-13.

**Related Use Cases:** UC-AI-2: Elicit requirements with the elicitation assistant; UC-AI-5: Request a critique from the critique assistant; UC-AI-10: Request a whole-project review from the critique assistant; UC-AI-6: Ask an assistant to explain a concept (Tutor Mode); UC-DOC-2: Edit a section-based requirement document; UC-DOC-6: Edit a use case; UC-CFG-1: Configure the teaching context for a course section; UC-CFG-2: Enable or disable AI assistants for a course section.
**Assumptions:**
**Open Issues:**

### **UC-AI-10: The Student requests a whole-project review from the critique assistant**

**UC ID and Name:** UC-AI-10: Request a whole-project review from the critique assistant
**Created By:**
**Date Created:**
**Primary Actor:** student
**Secondary Actors:** Critique assistant (realized via the LLM Service)
**Trigger:** The student indicates to request a whole-project review of the team's requirement documents (via a "Review whole project" action, or routed in from the project assistant in UC-AI-9).
**Description:** The student wants the critique assistant to review the team's entire set of requirement documents together — not one selected document section or use case — so that she can find cross-document problems that are invisible when reviewing one item at a time: coverage gaps, inconsistencies, conflicts, and broken traceability across the documents. The review is qualitative, rationale-rich feedback that complements both the deterministic ReqLint validation (UC-VAL-1) and the per-destination critique (UC-AI-5). It is governed by the cross-document review criteria the instructor configures for the course section (UC-CFG-4); the assistant's rationale is itself the lesson.

**Preconditions:**
- PRE-1. The student is logged into the system.
- PRE-2. The student is a member of the team that owns the documents.
- PRE-3. The critique assistant is enabled for the student's course section (see UC-CFG-2).
- PRE-4. The cross-document review criteria are defined for the student's course section (see UC-CFG-4).

**Postconditions:**
- POST-1. A whole-project review (cross-document findings with rationale) is generated and displayed, visually distinguished from student-authored content.
- POST-2. No content is modified by the assistant.

**Main Success Scenario:**
1. The student indicates to request a whole-project review of the team's requirement documents.
2. The system gathers the content of all the team's requirement documents, the relevant requirements-graph context for traceability, the course section's cross-document review criteria (UC-CFG-4), and the teaching context (UC-CFG-1).
3. The system asks the critique assistant, via the LLM Service, to evaluate the documents together against the configured criteria.
4. The critique assistant returns cross-document findings, each with a category (e.g., completeness, coverage and traceability, consistency, conflict), a location or set of locations, and an instructive rationale explaining why it weakens the requirements.
5. The system presents the findings to the student, visually distinguished from authored content, each linked to the relevant document, document section, or use case.
6. The student reviews a finding, navigates to the location, and revises the document section or use case herself through UC-DOC-2: Edit a section-based requirement document (or UC-DOC-6: Edit a use case).
7. Use case ends.

**Extensions:**
- **1a. The cross-document review criteria are not defined for the course section**
  - 1a1. The system informs the student that the whole-project review is unavailable until her instructor defines the criteria (UC-CFG-4) and terminates the use case.
- **3a. The critique assistant is disabled for the course section**
  - 3a1. The system informs the student that the critique assistant is unavailable and terminates the use case.
- **3b. The LLM Service is unavailable or times out**
  - 3b1. The system informs the student that AI assistance is temporarily unavailable and that the rest of RAM continues to operate, and terminates the use case.
- **4a. The critique assistant proposes a concrete rewrite for a finding**
  - 4a1. The student reviews and accepts or rejects the proposed rewrite through UC-AI-8: Review and accept or reject an assistant proposal.

**Priority:** High
**Frequency of Use:** Periodic; typically at milestones and before submission.
**Business Rules:** BR-16, BR-17 — Every finding shall include an instructive rationale. The critique assistant must be enabled for the course section (UC-CFG-2). The assistant shall not modify student-authored content without explicit confirmation (UC-AI-8). The review is advisory and distinct from ReqLint validation (UC-VAL-1).
**Associated Information:**
- The whole-project review reads the team's entire set of requirement documents together (the document set may grow beyond the current five); it is the project-wide complement to the per-destination critique in UC-AI-5 and to the elicitation assistant's broad-mode gap analysis (FR-AI-15).
- The dimensions and standards applied are the cross-document review criteria configured by the instructor for the course section (UC-CFG-4); the review methodology and the system-provided default criteria set are maintained in `docs/product/cross-document-review-criteria.md`.
- Requesting a whole-project review is read-only — it neither locks nor modifies any document. Acting on a finding changes content only through the normal path: a manual revision (UC-DOC-2 / UC-DOC-6) or an accepted rewrite (extension 4a) via UC-AI-8, each subject to the affected item's lock.
- Reachable directly (a "Review whole project" action) and via the project assistant (UC-AI-9), which routes the student into this use case.
- Realized by FR-AI-21, FR-AI-22; honors FR-AI-3, FR-AI-9, FR-AI-13, FR-AI-23.

**Related Use Cases:** UC-AI-5: Request a critique from the critique assistant; UC-AI-9: Consult the project assistant; UC-CFG-4: Configure the cross-document review criteria for a course section; UC-VAL-1: Run validation (ReqLint) on the current document; UC-AI-8: Review and accept or reject an assistant proposal; UC-DOC-2: Edit a section-based requirement document; UC-DOC-6: Edit a use case.
**Assumptions:**
**Open Issues:**
