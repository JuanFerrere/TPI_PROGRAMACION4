package ar.edu.utn.frvm.prode.prediction.repository;

import ar.edu.utn.frvm.prode.match.entity.MatchStatus;
import ar.edu.utn.frvm.prode.prediction.entity.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {
	Optional<Prediction> findByUserIdAndMatchId(Long userId, Long matchId);

	List<Prediction> findByUserIdOrderByMatchStartTimeAsc(Long userId);

	List<Prediction> findByUserIdAndMatchStatusOrderByMatchStartTimeAsc(Long userId, MatchStatus status);

	List<Prediction> findByMatchIdOrderByCreatedAtAsc(Long matchId);

	boolean existsByMatchId(Long matchId);

	List<Prediction> findByUserIdIn(List<Long> userIds);
}
