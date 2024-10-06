package team.projectpulse.user.userinvitation;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class UserInvitation {

    @Id
    private String email;
    private Integer courseId;
    private Integer sectionId;
    private String token;
    private String role;


    public UserInvitation() {
    }

    public UserInvitation(String email, Integer courseId, Integer sectionId, String token, String role) {
        this.email = email;
        this.courseId = courseId;
        this.sectionId = sectionId;
        this.token = token;
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public Integer getSectionId() {
        return sectionId;
    }

    public void setSectionId(Integer sectionId) {
        this.sectionId = sectionId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

}
