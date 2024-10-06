package team.projectpulse.evaluation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PeerEvaluationRepository extends JpaRepository<PeerEvaluation, Integer> {

    List<PeerEvaluation> findByWeek(String week);

    List<PeerEvaluation> findByWeekAndEvaluateeId(String week, Integer studentId);

    Optional<PeerEvaluation> findByWeekAndEvaluatorIdAndEvaluateeId(String week, Integer evaluatorId, Integer evaluateeId);

    List<PeerEvaluation> findByWeekAndEvaluatorId(String week, Integer evaluatorId);
    
}
