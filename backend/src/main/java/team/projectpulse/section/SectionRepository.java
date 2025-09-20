package team.projectpulse.section;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectionRepository extends JpaRepository<Section, Integer>, JpaSpecificationExecutor<Section> {
    /**
     * Sections that are active, explicitly list the given ISO week key in activeWeeks.
     * Students are loaded for emailing; activeWeeks are NOT fetched (filtered in SQL).
     */
    @EntityGraph(attributePaths = {"students"}) // only fetch students
    @Query("""
            select s
            from Section s
            where s.isActive = true
              and :weekKey member of s.activeWeeks
            """)
    List<Section> findReminderEligibleSectionsForWeek(@Param("weekKey") String weekKey);

}
