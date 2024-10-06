package team.projectpulse.rubric;

import team.projectpulse.course.Course;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Rubric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer rubricId;
    @NotEmpty(message = "rubric rubricName is required.")
    private String rubricName;
    @ManyToMany
    @JoinTable(name = "rubric_criterion",
            joinColumns = @JoinColumn(name = "rubric_id"),
            inverseJoinColumns = @JoinColumn(name = "criterion_id"))
    private Set<Criterion> criteria = new HashSet<>();
    @ManyToOne
    private Course course; // the course that this rubric belongs to


    public Rubric() {
    }

    public Rubric(String rubricName) {
        this.rubricName = rubricName;
    }

    public Integer getRubricId() {
        return rubricId;
    }

    public void setRubricId(Integer rubricId) {
        this.rubricId = rubricId;
    }

    public String getRubricName() {
        return rubricName;
    }

    public void setRubricName(String rubricName) {
        this.rubricName = rubricName;
    }

    public Set<Criterion> getCriteria() {
        return criteria;
    }

    public void addCriterion(Criterion criterion) {
        this.criteria.add(criterion);
    }

    public void removeCriterion(Criterion criterion) {
        this.criteria.remove(criterion);
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

}
