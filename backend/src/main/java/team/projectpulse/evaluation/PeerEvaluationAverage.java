package team.projectpulse.evaluation;

import java.util.List;

public class PeerEvaluationAverage {

    private Integer studentId;
    private String week;
    private String firstName;
    private String lastName;
    private String email;
    private String teamName;
    private Double averageTotalScore;
    private List<String> publicComments;
    private List<String> privateComments;
    private List<RatingAverage> ratingAverages;


    public PeerEvaluationAverage() {
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Double getAverageTotalScore() {
        return averageTotalScore;
    }

    public void setAverageTotalScore(Double averageTotalScore) {
        this.averageTotalScore = averageTotalScore;
    }

    public List<String> getPublicComments() {
        return publicComments;
    }

    public void setPublicComments(List<String> publicComments) {
        this.publicComments = publicComments;
    }

    public List<String> getPrivateComments() {
        return privateComments;
    }

    public void setPrivateComments(List<String> privateComments) {
        this.privateComments = privateComments;
    }

    public List<RatingAverage> getRatingAverages() {
        return ratingAverages;
    }

    public void setRatingAverages(List<RatingAverage> ratingAverages) {
        this.ratingAverages = ratingAverages;
    }

}
