package team.projectpulse.rubric;

import team.projectpulse.course.Course;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Entity
public class Criterion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer criterionId;
    @NotEmpty(message = "criterion is required.")
    private String criterion;
    @NotEmpty(message = "description is required.")
    private String description;
    @NotNull(message = "maxScore is required.")
    private Double maxScore;
    @ManyToOne
    private Course course; // the course that this criterion belongs to


    public Criterion() {
    }

    public Criterion(String criterion, String description, Double maxScore) {
        this.criterion = criterion;
        this.description = description;
        this.maxScore = maxScore;
    }

    public Integer getCriterionId() {
        return criterionId;
    }

    public void setCriterionId(Integer criterionId) {
        this.criterionId = criterionId;
    }

    public String getCriterion() {
        return criterion;
    }

    public void setCriterion(String criterion) {
        this.criterion = criterion;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Double maxScore) {
        this.maxScore = maxScore;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

}
