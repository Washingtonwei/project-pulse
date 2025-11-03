package team.projectpulse.activity;

import team.projectpulse.system.UserUtils;
import team.projectpulse.system.exception.ObjectNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
@Transactional
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserUtils userUtils;


    public ActivityService(ActivityRepository activityRepository, UserUtils userUtils) {
        this.activityRepository = activityRepository;
        this.userUtils = userUtils;
    }

    public Page<Activity> findByCriteria(Map<String, String> searchCriteria, Pageable pageable) {
        Specification<Activity> spec = Specification.unrestricted(); // Start with an unrestricted specification, matching all objects.

        if (StringUtils.hasLength(searchCriteria.get("week"))) {
            spec = spec.and(ActivitySpecs.hasWeek(searchCriteria.get("week")));
        }

        if (StringUtils.hasLength(searchCriteria.get("startWeek")) && StringUtils.hasLength(searchCriteria.get("endWeek"))) {
            spec = spec.and(ActivitySpecs.hasWeekBetween(searchCriteria.get("startWeek"), searchCriteria.get("endWeek")));
        }

        if (StringUtils.hasLength(searchCriteria.get("teamId"))) {
            spec = spec.and(ActivitySpecs.hasTeamId(searchCriteria.get("teamId")));
        }

        if (StringUtils.hasLength(searchCriteria.get("studentId"))) {
            spec = spec.and(ActivitySpecs.hasStudentId(searchCriteria.get("studentId")));
        }

        Integer sectionId = this.userUtils.getUserSectionId();

        spec = spec.and(ActivitySpecs.hasSectionId(sectionId));

        return this.activityRepository.findAll(spec, pageable);
    }

    public Activity findActivityById(Integer activityId) {
        return this.activityRepository.findById(activityId)
                .orElseThrow(() -> new ObjectNotFoundException("activity", activityId));
    }

    public Activity saveActivity(Activity newActivity) {
        return this.activityRepository.save(newActivity);
    }

    // We are not updating the comments field here.
    public Activity updateActivity(Integer activityId, Activity update) {
        return this.activityRepository.findById(activityId)
                .map(oldActivity -> {
                    oldActivity.setCategory(update.getCategory());
                    oldActivity.setActivity(update.getActivity());
                    oldActivity.setDescription(update.getDescription());
                    oldActivity.setPlannedHours(update.getPlannedHours());
                    oldActivity.setActualHours(update.getActualHours());
                    oldActivity.setStatus(update.getStatus());
                    return this.activityRepository.save(oldActivity);
                })
                .orElseThrow(() -> new ObjectNotFoundException("activity", activityId));
    }

    public void deleteActivity(Integer activityId) {
        this.activityRepository.findById(activityId)
                .orElseThrow(() -> new ObjectNotFoundException("activity", activityId));
        this.activityRepository.deleteById(activityId);
    }

    public Activity addActivityComment(Integer activityId, String comment) {
        return this.activityRepository.findById(activityId)
                .map(oldActivity -> {
                    oldActivity.addComment(comment);
                    // We are not updating the updatedAt field here.
                    return this.activityRepository.save(oldActivity);
                })
                .orElseThrow(() -> new ObjectNotFoundException("activity", activityId));
    }

}
