package team.projectpulse.activity.converter;

import team.projectpulse.activity.Activity;
import team.projectpulse.activity.dto.ActivityDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ActivityDtoToActivityConverter implements Converter<ActivityDto, Activity> {

    @Override
    public Activity convert(ActivityDto activityDto) {
        // The id is not read from the payload: a create has the server assign it, and an update takes it from the
        // URL. Mapping it here would let a create carry an existing id, which turns save() into a merge over that
        // activity.
        Activity activity = new Activity();
        activity.setWeek(activityDto.week());
        activity.setCategory(activityDto.category());
        activity.setActivity(activityDto.activity());
        activity.setDescription(activityDto.description());
        activity.setPlannedHours(activityDto.plannedHours());
        activity.setActualHours(activityDto.actualHours());
        activity.setStatus(activityDto.status());
        // The submitter and her team are not read from the payload: ActivityService stamps them from the caller.
        return activity;
    }

}
