package team.projectpulse.student;

import team.projectpulse.student.converter.StudentDtoToStudentConverter;
import team.projectpulse.student.converter.StudentToStudentDtoConverter;
import team.projectpulse.student.dto.StudentDto;
import team.projectpulse.system.Result;
import team.projectpulse.system.StatusCode;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.endpoint.base-url}/students")
public class StudentController {

    private final StudentService studentService;
    private final StudentToStudentDtoConverter studentToStudentDtoConverter;
    private final StudentDtoToStudentConverter studentDtoToStudentConverter;


    public StudentController(StudentService studentService, StudentToStudentDtoConverter studentToStudentDtoConverter, StudentDtoToStudentConverter studentDtoToStudentConverter) {
        this.studentService = studentService;
        this.studentToStudentDtoConverter = studentToStudentDtoConverter;
        this.studentDtoToStudentConverter = studentDtoToStudentConverter;
    }

    @PostMapping("/search")
    public Result findStudentsByCriteria(@RequestBody Map<String, String> searchCriteria, Pageable pageable) {
        Page<Student> studentPage = this.studentService.findByCriteria(searchCriteria, pageable);
        Page<StudentDto> studentDtoPage = studentPage.map(this.studentToStudentDtoConverter::convert);
        return new Result(true, StatusCode.SUCCESS, "Find students successfully", studentDtoPage);
    }

    @GetMapping("/{studentId}")
    public Result findStudentById(@PathVariable Integer studentId) {
        Student student = this.studentService.findStudentById(studentId);
        StudentDto studentDto = this.studentToStudentDtoConverter.convert(student);
        return new Result(true, StatusCode.SUCCESS, "Find student successfully", studentDto);
    }

    @GetMapping("/teams/{teamId}")
    public Result findStudentsByTeamId(@PathVariable Integer teamId) {
        List<Student> students = this.studentService.findStudentsByTeamId(teamId);
        List<StudentDto> studentDtos = students.stream().map(this.studentToStudentDtoConverter::convert).collect(Collectors.toList());
        return new Result(true, StatusCode.SUCCESS, "Find students by team id successfully", studentDtos);
    }

    /**
     * We are not using StudentDto to receive the request params, but Student, since we require password.
     *
     * @param newStudent
     * @return
     */
    @PostMapping
    public Result addStudent(@RequestParam Integer courseId, @RequestParam Integer sectionId, @RequestParam String registrationToken, @RequestParam String role, @Valid @RequestBody Student newStudent) {
        Student savedStudent = this.studentService.saveStudent(courseId, sectionId, newStudent, registrationToken, role);
        StudentDto savedStudentDto = this.studentToStudentDtoConverter.convert(savedStudent);
        return new Result(true, StatusCode.SUCCESS, "Add student successfully", savedStudentDto);
    }
    
    @PutMapping("/{studentId}")
    public Result updateStudent(@PathVariable Integer studentId, @Valid @RequestBody StudentDto studentDto) {
        Student update = this.studentDtoToStudentConverter.convert(studentDto);
        Student updatedStudent = this.studentService.updateStudent(studentId, update);
        StudentDto updatedStudentDto = this.studentToStudentDtoConverter.convert(updatedStudent);
        return new Result(true, StatusCode.SUCCESS, "Update student successfully", updatedStudentDto);
    }

    @DeleteMapping("/{studentId}")
    public Result deleteStudent(@PathVariable Integer studentId) {
        this.studentService.deleteStudent(studentId);
        return new Result(true, StatusCode.SUCCESS, "Delete student successfully", null);
    }

}
