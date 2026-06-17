package ar.edu.utn.frvm.prode.tournament.repository;

import ar.edu.utn.frvm.prode.tournament.entity.TournamentTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para participaciones de equipos en torneos.
 */
public interface TournamentTeamRepository extends JpaRepository<TournamentTeam, Long> {

	boolean existsByTournamentId(Long tournamentId);

	boolean existsByTournamentIdAndTeamId(Long tournamentId, Long teamId);

	Optional<TournamentTeam> findByIdAndTournamentId(Long id, Long tournamentId);

	@Query("""
			select tournamentTeam
			from TournamentTeam tournamentTeam
			join fetch tournamentTeam.team team
			where tournamentTeam.tournament.id = :tournamentId
			order by tournamentTeam.groupName asc, lower(team.name) asc
			""")
	List<TournamentTeam> findByTournamentIdOrdered(Long tournamentId);
}
