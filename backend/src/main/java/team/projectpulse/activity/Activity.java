package team.projectpulse.activity;

import team.projectpulse.student.Student;
import team.projectpulse.team.Team;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer activityId;
    @ManyToOne
    private Student student;
    String week;
    @ManyToOne
    private Team team;
    ActivityCategory category;
    String activity;
    String description;
    Double plannedHours;
    Double actualHours;
    ActivityStatus status;
    String comments; // comments on this activity from team members
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public Activity() {
    }

    public Activity(Student student, String week, Team team, ActivityCategory category, String activity, String description, Double plannedHours, Double actualHours, ActivityStatus status) {
        this.student = student;
        this.week = week;
        this.team = team;
        this.category = category;
        this.activity = activity;
        this.description = description;
        this.plannedHours = plannedHours;
        this.actualHours = actualHours;
        this.status = status;
    }

    public Integer getActivityId() {
        return activityId;
    }

    public void setActivityId(Integer activityId) {
        this.activityId = activityId;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public ActivityCategory getCategory() {
        return category;
    }

    public void setCategory(ActivityCategory category) {
        this.category = category;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPlannedHours() {
        return plannedHours;
    }

    public void setPlannedHours(Double plannedHours) {
        this.plannedHours = plannedHours;
    }

    public Double getActualHours() {
        return actualHours;
    }

    public void setActualHours(Double actualHours) {
        this.actualHours = actualHours;
    }

    public ActivityStatus getStatus() {
        return status;
    }

    public void setStatus(ActivityStatus status) {
        this.status = status;
    }

    public String getComments() {
        return comments;
    }

    public void addComment(String comment) {
        if (this.comments == null) {
            this.comments = comment;
        } else {
            this.comments += " " + comment;
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @PrePersist // automatically set createdAt and updatedAt when persisting
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}
