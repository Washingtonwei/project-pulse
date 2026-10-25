package team.projectpulse.activity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Integer>, JpaSpecificationExecutor<Activity> {

    /**
     * The ids of the course section's students with at least one activity recorded for the week, in any status.
     */
    @Query("""
            select distinct a.student.id
            from Activity a
            where a.week = :week
              and a.team.section.sectionId = :sectionId
            """)
    Set<Integer> findReportingStudentIds(@Param("sectionId") Integer sectionId, @Param("week") String week);

}
