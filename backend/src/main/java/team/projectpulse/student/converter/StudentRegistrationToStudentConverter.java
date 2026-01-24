package team.projectpulse.student.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.student.Student;
import team.projectpulse.student.dto.StudentRegistrationRequest;

@Component
public class StudentRegistrationToStudentConverter implements Converter<StudentRegistrationRequest, Student> {

    @Override
    public Student convert(StudentRegistrationRequest source) {
        Student student = new Student();
        student.setUsername(source.username());
        student.setFirstName(source.firstName());
        student.setLastName(source.lastName());
        student.setEmail(source.email());
        student.setPassword(source.password());
        return student;
    }
    
}
