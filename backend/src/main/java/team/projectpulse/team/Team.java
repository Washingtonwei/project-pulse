package team.projectpulse.team;

import team.projectpulse.instructor.Instructor;
import team.projectpulse.section.Section;
import team.projectpulse.student.Student;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer teamId;
    private String teamName;
    private String description;
    private String teamWebsiteUrl;
    @ManyToOne
    private Section section;
    @OneToMany(mappedBy = "team")
    private List<Student> students = new ArrayList<>();
    @ManyToOne
    private Instructor instructor;


    public Team() {
    }

    public Team(String teamName, String description, String teamWebsiteUrl) {
        this.teamName = teamName;
        this.description = description;
        this.teamWebsiteUrl = teamWebsiteUrl;
    }

    public Integer getTeamId() {
        return teamId;
    }

    public void setTeamId(Integer teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTeamWebsiteUrl() {
        return teamWebsiteUrl;
    }

    public void setTeamWebsiteUrl(String teamWebsiteUrl) {
        this.teamWebsiteUrl = teamWebsiteUrl;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public List<Student> getStudents() {
        return students;
    }

    public Instructor getInstructor() {
        return instructor;
    }

    public void addStudent(Student student) {
        this.students.add(student);
        student.setTeam(this);
    }

    public void removeStudent(Student student) {
        this.students.remove(student);
        student.setTeam(null);
    }

    public void addInstructor(Instructor instructor) {
        if (instructor == null) return;
        this.instructor = instructor;
        instructor.getTeams().add(this);
    }

    public void removeInstructor(Instructor instructor) {
        if (instructor == null) return;
        this.instructor = null;
        instructor.getTeams().remove(this);
    }

}
