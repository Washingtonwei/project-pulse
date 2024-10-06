package team.projectpulse.evaluation;

import java.util.ArrayList;
import java.util.List;

public class WeeklyPeerEvaluationReport {

    private String sectionName;
    private String week;
    private List<PeerEvaluationAverage> peerEvaluationAverages = new ArrayList<>();
    private List<String> studentsMissingPeerEvaluations = new ArrayList<>();


    public WeeklyPeerEvaluationReport() {
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public List<PeerEvaluationAverage> getPeerEvaluationAverages() {
        return peerEvaluationAverages;
    }

    public void setPeerEvaluationAverages(List<PeerEvaluationAverage> peerEvaluationAverages) {
        this.peerEvaluationAverages = peerEvaluationAverages;
    }

    public List<String> getStudentsMissingPeerEvaluations() {
        return studentsMissingPeerEvaluations;
    }

    public void setStudentsMissingPeerEvaluations(List<String> studentsMissingPeerEvaluations) {
        this.studentsMissingPeerEvaluations = studentsMissingPeerEvaluations;
    }

}
