package team.projectpulse.activity;

import team.projectpulse.system.UserUtils;
import team.projectpulse.system.exception.ObjectNotFoundException;
import team.projectpulse.team.Team;
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

        // Caller-supplied, so this is a filter and never the boundary: the scope below is applied on top of it,
        // which is what stops a student from reading another team by naming it here.
        if (StringUtils.hasLength(searchCriteria.get("teamId"))) {
            spec = spec.and(ActivitySpecs.hasTeamId(searchCriteria.get("teamId")));
        }

        if (StringUtils.hasLength(searchCriteria.get("studentId"))) {
            spec = spec.and(ActivitySpecs.hasStudentId(searchCriteria.get("studentId")));
        }

        // The scope, resolved from the caller and not from the request: a student sees her own team's weekly activity
        // reports and no other team's (BR-team-scoped-access), while an instructor or course admin sees her course
        // section (BR-section-scoped-access). A student naming another team above therefore matches nothing rather
        // than reading that team, and naming her own team is simply redundant with the scope.
        if (this.userUtils.hasRole("ROLE_student")) {
            Team team = this.userUtils.getStudent().getTeam();
            if (team == null) {  // Student.team is optional: a student on no team owns no weekly activity reports
                return Page.empty(pageable);
            }
            spec = spec.and(ActivitySpecs.hasTeamId(team.getTeamId()));
        } else {
            spec = spec.and(ActivitySpecs.hasSectionId(this.userUtils.getUserSectionId()));
        }

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
