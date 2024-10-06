package team.projectpulse.rubric;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RubricRepository extends JpaRepository<Rubric, Integer>, JpaSpecificationExecutor<Rubric> {
}
