package team.projectpulse.rubric;

import team.projectpulse.evaluation.PeerEvaluation;
import jakarta.persistence.*;

@Entity
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer ratingId;
    @ManyToOne
    private Criterion criterion;
    private Double actualScore;
    @ManyToOne
    private PeerEvaluation peerEvaluation;


    public Rating() {
    }

    public Rating(Criterion criterion, Double actualScore) {
        this.criterion = criterion;
        setActualScore(actualScore);
    }

    public Rating(Integer ratingId, Criterion criterion, Double actualScore) {
        this.ratingId = ratingId;
        this.criterion = criterion;
        setActualScore(actualScore);
    }

    public Integer getRatingId() {
        return ratingId;
    }

    public void setRatingId(Integer ratingId) {
        this.ratingId = ratingId;
    }

    public Criterion getCriterion() {
        return criterion;
    }

    public void setCriterion(Criterion criterion) {
        this.criterion = criterion;
    }

    public Double getActualScore() {
        return actualScore;
    }

    public void setActualScore(Double actualScore) {
        if (actualScore > criterion.getMaxScore() || actualScore < 0) {
            throw new IllegalArgumentException("Actual score must be between 0 and " + criterion.getMaxScore());
        }
        this.actualScore = actualScore;
    }

    public PeerEvaluation getPeerEvaluation() {
        return peerEvaluation;
    }

    public void setPeerEvaluation(PeerEvaluation peerEvaluation) {
        this.peerEvaluation = peerEvaluation;
    }

}
