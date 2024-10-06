package team.projectpulse.evaluation;

public class RatingAverage {

    private Integer criterionId;
    private Double averageScore;


    public RatingAverage() {
    }

    public RatingAverage(Integer criterionId, Double averageScore) {
        this.criterionId = criterionId;
        this.averageScore = averageScore;
    }

    public Integer getCriterionId() {
        return criterionId;
    }

    public void setCriterionId(Integer criterionId) {
        this.criterionId = criterionId;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

}
