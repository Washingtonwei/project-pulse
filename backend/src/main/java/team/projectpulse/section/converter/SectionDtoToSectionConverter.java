package team.projectpulse.section.converter;

import team.projectpulse.course.Course;
import team.projectpulse.course.CourseRepository;
import team.projectpulse.instructor.Instructor;
import team.projectpulse.section.Section;
import team.projectpulse.section.dto.SectionDto;
import team.projectpulse.system.UserUtils;
import team.projectpulse.system.exception.ObjectNotFoundException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component
public class SectionDtoToSectionConverter implements Converter<SectionDto, Section> {

    private final UserUtils userUtils;
    private final CourseRepository courseRepository;


    public SectionDtoToSectionConverter(UserUtils userUtils, CourseRepository courseRepository) {
        this.userUtils = userUtils;
        this.courseRepository = courseRepository;
    }

    @Override
    public Section convert(SectionDto sectionDto) {
        LocalDate startDate = LocalDate.parse(sectionDto.startDate(), DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        LocalDate endDate = LocalDate.parse(sectionDto.endDate(), DateTimeFormatter.ofPattern("MM-dd-yyyy"));

        LocalTime warDueTime = LocalTime.parse(sectionDto.warDueTime(), DateTimeFormatter.ofPattern("HH:mm"));
        LocalTime peerEvaluationDueTime = LocalTime.parse(sectionDto.peerEvaluationDueTime(), DateTimeFormatter.ofPattern("HH:mm"));

        Section section = new Section(sectionDto.sectionName(), startDate, endDate, sectionDto.isActive(), sectionDto.warWeeklyDueDay(), warDueTime, sectionDto.peerEvaluationWeeklyDueDay(), peerEvaluationDueTime);
        section.setSectionId(sectionDto.sectionId());
        if (sectionDto.sectionId() == null) {
            // When creating a new section, find and set the course; when updating a section, don't set the course
            Integer courseId = this.userUtils.getUserCourseId();
            Course course = this.courseRepository.findById(courseId)
                    .orElseThrow(() -> new ObjectNotFoundException("course", courseId));
            course.addSection(section);
            // When creating a new section, set the current user as the instructor
            Instructor instructor = this.userUtils.getInstructor();
            section.addInstructor(instructor);
        }
        return section;
    }

}
