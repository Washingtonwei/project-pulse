package team.projectpulse.instructor.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.instructor.Instructor;
import team.projectpulse.instructor.dto.InstructorRegistrationRequest;

@Component
public class InstructorRegistrationRequestToInstructorConverter implements Converter<InstructorRegistrationRequest, Instructor> {

    @Override
    public Instructor convert(InstructorRegistrationRequest source) {
        Instructor instructor = new Instructor();
        instructor.setUsername(source.username());
        instructor.setFirstName(source.firstName());
        instructor.setLastName(source.lastName());
        instructor.setEmail(source.email());
        instructor.setPassword(source.password());
        return instructor;
    }

}
