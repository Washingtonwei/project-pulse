package team.projectpulse.notification.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.notification.dto.OwingStudentDto;
import team.projectpulse.student.Student;

@Component
public class StudentToOwingStudentDtoConverter implements Converter<Student, OwingStudentDto> {

    @Override
    public OwingStudentDto convert(Student student) {
        return new OwingStudentDto(student.getId(), student.getFirstName(), student.getLastName());
    }

}
