package team.projectpulse.activity.converter;

import team.projectpulse.activity.Activity;
import team.projectpulse.activity.dto.ActivityDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ActivityToActivityDtoConverter implements Converter<Activity, ActivityDto> {

    @Override
    public ActivityDto convert(Activity activity) {
        return new ActivityDto(
                activity.getActivityId(),
                activity.getStudent().getId(),
                activity.getStudent().getFirstName() + " " + activity.getStudent().getLastName(),
                activity.getWeek(),
                activity.getTeam().getTeamId(),
                activity.getTeam().getTeamName(),
                activity.getCategory(),
                activity.getActivity(),
                activity.getDescription(),
                activity.getPlannedHours(),
                activity.getActualHours(),
                activity.getStatus(),
                activity.getComments(),
                activity.getCreatedAt(),
                activity.getUpdatedAt()
        );
    }

}
