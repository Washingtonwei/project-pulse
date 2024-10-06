package team.projectpulse.student.converter;

import team.projectpulse.student.Student;
import team.projectpulse.student.dto.StudentDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StudentToStudentDtoConverter implements Converter<Student, StudentDto> {

    @Override
    public StudentDto convert(Student student) {
        // We are not setting password in DTO.
        return new StudentDto(
                student.getId(),
                student.getUsername(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                student.isEnabled(),
                student.getRoles(),
                student.getSection().getSectionId(),
                student.getSection().getSectionName(),
                student.getTeam() == null ? null : student.getTeam().getTeamId(),
                student.getTeam() == null ? null : student.getTeam().getTeamName()
        );
    }

}
