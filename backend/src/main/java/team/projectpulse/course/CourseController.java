package team.projectpulse.course;

import team.projectpulse.course.converter.CourseDtoToCourseConverter;
import team.projectpulse.course.converter.CourseToCourseDtoConverter;
import team.projectpulse.course.dto.CourseDto;
import team.projectpulse.system.Result;
import team.projectpulse.system.StatusCode;
import team.projectpulse.user.userinvitation.UserInvitationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("${api.endpoint.base-url}/courses")
public class CourseController {

    private final CourseService courseService;
    private final CourseDtoToCourseConverter courseDtoToCourseConverter;
    private final CourseToCourseDtoConverter courseToCourseDtoConverter;
    private final UserInvitationService userInvitationService;


    public CourseController(CourseService courseService, CourseDtoToCourseConverter courseDtoToCourseConverter, CourseToCourseDtoConverter courseToCourseDtoConverter, UserInvitationService userInvitationService) {
        this.courseService = courseService;
        this.courseDtoToCourseConverter = courseDtoToCourseConverter;
        this.courseToCourseDtoConverter = courseToCourseDtoConverter;
        this.userInvitationService = userInvitationService;
    }

    /**
     * Find all courses this admin/instructor belongs to
     *
     * @param pageable
     * @return
     */
    @PostMapping("/search")
    public Result findCoursesByCriteria(@RequestBody Map<String, String> searchCriteria, Pageable pageable) {
        Page<Course> coursePage = this.courseService.findByCriteria(searchCriteria, pageable);
        Page<CourseDto> courseDtoPage = coursePage.map(this.courseToCourseDtoConverter::convert);
        return new Result(true, StatusCode.SUCCESS, "Find courses successfully", courseDtoPage);
    }

    @GetMapping("/{courseId}")
    public Result findCourseById(@PathVariable Integer courseId) {
        Course course = this.courseService.findCourseById(courseId);
        CourseDto courseDto = this.courseToCourseDtoConverter.convert(course);
        return new Result(true, StatusCode.SUCCESS, "Find course successfully", courseDto);
    }

    @PostMapping
    public Result addCourse(@Valid @RequestBody CourseDto courseDto) {
        Course newCourse = this.courseDtoToCourseConverter.convert(courseDto);
        Course savedCourse = this.courseService.saveCourse(newCourse);
        CourseDto savedCourseDto = this.courseToCourseDtoConverter.convert(savedCourse);
        return new Result(true, StatusCode.SUCCESS, "Add course successfully", savedCourseDto);
    }

    @PutMapping("/{courseId}")
    public Result updateCourse(@PathVariable Integer courseId, @Valid @RequestBody CourseDto courseDto) {
        Course update = this.courseDtoToCourseConverter.convert(courseDto);
        Course updatedCourse = this.courseService.updateCourse(courseId, update);
        CourseDto updatedCourseDto = this.courseToCourseDtoConverter.convert(updatedCourse);
        return new Result(true, StatusCode.SUCCESS, "Update course successfully", updatedCourseDto);
    }

    @DeleteMapping("/{courseId}")
    public Result deleteCourse(@PathVariable Integer courseId) {
        this.courseService.deleteCourse(courseId);
        return new Result(true, StatusCode.SUCCESS, "Delete course successfully", null);
    }

    @PostMapping("/{courseId}/instructors/email-invitations")
    public Result sendEmailInvitations(@PathVariable Integer courseId, @RequestBody List<String> emails) {
        this.userInvitationService.sendEmailInvitations(courseId, null, emails, "instructor");
        return new Result(true, StatusCode.SUCCESS, "Send email invitation successfully", null);
    }

}
