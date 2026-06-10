package ar.edu.utn.frvm.prode.team.repository;

import ar.edu.utn.frvm.prode.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
	boolean existsByNameIgnoreCase(String name);

	Optional<Team> findByNameIgnoreCase(String name);

	List<Team> findByNameContainingIgnoreCaseOrderByNameAsc(String name);
}
