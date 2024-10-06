package team.projectpulse.instructor.converter;

import team.projectpulse.instructor.dto.InstructorDto;
import team.projectpulse.instructor.Instructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class InstructorToInstructorDtoConverter implements Converter<Instructor, InstructorDto> {

    @Override
    public InstructorDto convert(Instructor instructor) {
        return new InstructorDto(
                instructor.getId(),
                instructor.getUsername(),
                instructor.getFirstName(),
                instructor.getLastName(),
                instructor.getEmail(),
                instructor.isEnabled(),
                instructor.getRoles(),
                instructor.getDefaultSection() == null ? null : instructor.getDefaultSection().getSectionId(),
                instructor.getDefaultCourse() == null ? null : instructor.getDefaultCourse().getCourseId()
        );
    }

}
