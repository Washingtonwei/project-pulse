package team.projectpulse.section;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectionRepository extends JpaRepository<Section, Integer>, JpaSpecificationExecutor<Section> {
    @Query("SELECT s FROM Section s LEFT JOIN FETCH s.students WHERE s.isActive = true")
    List<Section> findActiveSectionsWithStudents();

}
