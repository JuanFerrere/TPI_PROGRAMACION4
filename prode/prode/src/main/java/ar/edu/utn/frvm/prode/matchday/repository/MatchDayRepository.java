package ar.edu.utn.frvm.prode.matchday.repository;

import ar.edu.utn.frvm.prode.matchday.entity.MatchDay;
import ar.edu.utn.frvm.prode.matchday.entity.MatchDayStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchDayRepository extends JpaRepository<MatchDay, Long> {
	boolean existsByNameIgnoreCase(String name);

	List<MatchDay> findByStatus(MatchDayStatus status);
}
