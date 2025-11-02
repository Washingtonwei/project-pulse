package team.projectpulse.evaluation;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import team.projectpulse.rubric.Rating;
import team.projectpulse.student.Student;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class PeerEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer peerEvaluationId;
    private String week;
    @ManyToOne
    private Student evaluator;
    @ManyToOne
    private Student evaluatee;
    @OneToMany(mappedBy = "peerEvaluation", cascade = CascadeType.ALL)
    // automatically persist ratings when persisting peer evaluation
    private List<Rating> ratings = new ArrayList<>();
    private Double totalScore;
    private String publicComment;
    private String privateComment;
    @CreatedDate
    @Column(updatable = false) // never changed after insert
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;


    public PeerEvaluation() {
    }

    public PeerEvaluation(String week, Student evaluator, Student evaluatee, List<Rating> ratings, String publicComment, String privateComment) {
        this.week = week;
        this.evaluator = evaluator;
        this.evaluatee = evaluatee;
        setRatings(ratings);
        this.publicComment = publicComment;
        this.privateComment = privateComment;
    }

    public Integer getPeerEvaluationId() {
        return peerEvaluationId;
    }

    public void setPeerEvaluationId(Integer peerEvaluationId) {
        this.peerEvaluationId = peerEvaluationId;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public Student getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(Student evaluator) {
        this.evaluator = evaluator;
    }

    public Student getEvaluatee() {
        return evaluatee;
    }

    public void setEvaluatee(Student evaluatee) {
        this.evaluatee = evaluatee;
    }

    public List<Rating> getRatings() {
        return ratings;
    }

    public void setRatings(List<Rating> ratings) {
        this.ratings = ratings;
        this.ratings.forEach(rating -> rating.setPeerEvaluation(this));
        calculateTotalScore(); // calculate total score when ratings are set
    }

    public Double getTotalScore() {
        return totalScore;
    }

    public void calculateTotalScore() {
        this.totalScore = ratings.stream()
                .mapToDouble(Rating::getActualScore)
                .sum();
    }

    public String getPublicComment() {
        return publicComment;
    }

    public void setPublicComment(String publicComment) {
        this.publicComment = publicComment;
    }

    public String getPrivateComment() {
        return privateComment;
    }

    public void setPrivateComment(String privateComment) {
        this.privateComment = privateComment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

}
