package team.projectpulse.ram.requirement;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface ArtifactKeySequenceRepository extends JpaRepository<ArtifactKeySequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                select s from ArtifactKeySequence s
                where s.team.teamId = :teamId and s.type = :type
            """)
    Optional<ArtifactKeySequence> findForUpdate(@Param("teamId") Integer teamId,
                                                @Param("type") RequirementArtifactType type);

    Optional<ArtifactKeySequence> findByTeamTeamIdAndType(Integer teamId, RequirementArtifactType type);

}