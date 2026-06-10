package ar.edu.utn.frvm.prode.match.repository;

import ar.edu.utn.frvm.prode.match.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
	boolean existsByMatchDayId(Long matchDayId);

	boolean existsByHomeTeamId(Long teamId);

	boolean existsByAwayTeamId(Long teamId);

	List<Match> findByMatchDayIdOrderByStartTimeAsc(Long matchDayId);

	List<Match> findAllByOrderByStartTimeAsc();
}
