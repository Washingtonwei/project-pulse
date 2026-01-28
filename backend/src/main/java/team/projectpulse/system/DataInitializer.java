package team.projectpulse.system;

import team.projectpulse.activity.Activity;
import team.projectpulse.activity.ActivityCategory;
import team.projectpulse.activity.ActivityRepository;
import team.projectpulse.activity.ActivityStatus;
import team.projectpulse.course.Course;
import team.projectpulse.course.CourseRepository;
import team.projectpulse.evaluation.PeerEvaluation;
import team.projectpulse.evaluation.PeerEvaluationRepository;
import team.projectpulse.instructor.Instructor;
import team.projectpulse.instructor.InstructorRepository;
import team.projectpulse.ram.collaboration.Comment;
import team.projectpulse.ram.collaboration.CommentThread;
import team.projectpulse.ram.collaboration.CommentThreadRepository;
import team.projectpulse.ram.collaboration.CommentThreadStatus;
import team.projectpulse.ram.document.*;
import team.projectpulse.ram.document.template.DocumentTemplate;
import team.projectpulse.ram.document.template.DocumentTemplateRegistry;
import team.projectpulse.ram.requirement.*;
import team.projectpulse.ram.usecase.*;
import team.projectpulse.rubric.*;
import team.projectpulse.section.Section;
import team.projectpulse.student.Student;
import team.projectpulse.team.Team;
import team.projectpulse.user.PeerEvaluationUser;
import team.projectpulse.user.userinvitation.UserInvitation;
import team.projectpulse.user.userinvitation.UserInvitationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;
    private final PeerEvaluationRepository peerEvaluationRepository;
    private final ActivityRepository activityRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserInvitationRepository userInvitationRepository;
    private final RequirementArtifactService requirementArtifactService;
    private final ArtifactLinkRepository artifactLinkRepository;
    private final UseCaseService useCaseService;
    private final DocumentTemplateRegistry documentTemplateRegistry;
    private final DocumentRepository documentRepository;
    private final CommentThreadRepository commentThreadRepository;


    public DataInitializer(CourseRepository courseRepository, InstructorRepository instructorRepository, PeerEvaluationRepository peerEvaluationRepository, ActivityRepository activityRepository, PasswordEncoder passwordEncoder, UserInvitationRepository userInvitationRepository, RequirementArtifactService requirementArtifactService, ArtifactLinkRepository artifactLinkRepository, UseCaseService useCaseService, DocumentTemplateRegistry documentTemplateRegistry, DocumentRepository documentRepository, CommentThreadRepository commentThreadRepository) {
        this.courseRepository = courseRepository;
        this.instructorRepository = instructorRepository;
        this.peerEvaluationRepository = peerEvaluationRepository;
        this.activityRepository = activityRepository;
        this.passwordEncoder = passwordEncoder;
        this.userInvitationRepository = userInvitationRepository;
        this.requirementArtifactService = requirementArtifactService;
        this.artifactLinkRepository = artifactLinkRepository;
        this.useCaseService = useCaseService;
        this.documentTemplateRegistry = documentTemplateRegistry;
        this.documentRepository = documentRepository;
        this.commentThreadRepository = commentThreadRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Step 1: Create and save instructors (shared entities, not owned by courses)
        Instructor instructor1 = new Instructor("b.wei@abc.edu", "Bingyang", "Wei", "b.wei@abc.edu", "123456", true, "admin instructor");
        Instructor instructor2 = new Instructor("b.gates@abc.edu", "Bill", "Gates", "b.gates@abc.edu", "123456", true, "instructor");
        Instructor instructor3 = new Instructor("t.cook@abc.edu", "Tim", "Cook", "t.cook@abc.edu", "123456", true, "admin instructor");

        instructor1.setPassword(this.passwordEncoder.encode(instructor1.getPassword()));
        instructor2.setPassword(this.passwordEncoder.encode(instructor2.getPassword()));
        instructor3.setPassword(this.passwordEncoder.encode(instructor3.getPassword()));

        this.instructorRepository.saveAll(List.of(instructor1, instructor2, instructor3)); // They are assigned IDs after being saved

        // Step 2: Build complete Course graphs in memory
        // Courses own Criteria, Rubrics, Sections, Teams, and Students
        // Course 1
        Course course1 = new Course("COSC 40993 Senior Design", "Senior design project course for Computer Science, Computer Information Technology, and Data Science majors");
        course1.setCourseAdmin(instructor1);
        course1.addInstructor(instructor2);
        instructor1.setDefaultCourse(course1);
        instructor2.setDefaultCourse(course1);

        // Create peer evaluation criteria for course1
        Criterion criterion1 = new Criterion("Quality of work", "How do you rate the quality of this teammate's work? (1-10)", 10.00);
        Criterion criterion2 = new Criterion("Productivity", "How productive is this teammate? (1-10)", 10.00);
        Criterion criterion3 = new Criterion("Initiative", "How proactive is this teammate? (1-10)", 10.00);
        Criterion criterion4 = new Criterion("Courtesy", "Does this teammate treat others with respect? (1-10)", 10.00);
        Criterion criterion5 = new Criterion("Open-mindedness", "How well does this teammate handle criticism of their work? (1-10)", 10.00);
        Criterion criterion6 = new Criterion("Engagement in meetings", "How is this teammate's performance during meetings? (1-10)", 10.00);
        Criterion criterion7 = new Criterion("Problem-solving", "Assesses the ability to solve problems, think critically, and make decisions. (1-10)", 10.00);

        course1.addCriterion(criterion1);
        course1.addCriterion(criterion2);
        course1.addCriterion(criterion3);
        course1.addCriterion(criterion4);
        course1.addCriterion(criterion5);
        course1.addCriterion(criterion6);
        course1.addCriterion(criterion7);

        // Create rubric for course1
        Rubric rubric = new Rubric("Peer Eval Rubric v1");
        rubric.addCriterion(criterion1);
        rubric.addCriterion(criterion2);
        rubric.addCriterion(criterion3);
        rubric.addCriterion(criterion4);
        rubric.addCriterion(criterion5);
        rubric.addCriterion(criterion6);
        course1.addRubric(rubric);

        // Create section1 for course1
        Section section1 = new Section("2022-2023", LocalDate.of(2022, 8, 15), LocalDate.of(2023, 5, 1), true, DayOfWeek.MONDAY, LocalTime.of(23, 59), DayOfWeek.TUESDAY, LocalTime.of(23, 59));
        section1.setActiveWeeks(List.of("2022-W31", "2022-W32", "2022-W33", "2022-W34", "2022-W35"));
        section1.setRubric(rubric);
        section1.addInstructor(instructor1);
        section1.addInstructor(instructor2);
        course1.addSection(section1);

        // Create section2 for course1 with teams and students
        Section section2 = new Section("2023-2024", LocalDate.of(2023, 7, 31), LocalDate.of(2024, 10, 6), true, DayOfWeek.MONDAY, LocalTime.of(23, 59), DayOfWeek.TUESDAY, LocalTime.of(23, 59));
        section2.setActiveWeeks(List.of("2023-W31", "2023-W32", "2023-W33", "2023-W34", "2023-W35", "2023-W36", "2023-W37", "2023-W38", "2023-W39", "2023-W40"));
        section2.setRubric(rubric);
        section2.addInstructor(instructor1);
        section2.addInstructor(instructor2);
        instructor1.setDefaultSection(section2);
        instructor2.setDefaultSection(section2);

        // Create teams for section2
        Team team1 = new Team("Team1", "Team 1 description", "https://www.team1.com");
        team1.addInstructor(instructor1);

        Team team2 = new Team("Team2", "Team 2 description", "https://www.team2.com");
        team2.addInstructor(instructor1);

        Team team3 = new Team("Team3", "Team 3 description", "https://www.team3.com");
        team3.addInstructor(instructor1);

        // Create students for section2
        Student john = new Student("j.smith@abc.edu", "John", "Smith", "j.smith@abc.edu", "123456", true, "student");
        Student eric = new Student("e.hudson@abc.edu", "Eric", "Hudson", "e.hudson@abc.edu", "123456", true, "student");
        Student jerry = new Student("j.moon@abc.edu", "Jerry", "Moon", "j.moon@abc.edu", "123456", true, "student");

        Student woody = new Student("w.allen@abc.edu", "Woody", "Allen", "w.allen@abc.edu", "123456", true, "student");
        Student amanda = new Student("a.wagner@abc.edu", "Amanda", "Wagner", "a.wagner@abc.edu", "123456", true, "student");
        Student cora = new Student("c.manning@abc.edu", "Cora", "Manning", "c.manning@abc.edu", "123456", true, "student");
        Student agustin = new Student("a.freeman@abc.edu", "Agustin", "Freeman", "a.freeman@abc.edu", "123456", true, "student");

        Student mavis = new Student("m.huber@abc.edu", "Mavis", "Huber", "m.huber@abc.edu", "123456", true, "student");
        Student mary = new Student("m.vargas@abc.edu", "Mary", "Vargas", "m.vargas@abc.edu", "123456", true, "student");
        Student rosendo = new Student("r.maxwell@abc.edu", "Rosendo", "Maxwell", "r.maxwell@abc.edu", "123456", true, "student");
        Student jan = new Student("j.mckinney@abc.edu", "Jan", "Mckinney", "j.mckinney@abc.edu", "123456", true, "student");

        // Add some students who are not assigned to any team in section 2
        Student tracy = new Student("t.nicholson@abc.edu", "Tracy", "Nicholson", "t.nicholson@abc.edu", "123456", true, "student");
        Student arline = new Student("a.booker@abc.edu", "Arline", "Booker", "a.booker@abc.edu", "123456", true, "student");
        Student shad = new Student("s.huber@abc.edu", "Shad", "Huber", "s.huber@abc.edu", "123456", true, "student");
        Student lynne = new Student("l.deleon@abc.edu", "Lynne", "Deleon", "l.deleon@abc.edu", "123456", true, "student");

        // Encode passwords
        john.setPassword(this.passwordEncoder.encode(john.getPassword()));
        eric.setPassword(this.passwordEncoder.encode(eric.getPassword()));
        jerry.setPassword(this.passwordEncoder.encode(jerry.getPassword()));
        woody.setPassword(this.passwordEncoder.encode(woody.getPassword()));
        amanda.setPassword(this.passwordEncoder.encode(amanda.getPassword()));
        cora.setPassword(this.passwordEncoder.encode(cora.getPassword()));
        agustin.setPassword(this.passwordEncoder.encode(agustin.getPassword()));
        mavis.setPassword(this.passwordEncoder.encode(mavis.getPassword()));
        mary.setPassword(this.passwordEncoder.encode(mary.getPassword()));
        rosendo.setPassword(this.passwordEncoder.encode(rosendo.getPassword()));
        jan.setPassword(this.passwordEncoder.encode(jan.getPassword()));
        tracy.setPassword(this.passwordEncoder.encode(tracy.getPassword()));
        arline.setPassword(this.passwordEncoder.encode(arline.getPassword()));
        shad.setPassword(this.passwordEncoder.encode(shad.getPassword()));
        lynne.setPassword(this.passwordEncoder.encode(lynne.getPassword()));

        // Assign students to teams and section
        team1.addStudent(john);
        team1.addStudent(eric);
        team1.addStudent(jerry);

        team2.addStudent(woody);
        team2.addStudent(amanda);
        team2.addStudent(cora);
        team2.addStudent(agustin);

        team3.addStudent(mavis);
        team3.addStudent(mary);
        team3.addStudent(rosendo);
        team3.addStudent(jan);

        // Add teams to section2
        section2.addTeam(team1);
        section2.addTeam(team2);
        section2.addTeam(team3);

        // Add students to section2 (including those without teams)
        section2.addStudent(john);
        section2.addStudent(eric);
        section2.addStudent(jerry);
        section2.addStudent(woody);
        section2.addStudent(amanda);
        section2.addStudent(cora);
        section2.addStudent(agustin);
        section2.addStudent(mavis);
        section2.addStudent(mary);
        section2.addStudent(rosendo);
        section2.addStudent(jan);
        section2.addStudent(tracy); // Tracy is not assigned to any team
        section2.addStudent(arline); // Arline is not assigned to any team
        section2.addStudent(shad); // Shad is not assigned to any team
        section2.addStudent(lynne); // Lynne is not assigned to any team

        course1.addSection(section2);

        // Course 2
        Course course2 = new Course("CITE 30363 Web Tech", "Course project for Web Technology");
        course2.setCourseAdmin(instructor1);

        Criterion criterion8 = new Criterion("Team collaboration", "Assesses the effectiveness of team interactions, including communication, participation, contribution to discussions, willingness to help others, and overall teamwork dynamics. (1-10)", 10.00);
        course2.addCriterion(criterion8);

        // Course 3
        Course course3 = new Course("EE 30323 Capstone", "Capstone project course for EE majors");
        course3.setCourseAdmin(instructor3); // Set the course admin for course3
        instructor3.setDefaultCourse(course3); // Set the default course for instructor3

        // Create section3 for course3 with team and students
        Section section3 = new Section("EE Capstone 2024", LocalDate.of(2023, 8, 14), LocalDate.of(2024, 4, 29), true, DayOfWeek.MONDAY, LocalTime.of(23, 59), DayOfWeek.TUESDAY, LocalTime.of(23, 59));
        section3.addInstructor(instructor3);
        instructor3.setDefaultSection(section3);

        Team team4 = new Team("Team4", "Team 4 description", "https://www.team4.com");
        team4.addInstructor(instructor3);

        Student jana = new Student("j.norton@abc.edu", "Jana", "Norton", "j.norton@abc.edu", "123456", true, "student");
        Student lucius = new Student("l.osborne@abc.edu", "Lucius ", "Osborne", "l.osborne@abc.edu", "123456", true, "student");

        jana.setPassword(this.passwordEncoder.encode(jana.getPassword()));
        lucius.setPassword(this.passwordEncoder.encode(lucius.getPassword()));

        team4.addStudent(jana);
        team4.addStudent(lucius);

        section3.addTeam(team4);

        section3.addStudent(jana);
        section3.addStudent(lucius);

        course3.addSection(section3);

        // Step 3: Save all courses (which will cascade to sections, criteria, rubrics, teams, and students)
        this.courseRepository.saveAll(List.of(course1, course2, course3));

        // Update instructor default references after courses are persisted
        this.instructorRepository.saveAll(List.of(instructor1, instructor2, instructor3));

        // Step 4: Create peer evaluations (which will cascade to ratings)
        List<PeerEvaluation> peerEvaluations = new ArrayList<>();

        // 2023-W31
        // John evaluates Eric
        peerEvaluations.add(new PeerEvaluation("2023-W31", john, eric, List.of(
                new Rating(criterion1, 8.0),
                new Rating(criterion2, 10.0),
                new Rating(criterion3, 5.0),
                new Rating(criterion4, 8.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 7.0)), "Eric did a great job this week! I think he is on track.", "John wrote this private comment!"));

        // John evaluates Jerry
        peerEvaluations.add(new PeerEvaluation("2023-W31", john, jerry, List.of(
                new Rating(criterion1, 6.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 7.0),
                new Rating(criterion4, 3.0),
                new Rating(criterion5, 7.0),
                new Rating(criterion6, 10.0)), "Jerry is OK. Be responsive, please.", "Nothing to say! Commented by John!"));

        // John evaluates John
        peerEvaluations.add(new PeerEvaluation("2023-W31", john, john, List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 10.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 3.0),
                new Rating(criterion5, 8.0),
                new Rating(criterion6, 10.0)), "I (john) am doing well.", "Nothing to say! John is pretty good this week compared with last week!"));

        // Eric evaluates John
        peerEvaluations.add(new PeerEvaluation("2023-W31", eric, john, List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 10.0),
                new Rating(criterion6, 2.0)), "John's job is well done!", "I am eric."));

        // Eric evaluates Jerry
        peerEvaluations.add(new PeerEvaluation("2023-W31", eric, jerry, List.of(
                new Rating(criterion1, 4.0),
                new Rating(criterion2, 10.0),
                new Rating(criterion3, 7.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 2.0)), "Jerry's job is so so! Commented by Eric.", "I am eric."));

        // Eric evaluates Eric
        peerEvaluations.add(new PeerEvaluation("2023-W31", eric, eric, List.of(
                new Rating(criterion1, 7.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 9.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 10.0)), "As Eric, I just do my best!", "I am eric."));

        // Jerry evaluates John
        peerEvaluations.add(new PeerEvaluation("2023-W31", jerry, john, List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 9.0),
                new Rating(criterion5, 4.0),
                new Rating(criterion6, 10.0)), "John john john, go, go go!", "I am Jerry."));

        // Jerry evaluates Eric
        peerEvaluations.add(new PeerEvaluation("2023-W31", jerry, eric, List.of(
                new Rating(criterion1, 8.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 9.0),
                new Rating(criterion5, 6.0),
                new Rating(criterion6, 7.0)), "eric eric eric, go, go go!", "I am Jerry."));

        // Jerry evaluates Jerry
        peerEvaluations.add(new PeerEvaluation("2023-W31", jerry, jerry, List.of(
                new Rating(criterion1, 9.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 4.0)), "Jerry jerry jerry, go, go go!", "I am Jerry."));

        // 2023-W32
        // John evaluates Eric
        peerEvaluations.add(new PeerEvaluation("2023-W32", john, eric, List.of(
                new Rating(criterion1, 8.0),
                new Rating(criterion2, 10.0),
                new Rating(criterion3, 7.0),
                new Rating(criterion4, 8.0),
                new Rating(criterion5, 10.0),
                new Rating(criterion6, 2.0)), "Eric did a great job this week! I think he is on track.", "John wrote this private comment!"));

        // John evaluates Jerry
        peerEvaluations.add(new PeerEvaluation("2023-W32", john, jerry, List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 10.0),
                new Rating(criterion3, 7.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 7.0),
                new Rating(criterion6, 10.0)), "You consistently contributed great ideas during our team meetings and were open to feedback from others.", "Nothing to say! Commented by John!"));

        // John evaluates John
        peerEvaluations.add(new PeerEvaluation("2023-W32", john, john, List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 10.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 3.0),
                new Rating(criterion6, 4.0)), "I (john) am doing well.", "Nothing to say! John is pretty good this week compared with last week!"));

        // Eric evaluates John
        peerEvaluations.add(new PeerEvaluation("2023-W32", eric, john, List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 4.0),
                new Rating(criterion4, 4.0),
                new Rating(criterion5, 10.0),
                new Rating(criterion6, 8.0)), "You consistently contributed great ideas during our team meetings and were open to feedback from others.", "I am eric."));

        // Eric evaluates Jerry
        peerEvaluations.add(new PeerEvaluation("2023-W32", eric, jerry, List.of(
                new Rating(criterion1, 9.0),
                new Rating(criterion2, 5.0),
                new Rating(criterion3, 7.0),
                new Rating(criterion4, 8.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 10.0)), "Jerry's job is so so! Commented by Eric.", "I am eric."));

        // Eric evaluates Eric
        peerEvaluations.add(new PeerEvaluation("2023-W32", eric, eric, List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 5.0),
                new Rating(criterion3, 9.0),
                new Rating(criterion4, 9.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 5.0)), "As Eric, I just do my best!", "I am eric."));

        // Jerry evaluates John
        peerEvaluations.add(new PeerEvaluation("2023-W32", jerry, john, List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 10.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 9.0),
                new Rating(criterion5, 10.0),
                new Rating(criterion6, 10.0)), "Overall, you were a strong team player and brought valuable insights to the table. Keep up the great work!", "I am Jerry."));

        // Jerry evaluates Eric
        peerEvaluations.add(new PeerEvaluation("2023-W32", jerry, eric, List.of(
                new Rating(criterion1, 5.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 9.0),
                new Rating(criterion5, 6.0),
                new Rating(criterion6, 10.0)), "eric eric eric, go, go go!", "I am Jerry."));

        // Jerry evaluates Jerry
        peerEvaluations.add(new PeerEvaluation("2023-W32", jerry, jerry, List.of(
                new Rating(criterion1, 9.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 9.0),
                new Rating(criterion4, 9.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 6.0)), "You showed strong coding skills, though there were some instances where deeper testing of your code would have been beneficial.", "I am Jerry."));

        // 2023-W33
        // John evaluates Eric
        peerEvaluations.add(new PeerEvaluation("2023-W33", john, eric, List.of(
                new Rating(criterion1, 8.0),
                new Rating(criterion2, 10.0),
                new Rating(criterion3, 5.0),
                new Rating(criterion4, 5.0),
                new Rating(criterion5, 0.0),
                new Rating(criterion6, 0.0)), "Eric did a great job this week! I think he is on track.", "John wrote this private comment!"));

        // John evaluates Jerry
        peerEvaluations.add(new PeerEvaluation("2023-W33", john, jerry, List.of(
                new Rating(criterion1, 3.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 7.0),
                new Rating(criterion4, 9.0),
                new Rating(criterion5, 7.0),
                new Rating(criterion6, 0.0)), "Jerry is OK. Be responsive, please.", "Nothing to say! Commented by John!"));

        // John evaluates John
        peerEvaluations.add(new PeerEvaluation("2023-W33", john, john, List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 3.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 8.0),
                new Rating(criterion6, 10.0)), "I (john) am doing well.", "Nothing to say! John is pretty good this week compared with last week!"));

        // Eric evaluates John
        peerEvaluations.add(new PeerEvaluation("2023-W33", eric, john, List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 10.0),
                new Rating(criterion6, 10.0)), "You showed strong coding skills, though there were some instances where deeper testing of your code would have been beneficial.", "I am eric."));

        // Eric evaluates Jerry
        peerEvaluations.add(new PeerEvaluation("2023-W33", eric, jerry, List.of(
                new Rating(criterion1, 0.0),
                new Rating(criterion2, 10.0),
                new Rating(criterion3, 3.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 1.0)), "Jerry's job is so so! Commented by Eric.", "I am eric."));

        // Eric evaluates Eric
        peerEvaluations.add(new PeerEvaluation("2023-W33", eric, eric, List.of(
                new Rating(criterion1, 0.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 3.0),
                new Rating(criterion4, 9.0),
                new Rating(criterion5, 1.0),
                new Rating(criterion6, 10.0)), "As Eric, I just do my best!", "I am eric."));

        // Jerry evaluates John
        peerEvaluations.add(new PeerEvaluation("2023-W33", jerry, john, List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 10.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 9.0),
                new Rating(criterion5, 10.0),
                new Rating(criterion6, 10.0)), "John john john, go, go go!", "I am Jerry."));

        // Jerry evaluates Eric
        peerEvaluations.add(new PeerEvaluation("2023-W33", jerry, eric, List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 2.0),
                new Rating(criterion4, 9.0),
                new Rating(criterion5, 6.0),
                new Rating(criterion6, 10.0)), "eric eric eric, go, go go!", "I am Jerry."));

        // Jerry evaluates Jerry
        peerEvaluations.add(new PeerEvaluation("2023-W33", jerry, jerry, List.of(
                new Rating(criterion1, 6.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 7.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 10.0)), "You took the lead in organizing our project sprints and kept the team aligned. Great work!", "I am Jerry."));

        // 2023-W34
        // John evaluates Eric
        peerEvaluations.add(new PeerEvaluation("2023-W34", john, eric, List.of(
                new Rating(criterion1, 0.0),
                new Rating(criterion2, 2.0),
                new Rating(criterion3, 5.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 0.0),
                new Rating(criterion6, 7.0)), "Eric did a great job this week! I think he is on track.", "John wrote this private comment!"));

        // John evaluates Jerry
        peerEvaluations.add(new PeerEvaluation("2023-W34", john, jerry, List.of(
                new Rating(criterion1, 6.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 7.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 7.0),
                new Rating(criterion6, 5.0)), "You took the lead in organizing our project sprints and kept the team aligned. Great work!", "Nothing to say! Commented by John!"));

        // John evaluates John
        peerEvaluations.add(new PeerEvaluation("2023-W34", john, john, List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 10.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 8.0),
                new Rating(criterion6, 1.0)), "I (john) am doing well.", "Nothing to say! John is pretty good this week compared with last week!"));

        // Eric evaluates John
        peerEvaluations.add(new PeerEvaluation("2023-W34", eric, john, List.of(
                new Rating(criterion1, 5.0),
                new Rating(criterion2, 3.0),
                new Rating(criterion3, 7.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 10.0),
                new Rating(criterion6, 10.0)), "Overall, you were a strong team player and brought valuable insights to the table. Keep up the great work!", "I am eric."));

        // Eric evaluates Jerry
        peerEvaluations.add(new PeerEvaluation("2023-W34", eric, jerry, List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 0.0),
                new Rating(criterion3, 7.0),
                new Rating(criterion4, 0.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 0.0)), "Jerry's job is so so! Commented by Eric.", "I am eric."));

        // Eric evaluates Eric
        peerEvaluations.add(new PeerEvaluation("2023-W34", eric, eric, List.of(
                new Rating(criterion1, 7.0),
                new Rating(criterion2, 10.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 9.0),
                new Rating(criterion5, 6.0),
                new Rating(criterion6, 5.0)), "As Eric, I just do my best!", "I am eric."));

        // Jerry evaluates John
        peerEvaluations.add(new PeerEvaluation("2023-W34", jerry, john, List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 3.0),
                new Rating(criterion6, 10.0)), "John john john, go, go go!", "I am Jerry."));

        // Jerry evaluates Eric
        peerEvaluations.add(new PeerEvaluation("2023-W34", jerry, eric, List.of(
                new Rating(criterion1, 7.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 8.0),
                new Rating(criterion5, 6.0),
                new Rating(criterion6, 7.0)), "You've made solid contributions, but balancing individual work and team collaboration could improve slightly.", "I am Jerry."));

        // Jerry evaluates Jerry
        peerEvaluations.add(new PeerEvaluation("2023-W34", jerry, jerry, List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 10.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 7.0)), "Jerry jerry jerry, go, go go!", "I am Jerry."));

        // 2023-W35
        // John evaluates Eric
        peerEvaluations.add(new PeerEvaluation("2023-W35", john, eric, List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 10.0),
                new Rating(criterion3, 5.0),
                new Rating(criterion4, 8.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 3.0)), "Eric did a great job this week! I think he is on track.", "John wrote this private comment!"));

        // John evaluates Jerry
        peerEvaluations.add(new PeerEvaluation("2023-W35", john, jerry, List.of(
                new Rating(criterion1, 6.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 7.0),
                new Rating(criterion4, 3.0),
                new Rating(criterion5, 7.0),
                new Rating(criterion6, 3.0)), "Jerry is OK. Be responsive, please.", "Nothing to say! Commented by John!"));

        // John evaluates John
        peerEvaluations.add(new PeerEvaluation("2023-W35", john, john, List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 10.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 8.0),
                new Rating(criterion6, 10.0)), "I (john) am doing well.", "Nothing to say! John is pretty good this week compared with last week!"));

        // Eric evaluates John
        peerEvaluations.add(new PeerEvaluation("2023-W35", eric, john, List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 10.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 10.0),
                new Rating(criterion6, 9.0)), "John's job is well done!", "I am eric."));

        // Eric evaluates Jerry
        peerEvaluations.add(new PeerEvaluation("2023-W35", eric, jerry, List.of(
                new Rating(criterion1, 8.0),
                new Rating(criterion2, 10.0),
                new Rating(criterion3, 7.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 6.0)), "Jerry's job is so so! Commented by Eric.", "I am eric."));

        // Eric evaluates Eric
        peerEvaluations.add(new PeerEvaluation("2023-W35", eric, eric, List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 9.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 10.0)), "As Eric, I just do my best!", "I am eric."));

        // Jerry evaluates John
        peerEvaluations.add(new PeerEvaluation("2023-W35", jerry, john, List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 6.0),
                new Rating(criterion6, 10.0)), "John john john, go, go go!", "I am Jerry."));

        // Jerry evaluates Eric
        peerEvaluations.add(new PeerEvaluation("2023-W35", jerry, eric, List.of(
                new Rating(criterion1, 8.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 9.0),
                new Rating(criterion5, 10.0),
                new Rating(criterion6, 10.0)), "You were very approachable and always made time for discussions, which was valuable for our collaboration.", "I am Jerry."));

        // Jerry evaluates Jerry
        peerEvaluations.add(new PeerEvaluation("2023-W35", jerry, jerry, List.of(
                new Rating(criterion1, 9.0),
                new Rating(criterion2, 0.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 2.0)), "I stepped up and took initiative when we hit roadblocks, which helped the team move forward.", "I am Jerry."));

        // Woody evaluates Amanda
        peerEvaluations.add(new PeerEvaluation("2023-W31", woody, amanda, List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 10.0),
                new Rating(criterion3, 6.0),
                new Rating(criterion4, 8.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 9.0)), "Your ability to debug complex issues quickly has been invaluable to our project's success.", "You consistently deliver high-quality code, and your attention to detail is impressive."));

        // Woody evaluates Cora
        peerEvaluations.add(new PeerEvaluation("2023-W31", woody, cora, List.of(
                new Rating(criterion1, 9.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 3.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 4.0),
                new Rating(criterion6, 2.0)), "You communicate effectively and keep the team informed about your progress.", "Nothing to say!"));

        // Woody evaluates Agustin
        peerEvaluations.add(new PeerEvaluation("2023-W31", woody, agustin, List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 10.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 3.0),
                new Rating(criterion5, 8.0),
                new Rating(criterion6, 10.0)), "Your positive attitude and willingness to help others create a great team environment.", "Nothing to say! Agustin is pretty good this week compared with last week!"));

        // Woody evaluates Woody
        peerEvaluations.add(new PeerEvaluation("2023-W31", woody, woody, List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 10.0),
                new Rating(criterion6, 2.0)), "You handle feedback well and use it to improve your work continuously.", "I am woody."));

        // Amanda evaluates Woody
        peerEvaluations.add(new PeerEvaluation("2023-W31", amanda, woody, List.of(
                new Rating(criterion1, 4.0),
                new Rating(criterion2, 10.0),
                new Rating(criterion3, 7.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 2.0)), "Woody's job is so so! Commented by Amanda.", "I am Amanda."));

        // Amanda evaluates Cora
        peerEvaluations.add(new PeerEvaluation("2023-W31", amanda, cora, List.of(
                new Rating(criterion1, 7.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 9.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 10.0)), "No comments on Cora.", "Cora is commented by Amanda here."));

        // Amanda evaluates Agustin
        peerEvaluations.add(new PeerEvaluation("2023-W31", amanda, agustin, List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 2.0),
                new Rating(criterion3, 9.0),
                new Rating(criterion4, 9.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 6.0)), "Augustin, go, go go!", "I am Amanda."));

        // Amanda evaluates Amanda
        peerEvaluations.add(new PeerEvaluation("2023-W31", amanda, amanda, List.of(
                new Rating(criterion1, 8.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 9.0),
                new Rating(criterion5, 6.0),
                new Rating(criterion6, 7.0)), "I am doing my best!", "No private comment."));

        // Cora evaluates Woody
        peerEvaluations.add(new PeerEvaluation("2023-W31", cora, woody, List.of(
                new Rating(criterion1, 9.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 4.0)), "Woody Woody Woody, go, go go!", "I am cora."));

        // Cora evaluates Amanda
        peerEvaluations.add(new PeerEvaluation("2023-W31", cora, amanda, List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 10.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 10.0),
                new Rating(criterion6, 10.0)), "amanda amanda amanda, go, go go!", "Amanda is the GOAT."));

        // Cora evaluates Agustin
        peerEvaluations.add(new PeerEvaluation("2023-W31", cora, agustin, List.of(
                new Rating(criterion1, 9.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 4.0)), "I noticed that sometimes you struggle with time management; maybe setting smaller milestones could help!", "I am Cora."));

        // Cora evaluates Cora
        peerEvaluations.add(new PeerEvaluation("2023-W31", cora, cora, List.of(
                new Rating(criterion1, 9.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 4.0)), "I am happy about my progress this week!", "Nothing."));

        // Agustin evaluates Woody
        peerEvaluations.add(new PeerEvaluation("2023-W31", agustin, woody, List.of(
                new Rating(criterion1, 3.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 9.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 1.0),
                new Rating(criterion6, 6.0)), "Try to delegate tasks more effectively to avoid bottlenecks!", "Try to delegate tasks more effectively to avoid bottlenecks."));

        // Agustin evaluates Amanda
        peerEvaluations.add(new PeerEvaluation("2023-W31", agustin, amanda, List.of(
                new Rating(criterion1, 9.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 4.0),
                new Rating(criterion4, 9.0),
                new Rating(criterion5, 5.0),
                new Rating(criterion6, 10.0)), "our designs are innovative, but they sometimes lack scalability; keeping future growth in mind could improve them.", "I am Agustin."));

        // Agustin evaluates Cora
        peerEvaluations.add(new PeerEvaluation("2023-W31", agustin, cora, List.of(
                new Rating(criterion1, 9.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 4.0)), "You have great ideas, but sometimes they aren't clearly communicated; consider working on your presentation skills.", "I am Agustin."));

        // Agustin evaluates Agustin
        peerEvaluations.add(new PeerEvaluation("2023-W31", agustin, agustin, List.of(
                new Rating(criterion1, 9.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 4.0)), "Improving your test coverage would enhance the reliability of the code.", "I am OK."));

        // Mavis evaluates Mary
        peerEvaluations.add(new PeerEvaluation("2023-W31", mavis, mary, List.of(
                new Rating(criterion1, 9.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 4.0)), "There have been a few instances where your code was merged without proper testing; ensuring thorough testing is crucial.", "I am Mavis."));

        // Mavis evaluates Rosendo
        peerEvaluations.add(new PeerEvaluation("2023-W31", mavis, rosendo, List.of(
                new Rating(criterion1, 10.0),
                new Rating(criterion2, 10.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 10.0),
                new Rating(criterion6, 9.0)), "It would be helpful if you could provide more regular updates on your progress.", "Rosendo is the GOAT."));

        // Mavis evaluates Jan
        peerEvaluations.add(new PeerEvaluation("2023-W31", mavis, jan, List.of(
                new Rating(criterion1, 7.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 10.0),
                new Rating(criterion6, 3.0)), "I noticed that sometimes you struggle with time management; maybe setting smaller milestones could help!", "I am Mavis."));

        // Mavis evaluates Mavis
        peerEvaluations.add(new PeerEvaluation("2023-W31", mavis, mavis, List.of(
                new Rating(criterion1, 9.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 10.0)), "I am happy about my progress this week!", "Nothing."));

        // Mary evaluates Mavis
        peerEvaluations.add(new PeerEvaluation("2023-W31", mary, mavis, List.of(
                new Rating(criterion1, 6.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 9.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 2.0),
                new Rating(criterion6, 6.0)), "Try to delegate tasks more effectively to avoid bottlenecks!", "Well, Mavis is slow at replying emails."));

        // Mary evaluates Rosendo
        peerEvaluations.add(new PeerEvaluation("2023-W31", mary, rosendo, List.of(
                new Rating(criterion1, 9.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 9.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 10.0)), "His designs are innovative, but they sometimes lack scalability; keeping future growth in mind could improve them.", "Rosendo is great."));

        // Mary evaluates Jan
        peerEvaluations.add(new PeerEvaluation("2023-W31", mary, jan, List.of(
                new Rating(criterion1, 2.0),
                new Rating(criterion2, 2.0),
                new Rating(criterion3, 6.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 9.0),
                new Rating(criterion6, 6.0)), "Jan has great ideas, but sometimes they aren't clearly communicated; consider working on your presentation skills.", "Jan needs to be more friendly to teammates."));

        // Mary evaluates Mary
        peerEvaluations.add(new PeerEvaluation("2023-W31", mary, mary, List.of(
                new Rating(criterion1, 9.0),
                new Rating(criterion2, 9.0),
                new Rating(criterion3, 10.0),
                new Rating(criterion4, 10.0),
                new Rating(criterion5, 10.0),
                new Rating(criterion6, 7.0)), "Improving my test coverage would enhance the reliability of the code.", "I am OK."));

        // Save all peer evaluations (which will cascade to ratings)
        this.peerEvaluationRepository.saveAll(peerEvaluations);

        // Step 5: Create and save weekly activities (independent entities, not cascaded)
        List<Activity> activities = new ArrayList<>();

        activities.add(new Activity(john, "2023-W31", team1, ActivityCategory.DEVELOPMENT, "Develop Login Feature", "Implement login functionality for the application", 12.0, 10.5, ActivityStatus.IN_PROGRESS));
        activities.add(new Activity(john, "2023-W32", team1, ActivityCategory.TESTING, "Unit Testing for Login", "Write and execute unit tests for the login", 8.0, 9.0, ActivityStatus.COMPLETED));
        activities.add(new Activity(eric, "2023-W31", team1, ActivityCategory.DOCUMENTATION, "Create API Documentation", "Document all API endpoints and their usage", 6.0, 5.5, ActivityStatus.IN_PROGRESS));
        activities.add(new Activity(eric, "2023-W32", team1, ActivityCategory.DESIGN, "UI Mockups for Dashboard", "Design user interface mockups for the admin dashboard", 10.0, 10.5, ActivityStatus.IN_PROGRESS));
        activities.add(new Activity(jerry, "2023-W31", team1, ActivityCategory.COMMUNICATION, "Weekly Team Meeting", "Attend and contribute to the weekly team progress meeting", 2.0, 1.5, ActivityStatus.COMPLETED));
        activities.add(new Activity(woody, "2023-W31", team2, ActivityCategory.BUGFIX, "Fix Navigation Bugs", "Identify and fix bugs in the navigation menu", 4.0, 4.5, ActivityStatus.COMPLETED));
        activities.add(new Activity(amanda, "2023-W31", team2, ActivityCategory.DEPLOYMENT, "Deploy Initial Version", "Deploy the initial version of the application to the staging environment", 5.0, 6.0, ActivityStatus.IN_PROGRESS));
        activities.add(new Activity(mavis, "2023-W31", team3, ActivityCategory.LEARNING, "Learn Vue Basics", "Complete an online course on the basics of Vue.js", 3.0, 3.0, ActivityStatus.COMPLETED));
        activities.add(new Activity(john, "2023-W31", team1, ActivityCategory.TESTING, "Test Payment Integration", "Perform unit and integration testing for the payment module", 8.0, 7.0, ActivityStatus.COMPLETED));
        activities.add(new Activity(john, "2023-W32", team1, ActivityCategory.BUGFIX, "Fix Session Timeout Bug", "Resolve the issue causing session timeout after login", 5.0, 4.0, ActivityStatus.COMPLETED));
        activities.add(new Activity(john, "2023-W32", team1, ActivityCategory.COMMUNICATION, "Client Meeting", "Discuss requirements and feedback with the client for the next sprint", 2.0, 2.0, ActivityStatus.COMPLETED));
        activities.add(new Activity(john, "2023-W33", team1, ActivityCategory.DOCUMENTATION, "Write API Documentation", "Document API endpoints for external integration", 6.0, 5.5, ActivityStatus.IN_PROGRESS));
        activities.add(new Activity(john, "2023-W33", team1, ActivityCategory.DESIGN, "Design Database Schema", "Create and finalize the database schema for user management", 10.0, 9.0, ActivityStatus.IN_PROGRESS));
        activities.add(new Activity(john, "2023-W34", team1, ActivityCategory.PLANNING, "Sprint Planning", "Plan the tasks and priorities for Sprint 3", 3.0, 3.0, ActivityStatus.COMPLETED));
        activities.add(new Activity(john, "2023-W34", team1, ActivityCategory.LEARNING, "Learn OAuth2", "Study OAuth2 for secure authentication implementation", 4.0, 4.0, ActivityStatus.COMPLETED));
        activities.add(new Activity(john, "2023-W35", team1, ActivityCategory.DEPLOYMENT, "Deploy to Staging", "Deploy the latest build to the staging environment", 2.5, 2.5, ActivityStatus.COMPLETED));
        activities.add(new Activity(john, "2023-W35", team1, ActivityCategory.SUPPORT, "Support QA Team", "Assist the QA team with testing scenarios and environment setup", 5.0, 4.0, ActivityStatus.IN_PROGRESS));
        activities.add(new Activity(john, "2023-W36", team1, ActivityCategory.TESTING, "Test User Authentication", "Write unit tests for the authentication module", 6.0, 5.0, ActivityStatus.COMPLETED));
        activities.add(new Activity(john, "2023-W36", team1, ActivityCategory.BUGFIX, "Fix CSS Layout Issue", "Resolve UI layout issues on smaller screen sizes", 3.0, 2.5, ActivityStatus.COMPLETED));
        activities.add(new Activity(john, "2023-W37", team1, ActivityCategory.COMMUNICATION, "Team Sync-Up", "Weekly team sync-up meeting to review progress", 1.5, 1.5, ActivityStatus.COMPLETED));
        activities.add(new Activity(john, "2023-W37", team1, ActivityCategory.DEVELOPMENT, "Develop Signup Feature", "Implement signup functionality for the application", 10.0, 8.5, ActivityStatus.IN_PROGRESS));
        activities.add(new Activity(john, "2023-W38", team1, ActivityCategory.PLANNING, "Backlog Grooming", "Refine the product backlog and prepare for Sprint 4", 2.0, 2.0, ActivityStatus.COMPLETED));
        activities.add(new Activity(john, "2023-W38", team1, ActivityCategory.LEARNING, "Learn Docker", "Study Docker basics for containerizing the application", 5.0, 5.0, ActivityStatus.COMPLETED));
        activities.add(new Activity(john, "2023-W39", team1, ActivityCategory.DEPLOYMENT, "Deploy to Production", "Deploy the application to the production environment", 3.0, 3.0, ActivityStatus.COMPLETED));
        activities.add(new Activity(john, "2023-W39", team1, ActivityCategory.SUPPORT, "Support Live Deployment", "Monitor the application and resolve issues post-deployment", 4.0, 4.0, ActivityStatus.IN_PROGRESS));
        activities.add(new Activity(john, "2023-W40", team1, ActivityCategory.DOCUMENTATION, "Update User Guide", "Update the user guide with new features from the latest release", 4.5, 4.0, ActivityStatus.IN_PROGRESS));
        activities.add(new Activity(john, "2023-W40", team1, ActivityCategory.BUGFIX, "Fix API Response Bug", "Resolve the issue with incorrect data in API responses", 5.0, 4.0, ActivityStatus.IN_PROGRESS));
        this.activityRepository.saveAll(activities);

        UserInvitation userInvitationForElon = new UserInvitation("e.musk@abc.edu", 1, 2, "registrationToken", "instructor");
        UserInvitation userInvitationForLucas = new UserInvitation("l.santos@abc.edu", 1, 2, "registrationToken", "student");
        this.userInvitationRepository.saveAll(List.of(userInvitationForElon, userInvitationForLucas));

        // Initialize some requirement artifacts and use cases for team1 in section1 of course1
        RequirementArtifact createUseCase = new RequirementArtifact(team1, RequirementArtifactType.USE_CASE, "Create a use case", "The Student wants to create a new use case in the use case document so that a new user requirement is added in the document.", "");
        createUseCase.setPriority(Priority.CRITICAL);
        RequirementArtifact stakeholder1 = new RequirementArtifact(team1, RequirementArtifactType.STAKEHOLDER, "Student", "A student in the senior design team.", "");
        RequirementArtifact stakeholder2 = new RequirementArtifact(team1, RequirementArtifactType.STAKEHOLDER, "Instructor", "An instructor of the senior design project.", "");

        this.requirementArtifactService.saveRequirementArtifact(1, stakeholder1);
        this.requirementArtifactService.saveRequirementArtifact(1, stakeholder2);

        RequirementArtifact precondition1 = new RequirementArtifact(team1, RequirementArtifactType.PRECONDITION, "", "The Student is logged into the System.", "");
        RequirementArtifact precondition2 = new RequirementArtifact(team1, RequirementArtifactType.PRECONDITION, "", "The Student is a member of the team that owns the use case document.", "");
        RequirementArtifact precondition3 = new RequirementArtifact(team1, RequirementArtifactType.PRECONDITION, "", "Document is not locked for review.", "");
        RequirementArtifact postcondition1 = new RequirementArtifact(team1, RequirementArtifactType.POSTCONDITION, "", "The new use case is stored in the System.", "");
        RequirementArtifact postcondition2 = new RequirementArtifact(team1, RequirementArtifactType.POSTCONDITION, "", "The System records collaboration metadata (e.g., last-modified timestamp and editor identity).", "");

        UseCase useCase = new UseCase();
        useCase.setUseCaseTrigger("The Student indicates to create a new use case in the use case document.");
        useCase.setArtifact(createUseCase);

        useCase.setPrimaryActor(stakeholder1);
        useCase.addSecondaryActor(stakeholder2);

        useCase.addPrecondition(precondition1);
        useCase.addPrecondition(precondition2);
        useCase.addPrecondition(precondition3);
        useCase.addPostcondition(postcondition1);
        useCase.addPostcondition(postcondition2);

        UseCaseMainStep step1 = new UseCaseMainStep("Student", "The Student indicates to create a new use case in the use case document.");
        UseCaseMainStep step2 = new UseCaseMainStep("System", "The System asks the Student to enter the details of this new use case according to the “Details” defined in the Associated Information of this use case.");
        UseCaseMainStep step3 = new UseCaseMainStep("Student", "The Student enters the details of this new use case and confirms that she has finished.");
        UseCaseMainStep step4 = new UseCaseMainStep("System", "The System validates the Student’s inputs according to the “Details” defined in the Associated Information of this use case.");
        UseCaseMainStep step5 = new UseCaseMainStep("System", "The System validates that the creation of the new use case will not duplicate any existing use case according to the “Duplication detection rules” defined in the Associated Information of this use case.");
        UseCaseMainStep step6 = new UseCaseMainStep("System", "The System displays the details of the new use case and asks the Student to confirm the creation.");
        UseCaseMainStep step7 = new UseCaseMainStep("Student", "The Student either confirms the creation (continues the normal flow) or chooses to modify the details (returns to step 3).");
        UseCaseMainStep step8 = new UseCaseMainStep("System", "The System saves the new use case and informs the Student that this use case has been created.");
        UseCaseMainStep step9 = new UseCaseMainStep("System", "The System notifies relevant actors about the creation of the use case according to the “Notification” defined in the Associated Information of this use case.");
        UseCaseMainStep step10 = new UseCaseMainStep("", "Use case ends.");

        useCase.addMainStep(step1);
        useCase.addMainStep(step2);
        useCase.addMainStep(step3);
        useCase.addMainStep(step4);
        useCase.addMainStep(step5);
        useCase.addMainStep(step6);
        useCase.addMainStep(step7);
        useCase.addMainStep(step8);
        useCase.addMainStep(step9);
        useCase.addMainStep(step10);

        UseCaseExtension step4ExtensionA = new UseCaseExtension("Input validation rule violation", ExtensionKind.EXCEPTION, ExtensionExit.RESUME);
        step4ExtensionA.addStep(new UseCaseExtensionStep("System", "The System alerts the Student that an input validation rule is violated and displays the nature and location of the error."));
        step4ExtensionA.addStep(new UseCaseExtensionStep("Student", "The Student corrects the mistake and returns to step 4 of the normal flow."));
        step4.addExtension(step4ExtensionA);

        UseCaseExtension step5ExtensionA = new UseCaseExtension("The System finds possible duplicates from the existing use cases", ExtensionKind.EXCEPTION, ExtensionExit.RESUME);
        step5ExtensionA.addStep(new UseCaseExtensionStep("System", "The System alerts the Student that the use case she is trying to create already exists in the System."));
        step5ExtensionA.addStep(new UseCaseExtensionStep("Student", "The Student either chooses to correct the mistake and return to step 4 of the normal flow or chooses to terminate the use case."));
        step5.addExtension(step5ExtensionA);

        this.useCaseService.saveUseCase(1, useCase);

        // Create a requirement document
        RequirementDocument doc = new RequirementDocument();
        doc.setType(DocumentType.SRS);
        doc.setTeam(team1);
        doc.setDocumentKey(DocumentType.SRS.name());

        this.documentTemplateRegistry.find(DocumentType.SRS).ifPresentOrElse(
                tpl -> applyTemplate(doc, tpl),
                () -> doc.setTitle(DocumentType.SRS.name()) // fallback title if no template file exists
        );

        this.documentRepository.save(doc);

        // Create some requirement artifacts
        RequirementArtifact fr1 = new RequirementArtifact(team1, RequirementArtifactType.FUNCTIONAL_REQUIREMENT, "User authentication", "The system shall allow users to log in using their email and password.", "");
        fr1.setPriority(Priority.CRITICAL);
        RequirementArtifact fr2 = new RequirementArtifact(team1, RequirementArtifactType.FUNCTIONAL_REQUIREMENT, "Validation", "The system shall validate user inputs to ensure data integrity and prevent errors.", "");
        fr2.setPriority(Priority.CRITICAL);
        RequirementArtifact fr3 = new RequirementArtifact(team1, RequirementArtifactType.FUNCTIONAL_REQUIREMENT, "Notification", "The system shall notify team members of changes to the requirement document via email.", "");
        fr3.setPriority(Priority.HIGH);
        RequirementArtifact bo1 = new RequirementArtifact(team1, RequirementArtifactType.BUSINESS_OBJECTIVE, "Improve Student Requirements Quality", "Improve the quality of student-written requirements through structure, templates, standards, traceability, AI feedback, and consistency checks.", "");
        bo1.setPriority(Priority.CRITICAL);

        this.requirementArtifactService.saveRequirementArtifact(1, fr1);
        this.requirementArtifactService.saveRequirementArtifact(1, fr2);
        this.requirementArtifactService.saveRequirementArtifact(1, fr3);
        this.requirementArtifactService.saveRequirementArtifact(1, bo1);

        // Find the "NON_USE_CASE_FUNCTIONAL_REQUIREMENTS" section in the document and add the functional requirements to it
        doc.getSections().stream()
                .filter(section -> "NON_USE_CASE_FUNCTIONAL_REQUIREMENTS".equals(section.getSectionKey()))
                .findFirst()
                .ifPresent(section -> {
                    section.addRequirementArtifact(fr1);
                    section.addRequirementArtifact(fr2);

                });

        // Find the "INTRODUCTION" section in the document and lock it.
        doc.getSections().stream()
                .filter(section -> "INTRODUCTION".equals(section.getSectionKey()))
                .findFirst()
                .ifPresent(section -> {
                    DocumentSectionLock sectionLock = section.getLock();
                    PeerEvaluationUser currentUser = john;
                    Instant now = Instant.now();
                    Instant expiresAt = now.plus(Duration.ofMinutes(15));
                    sectionLock.lock(currentUser, now, expiresAt, "Locking section for editing");
                });

        this.documentRepository.save(doc);

        ArtifactLink link1 = new ArtifactLink();
        link1.setTeam(team1);
        link1.setSourceArtifact(fr1);
        link1.setTargetArtifact(createUseCase);
        link1.setType(ArtifactLinkType.DERIVES_FROM);
        ArtifactLink link2 = new ArtifactLink();
        link2.setTeam(team1);
        link2.setSourceArtifact(fr2);
        link2.setTargetArtifact(createUseCase);
        link2.setType(ArtifactLinkType.DERIVES_FROM);
        ArtifactLink link3 = new ArtifactLink();
        link3.setTeam(team1);
        link3.setSourceArtifact(createUseCase);
        link3.setTargetArtifact(bo1);
        link3.setType(ArtifactLinkType.DERIVES_FROM);

        this.artifactLinkRepository.saveAll(List.of(link1, link2, link3));

        // ---- Doc-level thread #1 (OPEN) ----
        CommentThread docThread1 = new CommentThread();
        docThread1.setTeam(team1);
        docThread1.setCreatedBy(john);
        doc.addCommentThread(docThread1);

        Comment docThread1c1 = new Comment();
        docThread1c1.setAuthor(john);
        docThread1c1.setContent("Please add a glossary term for 'artifact' and maybe 'traceability'.");

        Comment docThread1c2 = new Comment();
        docThread1c2.setAuthor(instructor1);
        docThread1c2.setContent("Agree. Add definitions + one example each.");

        docThread1.addComment(docThread1c1);
        docThread1.addComment(docThread1c2);

        this.commentThreadRepository.save(docThread1);

        // ---- Doc-level thread #2 (RESOLVED) ----
        CommentThread docThread2 = new CommentThread();
        docThread2.setTeam(team1);
        docThread2.setCreatedBy(instructor2);
        docThread2.setStatus(CommentThreadStatus.RESOLVED);
        doc.addCommentThread(docThread2);

        Comment docThread2c1 = new Comment();
        docThread2c1.setAuthor(instructor2);
        docThread2c1.setContent("Consider adding a short Scope paragraph to the Introduction.");

        Comment docThread2c2 = new Comment();
        docThread2c2.setAuthor(jerry);
        docThread2c2.setContent("Done. Added a scope paragraph. Resolving thread.");

        docThread2.addComment(docThread2c1);
        docThread2.addComment(docThread2c2);

        this.commentThreadRepository.save(docThread2);

        // ---- Section-level thread (OPEN) ----
        CommentThread sectionThread1 = new CommentThread();
        sectionThread1.setTeam(team1);
        sectionThread1.setCreatedBy(eric);
        doc.addCommentThread(sectionThread1);
        DocumentSection introSection = doc.getSections().stream()
                .filter(s -> "INTRODUCTION".equals(s.getSectionKey()))
                .findFirst()
                .get();
        introSection.addCommentThread(sectionThread1);

        Comment sectionThread1c1 = new Comment();
        sectionThread1c1.setAuthor(eric);
        sectionThread1c1.setContent("Please include the purpose of this document in the Introduction section.");

        Comment sectionThread1c2 = new Comment();
        sectionThread1c2.setAuthor(instructor1);
        sectionThread1c2.setContent("Sure. Added a Purpose subsection under Introduction.");

        sectionThread1.addComment(sectionThread1c1);
        sectionThread1.addComment(sectionThread1c2);

        this.commentThreadRepository.save(sectionThread1);

        // ---- Artifact-level thread #1 (OPEN) on FR-01 ----
        CommentThread artifactThread1 = new CommentThread();
        artifactThread1.setTeam(team1);
        artifactThread1.setCreatedBy(woody);
        fr1.addCommentThread(artifactThread1);

        Comment artifactThread1c1 = new Comment();
        artifactThread1c1.setAuthor(woody);
        artifactThread1c1.setContent("What does 'log in' mean—email/password only or also SSO?");

        Comment artifactThread1c2 = new Comment();
        artifactThread1c2.setAuthor(amanda);
        artifactThread1c2.setContent("Let’s specify email/password now; add SSO as a later requirement.");

        artifactThread1.addComment(artifactThread1c1);
        artifactThread1.addComment(artifactThread1c2);

        this.commentThreadRepository.save(artifactThread1);

        // ---- Artifact-level thread #2 (RESOLVED) on FR-02 ----
        CommentThread artifactThread2 = new CommentThread();
        artifactThread2.setTeam(team1);
        artifactThread2.setCreatedBy(instructor1);
        artifactThread2.setStatus(CommentThreadStatus.RESOLVED);
        fr2.addCommentThread(artifactThread2);

        Comment artifactThread2c1 = new Comment();
        artifactThread2c1.setAuthor(instructor1);
        artifactThread2c1.setContent("FR-02 is too broad. Add examples or measurable validation rules.");

        Comment artifactThread2c2 = new Comment();
        artifactThread2c2.setAuthor(cora);
        artifactThread2c2.setContent("Added examples: required fields + email format + length checks. Resolving.");

        artifactThread2.addComment(artifactThread2c1);
        artifactThread2.addComment(artifactThread2c2);

        this.commentThreadRepository.save(artifactThread2);
    }

    private void applyTemplate(RequirementDocument doc, DocumentTemplate tpl) {
        doc.setTitle(tpl.getTitle() != null ? tpl.getTitle() : doc.getType().name());

        if (tpl.getSections() == null || tpl.getSections().isEmpty()) {
            return;
        }

        for (DocumentTemplate.SectionTemplate sectionTemplate : tpl.getSections()) {
            DocumentSection section = new DocumentSection();
            section.setSectionKey(sectionTemplate.getSectionKey());
            section.setTitle(sectionTemplate.getTitle());
            section.setType(sectionTemplate.getType());
            section.setContent(null);
            if (sectionTemplate.getType() == SectionType.LIST) {
                section.setRequirementArtifacts(new ArrayList<>());
            }
            section.setGuidance(sectionTemplate.getGuidance());
            section.initLockIfMissing();

            doc.addSection(section);
        }
    }

}
