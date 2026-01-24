package team.projectpulse.instructor;

import team.projectpulse.instructor.converter.InstructorDtoToInstructorConverter;
import team.projectpulse.instructor.converter.InstructorRegistrationRequestToInstructorConverter;
import team.projectpulse.instructor.converter.InstructorToInstructorDtoConverter;
import team.projectpulse.instructor.dto.InstructorDto;
import team.projectpulse.instructor.dto.InstructorRegistrationRequest;
import team.projectpulse.system.Result;
import team.projectpulse.system.StatusCode;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("${api.endpoint.base-url}/instructors")
public class InstructorController {

    private final InstructorService instructorService;
    private final InstructorRegistrationRequestToInstructorConverter registrationRequestToInstructorConverter;
    private final InstructorToInstructorDtoConverter instructorToInstructorDtoConverter;
    private final InstructorDtoToInstructorConverter instructorDtoToInstructorConverter;


    public InstructorController(InstructorService instructorService, InstructorRegistrationRequestToInstructorConverter registrationRequestToInstructorConverter, InstructorToInstructorDtoConverter instructorToInstructorDtoConverter, InstructorDtoToInstructorConverter instructorDtoToInstructorConverter) {
        this.instructorService = instructorService;
        this.registrationRequestToInstructorConverter = registrationRequestToInstructorConverter;
        this.instructorToInstructorDtoConverter = instructorToInstructorDtoConverter;
        this.instructorDtoToInstructorConverter = instructorDtoToInstructorConverter;
    }

    @PostMapping("/search")
    public Result findInstructorsByCriteria(@RequestBody Map<String, String> searchCriteria, Pageable pageable) {
        Page<Instructor> instructorPage = this.instructorService.findByCriteria(searchCriteria, pageable);
        Page<InstructorDto> instructorDtoPage = instructorPage.map(this.instructorToInstructorDtoConverter::convert);
        return new Result(true, StatusCode.SUCCESS, "Find instructors successfully", instructorDtoPage);
    }

    @GetMapping("/{instructorId}")
    public Result findInstructorById(@PathVariable Integer instructorId) {
        Instructor instructor = this.instructorService.findInstructorById(instructorId);
        InstructorDto instructorDto = this.instructorToInstructorDtoConverter.convert(instructor);
        return new Result(true, StatusCode.SUCCESS, "Find instructor successfully", instructorDto);
    }
    
    @PostMapping
    public Result addInstructor(@RequestParam Integer courseId, @RequestParam Integer sectionId, @RequestParam String registrationToken, @RequestParam String role, @Valid @RequestBody InstructorRegistrationRequest registrationRequest) {
        Instructor newInstructor = this.registrationRequestToInstructorConverter.convert(registrationRequest);
        Instructor savedInstructor = this.instructorService.saveInstructor(newInstructor, courseId, sectionId, registrationToken, role);
        InstructorDto savedInstructorDto = this.instructorToInstructorDtoConverter.convert(savedInstructor);
        return new Result(true, StatusCode.SUCCESS, "Add instructor successfully", savedInstructorDto);
    }

    @PutMapping("/{instructorId}")
    public Result updateInstructor(@PathVariable Integer instructorId, @Valid @RequestBody InstructorDto instructorDto) {
        Instructor update = this.instructorDtoToInstructorConverter.convert(instructorDto);
        Instructor updatedInstructor = this.instructorService.updateInstructor(instructorId, update);
        InstructorDto updatedInstructorDto = this.instructorToInstructorDtoConverter.convert(updatedInstructor);
        return new Result(true, StatusCode.SUCCESS, "Update instructor successfully", updatedInstructorDto);
    }

    @DeleteMapping("/{instructorId}")
    public Result deleteInstructor(@PathVariable Integer instructorId) {
        this.instructorService.deleteInstructor(instructorId);
        return new Result(true, StatusCode.SUCCESS, "Delete instructor successfully", null);
    }

    @PutMapping("/sections/{sectionId}/default")
    public Result setDefaultSection(@PathVariable Integer sectionId) {
        this.instructorService.setDefaultSection(sectionId);
        return new Result(true, StatusCode.SUCCESS, "Set default section successfully", null);
    }

    @PutMapping("/courses/{courseId}/default")
    public Result setDefaultCourse(@PathVariable Integer courseId) {
        this.instructorService.setDefaultCourse(courseId);
        return new Result(true, StatusCode.SUCCESS, "Set default course successfully", null);
    }

}
