package team.projectpulse.instructor.converter;

import team.projectpulse.instructor.dto.InstructorDto;
import team.projectpulse.instructor.Instructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class InstructorDtoToInstructorConverter implements Converter<InstructorDto, Instructor> {

    @Override
    public Instructor convert(InstructorDto instructorDto) {
        Instructor instructor = new Instructor();
        instructor.setId(instructorDto.id());
        instructor.setUsername(instructorDto.username());
        instructor.setFirstName(instructorDto.firstName());
        instructor.setLastName(instructorDto.lastName());
        instructor.setEmail(instructorDto.email());
        return instructor;
    }

}
