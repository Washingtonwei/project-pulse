package team.projectpulse.activity.converter;

import team.projectpulse.activity.Activity;
import team.projectpulse.activity.dto.ActivityDto;
import team.projectpulse.student.Student;
import team.projectpulse.student.StudentRepository;
import team.projectpulse.system.UserUtils;
import team.projectpulse.system.exception.ObjectNotFoundException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ActivityDtoToActivityConverter implements Converter<ActivityDto, Activity> {

    private final StudentRepository studentRepository;
    private final UserUtils userUtils;


    public ActivityDtoToActivityConverter(StudentRepository studentRepository, UserUtils userUtils) {
        this.studentRepository = studentRepository;
        this.userUtils = userUtils;
    }

    @Override
    public Activity convert(ActivityDto activityDto) {
        Activity activity = new Activity();
        activity.setActivityId(activityDto.activityId());
        activity.setWeek(activityDto.week());
        activity.setCategory(activityDto.category());
        activity.setActivity(activityDto.activity());
        activity.setDescription(activityDto.description());
        activity.setPlannedHours(activityDto.plannedHours());
        activity.setActualHours(activityDto.actualHours());
        activity.setStatus(activityDto.status());
        if (activityDto.activityId() == null) { // If the activity id is null, it means the activity is new, so we need to set the submitter and team.
            Integer userIdFromJwt = this.userUtils.getUserId();
            Student activitySubmitter = this.studentRepository.findById(userIdFromJwt)
                    .orElseThrow(() -> new ObjectNotFoundException("student", userIdFromJwt));
            activity.setStudent(activitySubmitter);
            activity.setTeam(activitySubmitter.getTeam());
        }
        return activity;
    }

}
