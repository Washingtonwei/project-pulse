package team.projectpulse.activity;

import team.projectpulse.activity.converter.ActivityDtoToActivityConverter;
import team.projectpulse.activity.converter.ActivityToActivityDtoConverter;
import team.projectpulse.activity.dto.ActivityDto;
import team.projectpulse.system.Result;
import team.projectpulse.system.StatusCode;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("${api.endpoint.base-url}/activities")
public class ActivityController {

    private final ActivityService activityService;
    private final ActivityToActivityDtoConverter activityToActivityDtoConverter;
    private final ActivityDtoToActivityConverter activityDtoToActivityConverter;


    public ActivityController(ActivityService activityService, ActivityToActivityDtoConverter activityToActivityDtoConverter, ActivityDtoToActivityConverter activityDtoToActivityConverter) {
        this.activityService = activityService;
        this.activityToActivityDtoConverter = activityToActivityDtoConverter;
        this.activityDtoToActivityConverter = activityDtoToActivityConverter;
    }

    @PostMapping("/search")
    public Result findActivitiesByCriteria(@RequestBody Map<String, String> searchCriteria, Pageable pageable) {
        Page<Activity> activityPage = this.activityService.findByCriteria(searchCriteria, pageable);
        Page<ActivityDto> activityDtoPage = activityPage.map(this.activityToActivityDtoConverter::convert);
        return new Result(true, StatusCode.SUCCESS, "Find activities successfully", activityDtoPage);
    }

    @GetMapping("/{activityId}")
    public Result findActivityById(@PathVariable Integer activityId) {
        Activity activity = this.activityService.findActivityById(activityId);
        ActivityDto activityDto = this.activityToActivityDtoConverter.convert(activity);
        return new Result(true, StatusCode.SUCCESS, "Find activity successfully", activityDto);
    }

    @PostMapping
    public Result addActivity(@Valid @RequestBody ActivityDto activityDto) {
        Activity newActivity = this.activityDtoToActivityConverter.convert(activityDto);
        Activity savedActivity = this.activityService.saveActivity(newActivity);
        ActivityDto savedActivityDto = this.activityToActivityDtoConverter.convert(savedActivity);
        return new Result(true, StatusCode.SUCCESS, "Add activity successfully", savedActivityDto);
    }

    @PutMapping("/{activityId}")
    public Result updateActivity(@PathVariable Integer activityId, @Valid @RequestBody ActivityDto activityDto) {
        Activity update = this.activityDtoToActivityConverter.convert(activityDto);
        Activity updatedActivity = this.activityService.updateActivity(activityId, update);
        ActivityDto updatedActivityDto = this.activityToActivityDtoConverter.convert(updatedActivity);
        return new Result(true, StatusCode.SUCCESS, "Update activity successfully", updatedActivityDto);
    }

    @DeleteMapping("/{activityId}")
    public Result deleteActivity(@PathVariable Integer activityId) {
        this.activityService.deleteActivity(activityId);
        return new Result(true, StatusCode.SUCCESS, "Delete activity successfully", null);
    }

    @PatchMapping("/{activityId}/comments")
    public Result addActivityComment(@PathVariable Integer activityId, @RequestBody Map<String, String> commentMap) {
        String comment = commentMap.get("comment");
        Activity updatedActivity = this.activityService.addActivityComment(activityId, comment);
        ActivityDto updatedActivityDto = this.activityToActivityDtoConverter.convert(updatedActivity);
        return new Result(true, StatusCode.SUCCESS, "Add comment successfully", null);
    }

}
