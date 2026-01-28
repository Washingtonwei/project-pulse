package team.projectpulse.ram.requirement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RequirementArtifactRepository extends JpaRepository<RequirementArtifact, Long>, JpaSpecificationExecutor<RequirementArtifact> {
}
