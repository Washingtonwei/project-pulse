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
*List any activities that must take place, or any conditions that must be true, before the use case can be started. The system must be able to test each precondition. Number each precondition. Example: PRE-1: user's identity has been authenticated.*

***Postconditions***
*Describe the state of the system at the successful conclusion of the use case execution. Label each postcondition in the form POST-X, where X is a sequence number. Example: POST-1: Price of item in the database has been updated with the new value.*

***Main Success Scenario/Normal Flow***
*Provide a description of the user actions and corresponding system responses that will take place during execution of the use case under normal, expected conditions. This dialog sequence will ultimately lead to accomplishing the goal stated in the use case name and description. Show a numbered list of actions performed by the actor, alternating with responses provided by the system. The normal flow is numbered "X.0", where "X" is the Use Case ID.*

***Extensions:***

- ***Alternative Flows***
  *Document other successful usage scenarios that can take place within this use case. State the alternative flow, and describe any differences in the sequence of steps that take place. Number each alternative flow in the form "X.Y", where "X" is the Use Case ID and Y is a sequence number for the alternative flow. For example, "5.3" would indicate the third alternative flow for use case number 5. Indicate where each alternative flow would branch off from the normal flow, and if pertinent, where it would rejoin the normal flow.*

- ***Exceptions***
  *Describe any anticipated error conditions that could occur during execution of the use case and how the system is to respond to those conditions. Number each alternative flow in the form "X.Y.EZ", where "X" is the Use Case ID, Y indicates the normal (0) or alternative (>0) flow during which this exception could take place, "E" indicates an exception, and "Z" is a sequence number for the exceptions. For example "5.0.E2" would indicate the second exception for the normal flow for use case number 5. Indicate where in the normal (or an alternative) flow each exception could occur.*

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

The catalog of use cases begins below, grouped by area. Each area is an unnumbered H2 sub-heading and each use case an H3 carrying an area-prefixed ID (`UC-<AREA>-<n>`). A use case's primary actor is recorded in its `Primary Actor` field rather than duplicated here.

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
| Section name | String | Optional |  |  |

Search results display strategy (specify which properties to display for each matching course section): Section name, team names

Sort criteria: Section name in descending order, team names are in ascending order

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
| Section name | String |  |  |  |
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
**Description:** The course admin wants to create a new course section, so that she can invite students to join the section.

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
- Details: Section name: E.g., Section 2023-2024 Start and end date of the section: E.g., 08/21/2023 and 05/01/2024 Editing a rubric: When the course admin edits an existing rubric, behind the scenes, the system shall first duplicate the existing rubric and then let the course admin edit it. In other words, a new rubric is created. Duplication detection rules: Section name is used as the unique identifier for a section. The course admin shall be able to cancel the use case at any time prior to submitting it.

**Assumptions:**
**Open Issues:**

### **UC-SEC-4: The Course Admin edits a course section**

**UC ID and Name:** UC-SEC-4: Edit a course section
**Created By:**
**Date Created:**
**Primary Actor:** course admin
**Secondary Actors:**
**Trigger:** The course admin indicates to change the details of an existing course section.
**Description:** The course admin wants to change the details of an existing course section, so that the section information is correct and up-to-date.

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
| Section name | String | Yes |  |  |  |  |
| Start and end date of the section |  | Yes |  |  |  |  |
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
**Description:** The course admin wants to set up weeks for a section, so that the senior design students know in which weeks they need to submit WARs and peer evaluations.

**Preconditions:**
- PRE-1. The course admin is logged into the system.
- PRE-2. At least one course section is created.

**Postconditions:**
- POST-1. The active weeks for a course section are stored in the system.

**Main Success Scenario:**
1. The course admin indicates to set up weeks for a course section.
2. The system displays all the weeks of this section according to the start and end date of the section.
3. The course admin specifies the weeks during which the students do not need to submit WARs and peer evaluations, and confirms that she has finished.
4. The system displays the active weeks of the section and asks the course admin to confirm the setup.
5. The course admin either confirms the setup (continues the normal flow) or chooses to modify the details (return to step 3).
6. The system saves the active weeks for this section and informs the course admin that this setup has been done.
7. Use case ends.

**Extensions:**
**Priority:** High
**Frequency of Use:** 1 user, 1 usage per year.
**Business Rules:** BR-2

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
| Section Id |  |  |  |  |
| Section name | String | Optional |  |  |
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
- **5a. The course admin finds an wrong team member removal:**
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
- Email format: emails shall be separated by semicolon and the system shall ignore spaces in between. E.g., Good: john.doe@tcu.edu; f.smith@tcu.edu; tim.johnson@tcu.edu; lily.p.lee@tcu.edu Good: john.doe@tcu.edu;f.smith@tcu.edu Bad: john.doe@tcu.edu; f.smith@tcu.edu; Bad: john.doe@tcu.edu f.smith@tcu.edu Default email message: *Subject: Welcome to The Peer Evaluation Tool - Complete Your Registration Hello, [Name of the course admin] has invited you to join The Peer Evaluation Tool. To complete your registration, please use the link below: [Registration link] If you have any questions or need assistance, feel free to contact [course admin's email] or our team directly. Please note: This email is not monitored, so do not reply directly to this message. Best regards, Peer Evaluation Tool Team*  The invitation link shall be unique for each student. The course admin shall be able to cancel the use case at any time prior to submitting it.

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
| Section name | String | Optional |  |  |
| Team name | String | Optional |  |  |
| Section Id | Integer | Optional |  |  |
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
| Section name |  |  |  |  |
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
- Email format: emails shall be separated by semicolon and the system shall ignore spaces in between. E.g., Good: john.doe@tcu.edu; f.smith@tcu.edu; tim.johnson@tcu.edu; lily.p.lee@tcu.edu Good: john.doe@tcu.edu;f.smith@tcu.edu Bad: john.doe@tcu.edu; f.smith@tcu.edu; Bad: john.doe@tcu.edu f.smith@tcu.edu Default email message: *Subject: Welcome to The Peer Evaluation Tool - Complete Your Registration Hello, [Name of the course admin] has invited you to join The Peer Evaluation Tool. To complete your registration, please use the link below: [Registration link] If you have any questions or need assistance, feel free to contact [course admin's email] or our team directly. Please note: This email is not monitored, so do not reply directly to this message. Best regards, Peer Evaluation Tool Team*  The invitation link shall be unique for each instructor. The course admin shall be able to cancel the use case at any time prior to submitting it.

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
- **4a. The course admin finds an wrong team assignment:**
  - 4a1. The course admin removes an instructor from a team, reassign her to a new team, and returns to step 4 of the normal flow.

**Priority:** High
**Frequency of Use:** 1 user, 1 usage per year.
**Business Rules:** BR-1

**Associated Information:**
- Notification: The system notifies the instructors about their team assignment. The course admin shall be able to cancel the process at any time prior to submitting it.

**Related Use Cases:**
**Assumptions:** The instructor must be assigned to the section of the team first. TODO
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
- **5a. The course admin finds an wrong team member removal:**
  - 5a1. The course admin corrects the wrong removal and returns to step 4 of the normal flow.

**Priority:** Low
**Frequency of Use:** Rare. 1 user, 1 usage per year.
**Business Rules:** BR-1

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

### **UC-INS-6: The Course Admin deactivate an instructor**

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
- POST-1. The insturctor's account is deactivated.

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

### **UC-INS-7: The Course Admin reactivate an instructor**

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
- POST-1. The Insturctor's account is reactivated.

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
4. The system displays the activities already added by this student in the WAR and asks the student to select operations: Add a new activity (step 6-10) Edit an existing activity (step 11-16) Delete an existing activity (step 17-20) The student selects one out of the three operations.
5. Based on the student's selection in step 5, the flow goes through either step 6-10, or step 11-16, or step 17-20.
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
  - 13a2. The student corrects the mistake and returns to step 8 of the normal flow.

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
  - 4b1. The system alerts the user that an input validation rule is violated and displays the nature and location of the error.
  - 4b2. The user corrects the mistake and returns to step 4 of the normal flow.
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
  - 6b1. The system alerts the instructor that an input validation rule is violated and displays the nature and location of the error.
  - 6b2. The instructor corrects the mistake and returns to step 6 of the normal flow.
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
7. Peer evaluations can be edited after submission.
8. The student either confirms the evaluation and submission (continues the normal flow) or chooses to modify the details (return to step 3).
9. The system saves the peer evaluation and informs the student that this peer evaluation has been submitted.
10. Use case ends.

**Extensions:**
- **4a. Input validation rule violation:**
  - 4a1. The system alerts the student that an input validation rule is violated and displays the nature and location of the error.
  - 4a2. The student corrects the mistake and returns to step 4 of the normal flow.

**Priority:** High
**Frequency of Use:** Approximately 35-40 users, 1 usage per week.
**Business Rules:** BR-3, BR-4

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
  - 4b1. The system alerts the student that an input validation rule is violated and displays the nature and location of the error.
  - 4b2. The student corrects the mistake and returns to step 4 of the normal flow.
- **5a. No data is returned:**
  - 5a1. The system alerts the student that no data is available in the generated report.
  - 5a2. The student either chooses to return to step 3 of the normal flow or chooses to terminate the use case.

**Priority:** High
**Frequency of Use:** Approximately 35-40 users, average of 1 usage per week.
**Business Rules:** BR-5

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
  - 4b1. The system alerts the instructor that an input validation rule is violated and displays the nature and location of the error.
  - 4b2. The instructor corrects the mistake and returns to step 4 of the normal flow.
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
  - 6b1. The system alerts the instructor that an input validation rule is violated and displays the nature and location of the error.
  - 6b2. The instructor corrects the mistake and returns to step 6 of the normal flow.
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
