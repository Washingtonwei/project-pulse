# **Project Pulse**

# **Vision and Scope**

# **Version 1.0**

# **Revision History**

| Date | Version | Description | Author |
| ----- | ----- | ----- | ----- |
|  |  |  |  |
|  |  |  |  |
|  |  |  |  |
|  |  |  |  |

# **Introduction**

This document defines the overarching goals, purpose, and boundaries of the software project. Its purpose is to provide a shared understanding among stakeholders of what the software aims to achieve and the context in which it will operate. It identifies the business problems the software will solve, outlines the future vision for how the software will fit into the domain, and specifies the scope of the project by clearly stating what is included and excluded. This document serves as a foundation for aligning the project team and stakeholders, guiding decision-making, and managing expectations throughout the software development lifecycle. The details of how the Project Pulse system fulfills these needs are detailed in the use cases and software requirements specification.

## **Background**

The Department of Computer Science at Texas Christian University offers a course where senior students, in teams, collaborate with clients to solve real-world software problems. Students handle every project phase: definition, analysis, design, implementation, testing, deployment, and documentation. However, in a team, there's a variation in how much each student contributes. Some are very active, while others are not. Communication issues can also arise.

## **Current Process Flows (As-Is Process Flows)**

To handle these challenges and improve team efficiency, there is a student performance tracking system in place. This system has two main tools:

1. Weekly Activity Report (WAR)
- Every student is required to complete a WAR per week.
- WAR records what a student has done for a week.
- Google Sheets is used for recording WARs (see the image below).
- Students in the same team can edit and view the shared Google Sheets document.
- Each sheet records the activities of a team in a week.
- Foster better communication among team members.

The WAR Google Spread Sheets is available here: [https://docs.google.com/spreadsheets/d/1jpxBQ8Gvv94bRl1gSpBzXRFxJu7IQXKjQZ5n28-EpD8/edit?usp=sharing](https://docs.google.com/spreadsheets/d/1jpxBQ8Gvv94bRl1gSpBzXRFxJu7IQXKjQZ5n28-EpD8/edit?usp=sharing)

Here is the current process flow for submitting WARs:

Each Monday after the senior design project begins, every student must access the shared Google Sheets created for their team, locate the appropriate sheet based on the week number, and document the activities they completed during the previous week. The instructor will keep the URLs of all the Google Sheets and review the updated ones on Tuesday. Finally, the instructor will grade and provide feedback to students through the university's LMS (Learning Management System: TCU Online).

2. Peer Evaluation Form
- Excel Spreadsheet is used to collect peer evaluations in a team for a week (see the image below).
- Increase students' self-awareness and allow students to know how their team members perceive them.

Here is the current process flow for submitting and grading the peer evaluations:

Each Tuesday after the senior design project begins, every student must first review the team's WAR from the previous week, complete the peer evaluation form in Excel Spreadsheet, and upload it to the university's LMS. The instructor will then download all the forms, run a Java program written by the instructor to parse the data, finalize the grades, compile comments, and finally upload the grades and comments to the LMS. Students can subsequently view their teammates' evaluations through the same system.

While this system effectively improves team efficiency, it is overly manual and time-consuming. For the WAR, each student must individually edit the Google Sheets document for the week, which is then reviewed by the instructor. This process is prone to human error; for example, students may make mistakes when filling out the document, potentially resulting in them not receiving proper credit.

Similarly, the Peer Evaluation process is cumbersome. Each student must review the WAR from the previous week, create an Excel spreadsheet with specific columns, and upload it to TCU Online, the LMS used by TCU. Once all students have submitted their peer evaluation reports, the instructor must manually download all the reports from TCU Online, run them through a Java program to calculate results, and then re-upload the results to TCU Online. This process flow not only introduces opportunities for errors (e.g., spreadsheets with incorrect formatting or missing columns) but is also highly time-consuming for the instructor, who must handle multiple repetitive tasks manually.

## **References**

TODO: reference the WAR and Peer Evaluation form.

# **Business Requirements**

## **Business Opportunity/Problem Statement**

The current peer evaluation process within the Computer Science Department at TCU is inundated by inefficiencies, errors, and delays. Students are burdened with the laborious task of downloading, completing, and uploading peer evaluation forms on TCU Online, leading to a suboptimal academic experience. Faculty members struggle with the manual management of evaluations, sometimes resulting in delayed feedback. This inefficiency and error-prone method have substantial negative implications, hindering academic growth and resource allocation within the department. Addressing this problem presents a significant business opportunity. By automating the peer evaluation process, we can streamline operations, improve data accuracy, and provide timely feedback, all of which enhance the educational experience. This solution optimizes resource allocation and offers a user-friendly experience for both students and faculty. This result is a more efficient and effective peer evaluation system, benefitting the entire Computer Science department and fostering academic growth.

## **Business Objectives**

BO-1: Reduce the instructor's time to grade the peer evaluation by 50%.

BO-2: Increase students' WAR and Peer Evaluation submission rate by 20%.

BO-3: Reduce students' time to finish the WAR and Peer Evaluation by 25%.

## **Vision Statement**

| For | Students of TCU senior design |
| :---- | :---- |
| Who | Need an easier way to submit and update weekly activity reports and peer evaluations |
| The (product name) | Project Pulse |
| That | makes it easier for the student to submit weekly activity reports and peer evaluations, and makes it easier for the instructors to view and grade them |
| Unlike | the traditional manual process |
| Our product | streamlines the whole process making it more accessible and painless for both the instructor and the students to submit and grade progress. |

## **Proposed New/Improved Process Flows (To-Be Process Flows)**

In this project, we will improve the current process flows.

The WAR process has not changed:

As shown in the improved process, both students and the instructor can now complete their tasks entirely within the Project Pulse system. This streamlines the workflow, reducing manual steps and minimizing the risk of errors. However, one manual task remains: the instructor must still upload grades to the university's LMS. While this integration would further enhance efficiency, it is currently outside the scope of this project.

The Peer Evaluation process is also simplified:

The improved process for submitting and grading weekly peer evaluations is illustrated in the UML activity diagram. Students start by reviewing the WAR for their entire team and evaluating their team members within the Project Pulse system. Once all evaluations are completed, Project Pulse automates the process by compiling peer evaluations for the entire class, calculating scores, and generating feedback. The instructor can access and review the aggregated scores and feedback directly in Project Pulse. Additionally, a student can independently view their scores and feedback in the system. The final step, performed manually by the instructor, involves uploading the grades and feedback to the university's LMS to make them accessible to all students. This diagram emphasizes the seamless interaction between participants, the significant automation achieved through Project Pulse, and the remaining manual task of LMS integration, which is outside the current scope of this system.

## **Business Risks**

RI-1: If the system has to be deployed on a cloud service provider, the annual cloud fees need to be taken care of by the Computer Science Department.

RI-2: The students' peer evaluation database may be breached by hackers.

RI-3 The application could be more confusing to use than the previous method.

RI-4: The application might not be broad enough for other applications except for the TCU senior design course specifically.

## **Business Assumptions and Dependencies**

AS-1: The system should use technologies that the client has knowledge about and can maintain after the product has been delivered.

# **Stakeholder Profiles and User Descriptions**

## **Stakeholder Profiles**

| Stakeholder | Major value or benefit from this product | Attitudes | Major features of interest | Constraints | End user or not? |
| ----- | ----- | ----- | ----- | ----- | ----- |
| Students | It will be easier for students to submit and view their weekly activities and peer evaluations. | Supportive | Submitting everything in one place without having to download or upload anything directly. | Students must be instructed on how to use the platform. | Yes |
| Instructors | It will be easier for instructors to compile and grade the students' work and understand the team dynamics. | Supportive | View WARs and generate peer evaluation reports every week. | Instructors must be instructed on how to use the platform. | Yes |

## **User Environment**

Users will access the application using a web browser on their desktop, laptop, or mobile device regardless of operating system.

## **Alternatives and Competition**

The alternative is what is currently being used, which is where the student will upload their peer evaluation onto TCU Online, and then the instructor will have to download each one and calculate the grade for each group manually.

# **Scope and Limitations**

## **Product Perspective**

The Level 1: Context Diagram for the Project Pulse system provides a high-level overview of its interactions with users and external systems. Project Pulse serves as the central platform, enabling instructors to manage courses, create Weekly Activity Reports (WARs) and peer evaluation templates, and review submissions, while students use it to submit WARs, complete peer evaluations, and view scores and feedback. The system integrates with the Gmail system to send automated email notifications, such as reminders and updates, to both instructors and students. This diagram highlights the roles of the instructor and student as primary users, the central functionality of Project Pulse, and its reliance on Gmail for communication, offering a clear picture of the system's operational scope and interactions.

## **Major Features / Scope**

FE-1: Manage course sections, teams, and students.

FE-3: Submit weekly activity reports and peer evaluations.

FE-4: Generate weekly activity reports and peer evaluation grades for the entire course section.

See the use case document for more details.

## **Deployment Considerations**

The system will be hosted on a cloud service like Microsoft Azure.
