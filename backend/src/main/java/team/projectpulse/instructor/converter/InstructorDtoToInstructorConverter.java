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

        /*
        Instructors themselves cannot change their enabled status or roles through the API.
        The enabled status and roles in this instructor object will be ignored when updating the instructor.
        However, if the admin updates an instructor, these fields will be set.
        See InstructorService.updateInstructor(Integer instructorId, Instructor update)
        */
        instructor.setEnabled(instructorDto.enabled());
        instructor.setRoles(instructorDto.roles());
        return instructor;
    }

}
