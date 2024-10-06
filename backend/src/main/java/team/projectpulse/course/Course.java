package team.projectpulse.course;

import team.projectpulse.instructor.Instructor;
import team.projectpulse.rubric.Criterion;
import team.projectpulse.rubric.Rubric;
import team.projectpulse.section.Section;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer courseId;
    private String courseName;
    private String courseDescription;
    @OneToMany(mappedBy = "course")
    private List<Section> sections = new ArrayList<>();
    @ManyToOne
    private Instructor courseAdmin; // the instructor who is the admin of this course
    @ManyToMany
    @JoinTable(name = "course_instructor",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "instructor_id"))
    private Set<Instructor> instructors = new HashSet<>(); // the instructors who are teaching this course
    @OneToMany(mappedBy = "course")
    private List<Criterion> criteria = new ArrayList<>();
    @OneToMany(mappedBy = "course")
    private List<Rubric> rubrics = new ArrayList<>();

    public Course() {
    }

    public Course(String courseName, String courseDescription) {
        this.courseName = courseName;
        this.courseDescription = courseDescription;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseDescription() {
        return courseDescription;
    }

    public void setCourseDescription(String courseDescription) {
        this.courseDescription = courseDescription;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void addSection(Section section) {
        this.sections.add(section);
        section.setCourse(this);
    }

    public void removeSection(Section section) {
        this.sections.remove(section);
        section.setCourse(null);
    }

    public Instructor getCourseAdmin() {
        return courseAdmin;
    }

    public void setCourseAdmin(Instructor courseAdmin) {
        this.courseAdmin = courseAdmin;
        addInstructor(courseAdmin);
    }

    public Set<Instructor> getInstructors() {
        return instructors;
    }

    public void addInstructor(Instructor instructor) {
        this.instructors.add(instructor);
        instructor.getCourses().add(this);
    }

    public void removeInstructor(Instructor instructor) {
        this.instructors.remove(instructor);
        instructor.getCourses().remove(this);
    }

    public List<Criterion> getCriteria() {
        return criteria;
    }

    public void addCriterion(Criterion criterion) {
        this.criteria.add(criterion);
        criterion.setCourse(this);
    }

    public void removeCriterion(Criterion criterion) {
        this.criteria.remove(criterion);
        criterion.setCourse(null);
    }

    public List<Rubric> getRubrics() {
        return rubrics;
    }

    public void addRubric(Rubric rubric) {
        this.rubrics.add(rubric);
        rubric.setCourse(this);
    }

    public void removeRubric(Rubric rubric) {
        this.rubrics.remove(rubric);
        rubric.setCourse(null);
    }

}
